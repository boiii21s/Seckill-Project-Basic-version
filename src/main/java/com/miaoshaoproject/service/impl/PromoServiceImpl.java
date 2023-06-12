package com.miaoshaoproject.service.impl;

import com.miaoshaoproject.dao.PromoDOMapper;
import com.miaoshaoproject.dataobject.PromoDO;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.service.ItemService;
import com.miaoshaoproject.service.PromoService;
import com.miaoshaoproject.service.UserService;
import com.miaoshaoproject.service.model.ItemModel;
import com.miaoshaoproject.service.model.PromoModel;
import com.miaoshaoproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class PromoServiceImpl implements PromoService {

    @Autowired(required = false)
    PromoDOMapper promoDOMapper;

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByItemId(Integer itemId){
        //获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //do ->model
        PromoModel promoModel = convertFromDoToModel(promoDO);

        //判断活动是否在进行中或者即将进行
        if (promoModel == null){
            return null;
        }

        if (promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    //活动发布
    @Override
    public void publishPromo(Integer promoId) throws BusinessException {
        //根据活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        //若活动不存在，或者活动号码有误，则返回
        if (promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0 || promoDO == null){
            throw new BusinessException(EmBusinessError.UNKOWN_ERROR,"活动信息错误");

        }
        //从promoDo的itemid获得相应的ItemModel，从而活动item_stock
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        //将itemModel中的库存存入redis缓存中
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());

        //将大闸的限制数字设到redis中
        redisTemplate.opsForValue().set("promo_door_count_"+promoId,itemModel.getStock().intValue()*3);

    }

    @Override
    public String generateSecKillToken(Integer promoId,Integer itemId,Integer userId) {
        //先判断该商品的库存是否售罄，如果售罄不在进行后续操作直接返回库存不足
        if (redisTemplate.hasKey("promo_item_stock_invalid"+itemId)){
            return null;
        }

        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        //do ->model
        PromoModel promoModel = convertFromDoToModel(promoDO);

        //判断活动是否在进行中或者即将进行
        if (promoModel == null){
            return null;
        }

        if (promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else {
            promoModel.setStatus(2);
        }
        //判断活动是否正在进行
        if (promoModel.getStatus().intValue() != 2){
            return null;
        }
        //判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null){
            return null;
        }
        //判断商品是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null){
           return null;
        }
        //获取秒杀大闸count数量
        Long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1);
        if (result < 0){//已经没有多余的令牌
            return null;
        }
        //生成token并存入缓存中，有效期5min
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid"+userId+"_itemid"+itemId,token);
        redisTemplate.expire("promo_token_"+promoId+"_userid"+userId+"_itemid"+itemId,5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel convertFromDoToModel(PromoDO promoDO) {
        if (promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setName(promoDO.getPromoName());
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
