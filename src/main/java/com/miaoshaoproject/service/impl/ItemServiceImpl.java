package com.miaoshaoproject.service.impl;

import com.miaoshaoproject.dao.ItemDoMapper;
import com.miaoshaoproject.dao.ItemStockDoMapper;
import com.miaoshaoproject.dao.StockLogDOMapper;
import com.miaoshaoproject.dataobject.ItemDo;
import com.miaoshaoproject.dataobject.ItemStockDo;
import com.miaoshaoproject.dataobject.StockLogDO;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.mq.MqProducer;
import com.miaoshaoproject.service.ItemService;
import com.miaoshaoproject.service.PromoService;
import com.miaoshaoproject.service.model.ItemModel;
import com.miaoshaoproject.service.model.PromoModel;
import com.miaoshaoproject.validator.ValidationResult;
import com.miaoshaoproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired(required = false)
    ItemDoMapper itemDoMapper;
    @Autowired(required = false)
    ItemStockDoMapper itemStockDoMapper;

    @Autowired
    @Lazy
    PromoService promoService;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private StockLogDOMapper stockLogDOMapper;

//    @Autowired
//    MqProducer producer;

//    @Autowired
//    MqProducer producer;
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //较验入参
        if (itemModel == null){
            return null;
        }
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasError()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //把itemmode转成daoObject加入到数据库中
        ItemDo itemDo = convertFromItemModel(itemModel);
        itemDoMapper.insertSelective(itemDo);
        itemModel.setId(itemDo.getId());
        ItemStockDo itemStockDo = convertStcckFromItemModel(itemModel);
        itemStockDoMapper.insertSelective(itemStockDo);

        //写入数据库


        //把写入的对象返回
        return getItemById(itemModel.getId());
    }

    public ItemDo convertFromItemModel(ItemModel itemModel){
        if (itemModel == null){
            return null;
        }
        ItemDo itemDo = new ItemDo();
        BeanUtils.copyProperties(itemModel,itemDo);
        itemDo.setPrice(itemModel.getPrice().doubleValue());
        return itemDo;
    }

    public ItemStockDo convertStcckFromItemModel(ItemModel itemModel){
        if (itemModel == null){
            return null;
        }
        ItemStockDo itemStockDo = new ItemStockDo();
        itemStockDo.setItemId(itemModel.getId());
        itemStockDo.setStock(itemModel.getStock());
        return itemStockDo;
    }
    @Override
    public List<ItemModel> itemList() {
        List<ItemDo> itemDolList = itemDoMapper.listItem();
        List<ItemModel> itemModelList = itemDolList.stream().map(itemDo -> {
            ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());
            ItemModel itemModel = convertFromItemDoToItemModel(itemDo,itemStockDo);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
         //获得对应id的dao对象
        ItemDo itemDo = itemDoMapper.selectByPrimaryKey(id);
        if (itemDo == null){
            return null;
        }
        //处理获得库存数据
        ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(id);
        //把dao转成itemModel给controller处理
        ItemModel itemModel = convertFromItemDoToItemModel(itemDo, itemStockDo);
        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(id);
        if (promoModel != null && promoModel.getStatus().intValue() != 3){
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    //item以及promo模型在缓存的验证
    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null){//若当前缓存中没有，去数据库中取
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id,itemModel);
            //设置过期时间
            redisTemplate.expire("item_validate_" + id,10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        //int i = itemStockDoMapper.decreaseStock(itemId, amount);
        //先不需要减去数据库中的库存，现在redis缓存中减
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue()*-1);
        if (result > 0){
            //把异步发送消息的步骤提到另外一层，扣减库存是只需要确保缓存中有库存可扣
//            boolean mqResult = producer.asyncReduceStock(itemId, amount);
//            if (!mqResult){
//                //若消息发送失败，回滚库存
//                redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
//                return false;
//            }else return true;
            return true;

        }else if (result == 0){
            //若库存减掉以后为0，返回成功，但是要有售罄标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid"+itemId,"true");
            return true;
        } else {
            //更新库存失败，库存也需要回滚
            increaseStock(itemId, amount);
            return false;
        }
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
        return true;
    }

    //在创建订单之前，初始化流水
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStatus(1);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }

    //  @Override
//    public boolean asynDecreaseStock(Integer itemId, Integer amount) {
//        boolean mqResult = producer.asyncReduceStock(itemId, amount);
//        return mqResult;
//    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemDoMapper.increaseSales(itemId,amount);
    }

    private ItemModel convertFromItemDoToItemModel(ItemDo itemDo, ItemStockDo itemStockDo) {
        if (itemDo == null){
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDo,itemModel);
        itemModel.setPrice(new BigDecimal(itemDo.getPrice()));
        itemModel.setStock(itemStockDo.getStock());
        return itemModel;
    }


}
