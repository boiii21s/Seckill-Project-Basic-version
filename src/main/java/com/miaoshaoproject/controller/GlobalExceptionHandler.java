package com.miaoshaoproject.controller;


import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.response.CommonReturnType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonReturnType doError(HttpServletRequest request, HttpServletResponse response,Exception ex){
        ex.printStackTrace();
        Map<String,Object> responseData = new HashMap<>();
        if (ex instanceof BusinessException){
            BusinessException businessException = (BusinessException)ex;
            responseData.put("errCode",businessException.getErrCode());
            responseData.put("errMsg",businessException.getErrMsg());
        } else if (ex instanceof ServletRequestBindingException) {
            responseData.put("errCode", EmBusinessError.UNKOWN_ERROR.getErrCode());
            responseData.put("errMsg","url绑定路由器错误");
        } else if (ex instanceof NoHandlerFoundException) {
            responseData.put("errCode", EmBusinessError.UNKOWN_ERROR.getErrCode());
            responseData.put("errMsg","没有找到对应的访问了路径");
        }else {
            responseData.put("errCode", EmBusinessError.UNKOWN_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKOWN_ERROR.getErrCode());
        }
        return CommonReturnType.create(responseData,"fail");
    }
}
