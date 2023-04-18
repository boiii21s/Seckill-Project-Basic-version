package com.miaoshaoproject.service.model;

import java.math.BigDecimal;

//用户下单的交易模型
public class OrderModel {
    //20180101000001日期加其他信息
    private String id;
    //用户id
    private Integer userId;
    //购买的商品id
    private Integer itemId;
    //若非空，则表示以秒杀活动下单
    private Integer promoId;
    //若promoId非空，则表示以秒杀价格下单
    private BigDecimal itemPrice;
    //购买商品的数量
    private Integer amount;
    //一共消费的金额
    private BigDecimal orderPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }
}
