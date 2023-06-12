package com.miaoshaoproject.service.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class ItemModel implements Serializable {
    private Integer id;

    //商品名
    @NotBlank(message = "商品名不能为空")
    private String title;
    //商品价格
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "价格不能小于0")
    private BigDecimal price;
    //商品描述
    @NotBlank(message = "商品描述不能为空")
    private String description;
    //商品销量

    private Integer sales;
    //商品库存
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
    //商品图片
    @NotBlank(message = "商品图片不能为空")
    private String imgUrl;

    //使用聚合模型,如果promoModel不为空就是有活动
    private PromoModel promoModel;

    public PromoModel getPromoModel() {
        return promoModel;
    }

    public void setPromoModel(PromoModel promoModel) {
        this.promoModel = promoModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
