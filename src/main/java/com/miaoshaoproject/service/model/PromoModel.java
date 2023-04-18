package com.miaoshaoproject.service.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Date;

public class PromoModel {
    private Integer id;
    //活动状态 1->未开始 2->进行中 3->已结束
    private Integer status;
    //秒杀活动的名字
    private String name;
    //秒杀开始的时间
    private DateTime startDate;
    //秒杀结束时间
    private DateTime endDate;
    //秒杀活动适用商品
    private Integer itemId;
    //活动价格
    private BigDecimal promoItemPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }
}
