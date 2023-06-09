package com.miaoshaoproject.dao;

import com.miaoshaoproject.dataobject.ItemStockDo;
import org.apache.ibatis.annotations.Param;

public interface ItemStockDoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int insert(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int insertSelective(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    ItemStockDo selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int updateByPrimaryKeySelective(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int updateByPrimaryKey(ItemStockDo record);

    ItemStockDo selectByItemId(Integer id);
    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
}