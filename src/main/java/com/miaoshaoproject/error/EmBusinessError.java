package com.miaoshaoproject.error;

public enum EmBusinessError implements CommonError {
    //通用错误类型10001开头
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKOWN_ERROR(10002,"未知错误"),
    //20000开头为用户信息相关错误定义
    USER_NOT_EXIT(20001,"用户不存在"),
    LOGIN_FAILED(20002,"手机号或密码错误"),
    USER_NOT_LOGIN(20003,"用户尚未登陆"),
    //30000开头是商品相关错误
    PRODUCT_NOT_EXIT(30001,"产品不存在"),

    //40000开头是交易信息有误
    STOCK_NOT_ENOUGH(40001,"库存不足！"),
    MQ_SEND_FAIL(40002,"异步消息发送失败"),
    RATELIMIT(40003,"活动太火爆，请稍后再尝试"),
    ;

    private EmBusinessError(int errCode,String msg){
        this.errCode = errCode;
        this.errMsg = msg;
    }
    private int errCode;
    private String errMsg;
    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String msg) {
        this.errMsg = msg;
        return this;
    }
}
