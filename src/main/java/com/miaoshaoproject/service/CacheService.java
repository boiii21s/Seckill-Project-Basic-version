package com.miaoshaoproject.service;
//该接口是用于本地缓存用的接口
//封装本地缓存操作类
public interface CacheService {
    //存
    void setcommonCache(String key, Object value);
    //取
    Object getFromCommonCache(String key);
}
