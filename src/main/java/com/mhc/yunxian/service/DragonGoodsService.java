package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.DragonGoods;

import java.util.List;

public interface DragonGoodsService {



    List<DragonGoods> getGoodsNUM(String dragonNum);

    boolean del(String dragonNum,int number);

    boolean delDragonGoods(String goodsNum);

    DragonGoods getDragonGoodsByGoodsNumAndDragonNum(String goodsNum, String dragonNum);

    boolean updateDragonGoods(DragonGoods dragonGoods);

    DragonGoods findByGoodsNum(String goodsNum);

    List<DragonGoods> getByGoodsNum(String goodsNum);
}
