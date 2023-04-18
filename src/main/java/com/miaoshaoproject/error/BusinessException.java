package com.miaoshaoproject.error;

public class BusinessException extends Exception implements CommonError{
    private CommonError error;//这个error传进来的是即使上市EmBusinessError
    //直接接收EmBusinessError的传参用于构造业务异常
    public BusinessException(CommonError error){
        super();
        this.error = error;
    }
    //接收自定义的errMsg
    public BusinessException(CommonError error,String msg){
        super();
        this.error = error;
        this.error.setErrMsg(msg);
    }

    @Override
    public int getErrCode() {
        return this.error.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.error.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String msg) {
        this.error.setErrMsg(msg);
        return this;

    }
}
