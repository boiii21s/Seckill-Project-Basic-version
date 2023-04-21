package com.miaoshaoproject.dao;

import com.miaoshaoproject.dataobject.ItemDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemDoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int insert(ItemDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int insertSelective(ItemDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    ItemDo selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int updateByPrimaryKeySelective(ItemDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbggenerated Fri Apr 14 12:39:32 CST 2023
     */
    int updateByPrimaryKey(ItemDo record);

    List<ItemDo> listItem();
    int increaseSales(@Param("itemId")Integer itemId,@Param("amount")Integer amount);
}