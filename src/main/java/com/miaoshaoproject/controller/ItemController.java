package com.miaoshaoproject.controller;

import com.miaoshaoproject.controller.view.ItemVO;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.response.CommonReturnType;
import com.miaoshaoproject.service.CacheService;
import com.miaoshaoproject.service.ItemService;
import com.miaoshaoproject.service.PromoService;
import com.miaoshaoproject.service.model.ItemModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller("/item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", originPatterns = "*")
public class ItemController extends BaseController {

    @Autowired
    ItemService itemService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    HttpServletResponse httpServletResponse;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    CacheService cacheService;

    @Autowired

    PromoService promoService;


    //获取商品列表页面浏览
    @RequestMapping(value = "/list")
    @ResponseBody
    public CommonReturnType listItem() {
//        List<ItemModel> itemModelList = itemService.itemList();
//        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
//            ItemVO itemVO = convertFromModelToVO(itemModel);
//            return itemVO;
//        }).collect(Collectors.toList());
//        return CommonReturnType.create(itemVOList);
        //设置samesite=None, httponly,secure等属性
//        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
//                .httpOnly(true)       // 禁止js读取
//                .secure(true)     // 在http下也传输
//                .domain("localhost")// 域名
//                .path("/")       // path
//                .maxAge(3600)  // 1个小时候过期
//                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
//                .build()
//                ;
//        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        List<ItemModel> itemModelList = itemService.itemList();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertFromModelToVO(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        //System.out.println(httpServletRequest.getSession());
        return CommonReturnType.create(itemVOList);
    }

    //添加商品信息的方法
    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType createItem(@RequestParam("title") String title,
                                       @RequestParam("price") BigDecimal price,
                                       @RequestParam("description") String description,
                                       @RequestParam("stock") Integer stock,
                                       @RequestParam("imgUrl") String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        ItemModel item = itemService.createItem(itemModel);
        //把创建好的item用VO返回给前端

        ItemVO itemVO = convertFromModelToVO(itemModel);
        return CommonReturnType.create(itemVO);
    }

    //活动发布
    @RequestMapping(value = "/publishpromo", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType publishpromo(@RequestParam("id") Integer promo_id) throws BusinessException {
        promoService.publishPromo(promo_id);
        return CommonReturnType.create(null);
    }


        //获取商品信息
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam("id") Integer id) throws BusinessException {
        //先看看本地缓存中是否有该信息
        ItemModel itemModel = null;
        itemModel = (ItemModel)cacheService.getFromCommonCache("item_" + id);
        //如本地缓存中没有，则到redis中查询
        if (itemModel == null){
            //查询redis中是否存有该id的商品详情信息
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
            //如果redis中去得到itemModel为空则向service下游的数据库中取
            if (itemModel == null){
                itemModel = itemService.getItemById(id);
                if (itemModel == null) {
                    throw new BusinessException(EmBusinessError.PRODUCT_NOT_EXIT);
                }
                //把数据库取得的model放入redis内
                redisTemplate.opsForValue().set("item_"+id,itemModel);
                redisTemplate.expire("item"+id,10, TimeUnit.MINUTES);
            }
            //也需要把数据放入本地缓存中
            cacheService.setcommonCache("item_"+id,itemModel);
        }


        ItemVO itemVO = convertFromModelToVO(itemModel);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO convertFromModelToVO(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        //看是否有正在进行的活动
        if (itemModel.getPromoModel() != null){
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }else{
            itemVO.setPromoStatus(0);
        }

        return itemVO;
    }

}
