package com.miaoshaoproject.service.impl;

import com.miaoshaoproject.dao.OrderDOMapper;
import com.miaoshaoproject.dao.SequenceDOMapper;
import com.miaoshaoproject.dataobject.OrderDO;
import com.miaoshaoproject.dataobject.SequenceDO;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.service.ItemService;
import com.miaoshaoproject.service.OrderService;
import com.miaoshaoproject.service.UserService;
import com.miaoshaoproject.service.model.ItemModel;
import com.miaoshaoproject.service.model.OrderModel;
import com.miaoshaoproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    @Autowired(required = false)
    OrderDOMapper orderDOMapper;
    @Autowired(required = false)
    SequenceDOMapper sequenceDOMapper;



    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {

        //较验下单状态：用户是否存在、产品是否存在、购买数量是都合法
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIT,"用户id不存在！");
        }
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null){
            throw new BusinessException(EmBusinessError.PRODUCT_NOT_EXIT,"产品id有误！");
        }
        if (amount < 0 || amount > 99){
            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"购买数量有误！");
        }
        //较验活动信息
        if(promoId != null){
            //较验活动是否适用于该商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息有误");
            } else if (itemModel.getPromoModel().getStatus() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"未在活动时间范围内");
            }
        }

        //落单，减库存（落单就要锁库存）or支付减库存
        if (!itemService.decreaseStock(itemId, amount)){
          throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        if (promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setAmount(amount);
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易流水号
        orderModel.setId(this.generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //订单返回前端

        return orderModel;
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }

    //REQUIRES_NEW->意味着不管外层是否有事物，这个方法的调用都会重新开一个事物，外层事务成功与否都于该独立事务不相关
    //如果该独立事务commit了就代表该事务成功
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected String generateOrderNo(){
        //订单号16位
        StringBuilder stringBuilder = new StringBuilder();
        //前八位为时间信息，包括年月日
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(date);
        //中间六位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.selectByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String num = String.valueOf(sequence);
        for (int i = 0; i < 6-num.length(); i++) {
            stringBuilder.append("0");
        }
        stringBuilder.append(num);
        //最后两位是分库分表位,先写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
