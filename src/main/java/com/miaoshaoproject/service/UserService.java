package com.miaoshaoproject.service;

import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.service.model.UserModel;

public interface UserService {

    public UserModel getUserById(Integer id);
    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);

    void register(UserModel userModel) throws BusinessException;

    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;
}
