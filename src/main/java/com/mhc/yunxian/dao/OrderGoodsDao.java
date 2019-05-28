package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.GetMyUserOrderGoods;
import com.mhc.yunxian.bean.OrderStatisticsListGoodsDetail;
import com.mhc.yunxian.dao.model.OrderGoodsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface OrderGoodsDao {
//    int deleteByPrimaryKey(Integer id);
//
//    int insert(OrderGoodsInfo record);
//
//
//
//    OrderGoodsInfo selectByPrimaryKey(Integer id);
//
//    int updateByPrimaryKeySelective(OrderGoodsInfo record);
//
//    int updateByPrimaryKey(OrderGoodsInfo record);


    List<OrderGoodsInfo> select(String orderNum);

    List<OrderGoodsInfo> selectLimitOrderGood(String openid, String dragonNum);

    int insertSelective(OrderGoodsInfo record);

    int updateByGoodsNum(OrderGoodsInfo orderGoodsInfo);

    int updateByOrderNum(OrderGoodsInfo orderGoodsInfo);

    int updateById(OrderGoodsInfo Info);

    List<OrderStatisticsListGoodsDetail> selectOrderStatisticsListGoodsDetail(String orderNum);

    List<GetMyUserOrderGoods> selectMyUserOrderGoods(String orderNum);

    int selectLimitNumber(String openid, String dragonNum);

    /**
     * 根据订单编号集和查询订单商品列表
     *
     * @param list
     * @return
     */
    List<OrderGoodsInfo> queryByOrderNums(@Param(value = "list") List<String> list);

    /**
     * 根据商品编号列表查询商品列表集合
     */
    List<OrderGoodsInfo> queryGoodsByGoodsNums(@Param("set") Set<String> set);

    /**
     * @param goodsNum
     * @return
     */
    List<String> queryBuyNumGtOneGoodsList(@Param("goodsNum") String goodsNum);

    // 通过openid查询订单商品
    List<OrderGoodsInfo> selectOrderGoodsInfoByOpeId(@Param("openid") String openid);

    List<OrderGoodsInfo> selectOrderGoodsByGoodsNum(@Param("goodsNum") String goodsNum);
}