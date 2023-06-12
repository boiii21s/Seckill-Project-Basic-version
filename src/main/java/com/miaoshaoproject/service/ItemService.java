package com.miaoshaoproject.service;

import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> itemList();

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //item及promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //扣减库存(redis)
    boolean decreaseStock(Integer itemId, Integer amount);
    //消息发展不成功库存回滚库存(redis)
    boolean increaseStock(Integer itemId, Integer amount);

    //异步更新产品库存
   // boolean asynDecreaseStock(Integer itemId, Integer amount);

    //初始化库存流水
    String initStockLog(Integer itemId, Integer amount);




    void increaseSales(Integer itemId, Integer amount);
}
