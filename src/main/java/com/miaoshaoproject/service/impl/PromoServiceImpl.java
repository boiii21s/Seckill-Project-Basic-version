package com.miaoshaoproject.service.impl;

import com.miaoshaoproject.dao.PromoDOMapper;
import com.miaoshaoproject.dataobject.PromoDO;
import com.miaoshaoproject.service.PromoService;
import com.miaoshaoproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


@Service
public class PromoServiceImpl implements PromoService {

    @Autowired(required = false)
    PromoDOMapper promoDOMapper;
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
