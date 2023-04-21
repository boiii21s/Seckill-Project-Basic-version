package com.miaoshaoproject.service.impl;

import com.miaoshaoproject.dao.ItemDoMapper;
import com.miaoshaoproject.dao.ItemStockDoMapper;
import com.miaoshaoproject.dataobject.ItemDo;
import com.miaoshaoproject.dataobject.ItemStockDo;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.service.ItemService;
import com.miaoshaoproject.service.PromoService;
import com.miaoshaoproject.service.model.ItemModel;
import com.miaoshaoproject.service.model.PromoModel;
import com.miaoshaoproject.validator.ValidationResult;
import com.miaoshaoproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired(required = false)
    ItemDoMapper itemDoMapper;
    @Autowired(required = false)
    ItemStockDoMapper itemStockDoMapper;

    @Autowired
    PromoService promoService;

    @Autowired
    private ValidatorImpl validator;
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

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int i = itemStockDoMapper.decreaseStock(itemId, amount);
        if (i > 0){
            return true;
        }
        return false;
    }

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
