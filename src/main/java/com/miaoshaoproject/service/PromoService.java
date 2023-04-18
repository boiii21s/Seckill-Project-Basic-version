package com.miaoshaoproject.service;

import com.miaoshaoproject.service.model.PromoModel;

public interface PromoService {

    //根据itemId获得即将进行或者正在进行的item信息
    PromoModel getPromoByItemId(Integer itemId);
}
