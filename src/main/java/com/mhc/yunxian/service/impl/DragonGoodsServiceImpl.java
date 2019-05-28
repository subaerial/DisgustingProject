package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.DragonGoodsDao;
import com.mhc.yunxian.dao.model.DragonGoods;
import com.mhc.yunxian.service.DragonGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DragonGoodsServiceImpl implements DragonGoodsService {


    @Autowired
    DragonGoodsDao dragonGoodsDao;

    @Override
    public List<DragonGoods> getGoodsNUM(String dragonNum) {
        return dragonGoodsDao.select(dragonNum);
    }

    @Override
    public boolean del(String dragonNum,int number) {
        return dragonGoodsDao.delByDragonNum(dragonNum) == number;
    }

    @Override
    public boolean delDragonGoods(String goodsNum) {
        return dragonGoodsDao.delGoodsByGoodsNum(goodsNum) > 0;
    }

    @Override
    public DragonGoods getDragonGoodsByGoodsNumAndDragonNum(String goodsNum, String dragonNum) {
        return dragonGoodsDao.selectByGoodsNumAndDragonNum(dragonNum,goodsNum);
    }

    @Override
    public boolean updateDragonGoods(DragonGoods dragonGoods) {
        return dragonGoodsDao.updateByPrimaryKeySelective(dragonGoods) > 0;
    }

    @Override
    public DragonGoods findByGoodsNum(String goodsNum) {
        return dragonGoodsDao.findByGoodsNum(goodsNum);
    }

    @Override
    public List<DragonGoods> getByGoodsNum(String goodsNum) {
        return dragonGoodsDao.getAllByGoodsNum(goodsNum);
    }
}
