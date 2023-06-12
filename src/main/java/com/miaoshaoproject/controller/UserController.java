package com.miaoshaoproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaoproject.controller.view.UserVO;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.response.CommonReturnType;
import com.miaoshaoproject.service.UserService;
import com.miaoshaoproject.service.model.UserModel;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.filters.ExpiresFilter;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",originPatterns = "*")
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    HttpServletResponse httpServletResponse;

    @Autowired
    RedisTemplate redisTemplate;



    /*
    * telephone是用户注册的手机号
    * 要对比的password是加密后的pwd*/
    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType login(@RequestParam("telphone")String telphone,
                                  @RequestParam("password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参较验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
                org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机或者密码错误！");
        }
        //登录服务,validate登录是否合法，若登录成功会有一个userModel对象返回
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));
        //设置samesite=None, httponly,secure等属性
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
                .httpOnly(true)       // 禁止js读取
                .secure(true)     // 在http下也传输
                .domain("localhost")// 域名
                .path("/")       // path
                .maxAge(3600)  // 1个小时候过期
                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build()
                ;
        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        //将登陆凭证加入到session内
        //修改成若用户登陆成功，则将对应的登陆信息和凭证存入到redis中
        //生成token，UUID
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");
        //建立token和用户登陆态之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);//在redis中将token和用户模型关联并储存
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);//设置在redis中存储的时间范围
//        HttpSession session = this.httpServletRequest.getSession();
//        session.setAttribute("IS_LOGIN", true);
//        session.setAttribute("LOGIN_USER", userModel);
        //下发了token
        System.out.println(uuidToken);
        return CommonReturnType.create(uuidToken);

    }



    //用户注册的接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType register(@RequestParam("telphone") String telphone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("name")String name,
                                     @RequestParam("gender")Integer gender,
                                     @RequestParam("age")Integer age,
                                     @RequestParam("pwd")String pwd) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //otpCode是否正确
        HttpSession session = this.httpServletRequest.getSession();
        String inSessionCode = (String)session.getAttribute(telphone);
        if (!StringUtils.equals(otpCode,inSessionCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"Invalid otpCode");
        }

        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender.byteValue());
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("Register by phone");
        userModel.setEncrptPwd(this.EncodeByMd5(pwd));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //确定加密的计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64.Encoder encoder = Base64.getEncoder();
        //加密字符串
        String s = encoder.encodeToString(str.getBytes("utf-8"));
        return s;


    }

    //用户获得otp短信的接口
    @ResponseBody
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST},consumes = (CONTENT_TYPE_FORMED))
    public CommonReturnType getOtp(@RequestParam("telphone")String telphone){
        //按照一定的规则生成otp
        Random random = new Random();
        int i = random.nextInt(99999);//i的范围1-99999
        i += 10000;//i的范围10000-99999
        String s = String.valueOf(i);
        //设置samesite=None, httponly,secure等属性
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
                .httpOnly(true)       // 禁止js读取
                .secure(true)     // 在http下也传输
                .domain("localhost")// 域名
                .path("/")       // path
                .maxAge(3600)  // 1个小时候过期
                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build()
                ;
        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //将otp和用户手机号关联利用 httpsession绑定手机号于otp
        HttpSession session = this.httpServletRequest.getSession();
        session.setAttribute(telphone,s);
        System.out.println("otpcode "+s);

        //将otp通过短信发送给用户忽略
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam("id") Integer id)throws BusinessException{
        UserModel user = userService.getUserById(id);
        if (user == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIT);
        }
//        int i = 1/0;
        UserVO userVO = convertFromModel(user);
        CommonReturnType type = CommonReturnType.create(userVO);
        return type;
    }
    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }


}
