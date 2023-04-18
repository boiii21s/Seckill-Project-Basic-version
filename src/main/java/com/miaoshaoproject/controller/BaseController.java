package com.miaoshaoproject.controller;

import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.response.CommonReturnType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;

public class BaseController {
    public static final String  CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";



    //定义exceptionhandler解决没有被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex){
        HashMap<String,Object> response = new HashMap<>();
        if (ex instanceof BusinessException){
            BusinessException businessException = (BusinessException)ex;
            response.put("errCode",businessException.getErrCode());
            response.put("errMsg",businessException.getErrMsg());
        }else{
            response.put("errCode", EmBusinessError.UNKOWN_ERROR.getErrCode());
            response.put("errMsg",EmBusinessError.UNKOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(response,"fail");
    }
}
