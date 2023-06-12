package com.miaoshaoproject.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaoshaoproject.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    //该对象是guava cache的对象
    private Cache<String,Object> cache = null;

    @PostConstruct//该注释代表着spring在加载该类到容器中的时候，会在创建该类实例时优先执行注释下的方法
    public void init(){
        cache = CacheBuilder.newBuilder()
                //设置缓存的初始化容量为10
                .initialCapacity(10)
                //设置缓存的最大容量为50，超过50个以后会按照lru策略淘汰对象
                .maximumSize(50)
                //写入缓存将在30秒以后过期
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }
    @Override
    public void setcommonCache(String key, Object value) {
        cache.put(key,value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return cache.getIfPresent(key);
    }
}
