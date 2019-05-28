package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.OrderStatisticsListGoodsDetail;
import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.dao.model.OrderGoodsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface OrderGoodsService {


	List<OrderGoodsInfo> getOrderGoods(String orderNum);


	void add(OrderGoodsInfo info);

	boolean updateOrderGoods(OrderGoodsInfo Info);

	boolean updateById(OrderGoodsInfo Info);

	boolean updateOrderGoodsByOrderNum(OrderGoodsInfo Info);

	List<OrderStatisticsListGoodsDetail> selectOrderStatisticsListGoodsDetail(String orderNum);

	List<OrderGoodsInfo> selectLimitOrderGood(String openid, String dragonNum);

	int selectLimitNumber(String openid, String dragonNum);


	/**
	 * 根据商品编号查询商品订单列表()
	 * @param goodsNum
	 * @return
	 */
	List<String> queryBuyNumGtOneGoodsList(String goodsNum);

	/**
	 * 通过openid查询，以统计复购口碑数
	 * @param openid
	 * @return
	 */
	List<OrderGoodsInfo> selectOrderGoodsInfoByOpeId(@Param("openid") String openid);


}
