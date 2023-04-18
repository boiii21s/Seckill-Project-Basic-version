package com.miaoshaoproject.controller;

import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.response.CommonReturnType;
import com.miaoshaoproject.service.OrderService;
import com.miaoshaoproject.service.UserService;
import com.miaoshaoproject.service.model.OrderModel;
import com.miaoshaoproject.service.model.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns="*",allowCredentials = "true")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;



    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    HttpServletResponse httpServletResponse;


    //封装下单请求
@RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
@ResponseBody
public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                    @RequestParam(name = "amount") Integer amount,
                                    @RequestParam(name = "promoId",required = false) Integer promoId) throws BusinessException {

    //设置samesite=None, httponly,secure等属性
//    ResponseCookie cookie = ResponseCookie.from("JSESSIONID", httpServletRequest.getSession().getId() ) // key & value
//            .httpOnly(true)       // 禁止js读取
//            .secure(true)     // 在http下也传输
//            .domain("localhost")// 域名
//            .path("/")       // path
//            .maxAge(3600)  // 1个小时候过期
//            .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
//            .build()
//            ;
//    httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    //获取用户登录信息
    HttpSession session = httpServletRequest.getSession();
    Boolean isLogin = (Boolean) session.getAttribute("IS_LOGIN");

    if (isLogin == null || !isLogin.booleanValue()) {
        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能下单");
    }
    UserModel userModel = (UserModel) session.getAttribute("LOGIN_USER");



    OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId,amount);

    return CommonReturnType.create(null);
}
}
