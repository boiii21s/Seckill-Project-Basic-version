package com.miaoshaoproject.service;

import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.service.model.PromoModel;

public interface PromoService {

    //根据itemId获得即将进行或者正在进行的item信息
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId) throws BusinessException;

    //生成秒杀用的令牌
    String generateSecKillToken(Integer promoId,Integer itemId,Integer userId);
}
