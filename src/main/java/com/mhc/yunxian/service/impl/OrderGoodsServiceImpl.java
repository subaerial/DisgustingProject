package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.OrderStatisticsListGoodsDetail;
import com.mhc.yunxian.dao.OrderGoodsDao;
import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.dao.model.OrderGoodsInfo;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.OrderGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class OrderGoodsServiceImpl implements OrderGoodsService {


    @Autowired
    OrderGoodsDao orderGoodsDao;

    @Override
    public List<OrderGoodsInfo> getOrderGoods(String orderNum) {
        return orderGoodsDao.select(orderNum);
    }

    @Override
    public void add(OrderGoodsInfo info) {
        try {
            orderGoodsDao.insertSelective(info);
        } catch (Exception e){
            e.printStackTrace();
            throw new DataException("订单商品关联信息添加失败");
        }


    }

    @Override
    public boolean updateOrderGoods(OrderGoodsInfo Info) {
        return orderGoodsDao.updateByGoodsNum(Info) > 0;
    }

    @Override
    public boolean updateById(OrderGoodsInfo Info) {
        return orderGoodsDao.updateById(Info) > 0;
    }

    @Override
    public boolean updateOrderGoodsByOrderNum(OrderGoodsInfo Info) {
        return orderGoodsDao.updateByOrderNum(Info) > 0;
    }

    @Override
    public List<OrderStatisticsListGoodsDetail> selectOrderStatisticsListGoodsDetail(String orderNum){
        return orderGoodsDao.selectOrderStatisticsListGoodsDetail(orderNum);
    }

    @Override
    public List<OrderGoodsInfo> selectLimitOrderGood(String openid, String dragonNum) {
        return orderGoodsDao.selectLimitOrderGood(openid,dragonNum);
    }

    @Override
    public int selectLimitNumber(String openid, String dragonNum) {
        return orderGoodsDao.selectLimitNumber(openid,dragonNum);
    }


    @Override
    public List<String> queryBuyNumGtOneGoodsList(String goodsNum) {
        return orderGoodsDao.queryBuyNumGtOneGoodsList(goodsNum);
    }

    @Override
    public List<OrderGoodsInfo> selectOrderGoodsInfoByOpeId(String openid) {
        return orderGoodsDao.selectOrderGoodsInfoByOpeId(openid);
    }

}
