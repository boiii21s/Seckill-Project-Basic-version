package com.miaoshaoproject.service;

import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.service.model.OrderModel;

public interface OrderService {

    //选1）通过前端url的秒杀活动id，在下单接口内较验id是否存在，活动是否开始
    //直接在下单接口内判断对应商品是否有活动
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId,Integer amount) throws BusinessException;

}
