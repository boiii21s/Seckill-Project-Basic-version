package com.miaoshaoproject.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.mq.MqProducer;
import com.miaoshaoproject.response.CommonReturnType;
import com.miaoshaoproject.service.ItemService;
import com.miaoshaoproject.service.OrderService;
import com.miaoshaoproject.service.PromoService;
import com.miaoshaoproject.service.UserService;
import com.miaoshaoproject.service.model.OrderModel;
import com.miaoshaoproject.service.model.UserModel;
import com.miaoshaoproject.utils.CodeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns="*",allowCredentials = "true")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    HttpServletResponse httpServletResponse;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MqProducer mqProducer;

    private ThreadPoolExecutor threadPoolExecutor;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
        threadPoolExecutor = new ThreadPoolExecutor(15,15,0L, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
        //一秒钟限制100个tps
        orderCreateRateLimiter = RateLimiter.create(100);
    }

    //生成验证码
    @RequestMapping(value = "/generateverifycode", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public void generateverifycode(HttpServletResponse response) throws BusinessException, IOException {


        //验证用户登陆态
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能生成验证码");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "已超时，请重新登陆");
        }
        Map<String,Object> map = CodeUtil.generateCodeAndPic();
        //把用户id和验证码做绑定
        redisTemplate.opsForValue().set("verify_code_"+userModel.getId(),map.get("code"));
        redisTemplate.expire("verify_code_"+userModel.getId(),5,TimeUnit.MINUTES);
        ImageIO.write((RenderedImage)map.get("codePic"),"jpeg",response.getOutputStream());

    }

    //生成秒杀令牌promoToken
    @RequestMapping(value = "/generatetoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatePromoToken(@RequestParam(name = "itemId") Integer itemId,
                                               @RequestParam(name = "promoId") Integer promoId,
                                               @RequestParam(name = "verifyCode") String verifyCode) throws BusinessException {
        //取得token
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能下单");
        }
        //获取用户登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "已超时，请重新登陆");
        }
        //通过verifycode验证验证码的有效性
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_"+userModel.getId());
        if (redisVerifyCode == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法");
        }
        if (!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法，验证码错误");
        }

        //获取秒杀的令牌
        String secKillToken = promoService.generateSecKillToken(promoId, itemId, userModel.getId());
        if (secKillToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        return CommonReturnType.create(secKillToken);
    }


        //封装下单请求
@RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
@ResponseBody
public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                    @RequestParam(name = "amount") Integer amount,
                                    @RequestParam(name = "promoId",required = false) Integer promoId,
                                    @RequestParam(name = "promoToken",required = false) String promoToken) throws BusinessException {

        if (!orderCreateRateLimiter.tryAcquire()){//该方法返回false就是没有获得令牌
            throw  new BusinessException(EmBusinessError.RATELIMIT);
        }

    //取得登陆token
    String token = httpServletRequest.getParameterMap().get("token")[0];
    if (StringUtils.isEmpty(token)){
        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能下单");
    }
    //获取用户登陆信息
    UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
    if (userModel == null){
        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "已超时，请重新登陆");
    }
    //验证promoToken的合法性
    if (promoId != null){
        String redisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid"+userModel.getId()+"_itemid"+itemId);
        if (redisPromoToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌较验失败");
        }
        if (!StringUtils.equals(promoToken,redisPromoToken)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌较验失败");
        }
    }

    //同步调用线程池的submit方法
    //submit方法中执行库存流水，下订单的操作
    //拥塞窗口为20的等待队列，用队列来泄洪
    Future<Object> future = threadPoolExecutor.submit(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
            //先加入库存流水init（）状态
            String stockLogId = itemService.initStockLog(itemId, amount);

            //流水设置完再调用异步消息producer即，在producer内执行createorder的操作
            boolean res = mqProducer.transactionAsyncReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId);
            if (!res) {
                throw new BusinessException(EmBusinessError.UNKOWN_ERROR, "下单失败");
            }
            return null;
        }
    });
    try {
        future.get();//future.get()返回的是线程池中callable中任务的执行结果
    } catch (InterruptedException e) {
        throw new BusinessException(EmBusinessError.UNKOWN_ERROR);
    } catch (ExecutionException e) {
        throw new BusinessException(EmBusinessError.UNKOWN_ERROR);
    }


    return CommonReturnType.create(null);
}
}
