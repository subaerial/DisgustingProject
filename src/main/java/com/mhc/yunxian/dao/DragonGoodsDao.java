package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.DragonGoods;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DragonGoodsDao {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(DragonGoods record);

    DragonGoods selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DragonGoods record);


    DragonGoods selectByGoodsNumAndDragonNum(String dragonNum,String goodsNum);

    List<DragonGoods> select(String dragonNum);

    int delByDragonNum(String dragonNum);

    int delGoodsByGoodsNum(String goodsNum);


    DragonGoods findByGoodsNum(@Param("goodsNum") String goodsNum);

    /**
     * 根据goodsNum查询
     * @param goodsNum
     * @return
     */
    List<DragonGoods> getAllByGoodsNum(String goodsNum);
}