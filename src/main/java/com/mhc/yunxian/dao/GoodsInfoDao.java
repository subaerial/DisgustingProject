package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.vo.CountGoodsVO;
import com.mhc.yunxian.vo.CountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface GoodsInfoDao {


	int insertGoods(GoodsInfo goodsInfo);


	GoodsInfo selectGoods(String goodsNum);

	int updateGoods(GoodsInfo goodsInfo);

	/**
	 * 主键更新
	 * @param goodsInfo
	 * @return
	 */
	int updateByGoodsId(GoodsInfo goodsInfo);
	int deleteGoods(String goodsNum);

	List<GoodsInfo> getGoodsListByUser(String openId);

	List<GoodsInfo> queryGoodsListByCreatorOpenId(@Param("openId") String openId);

	/**
	 * 根据商品编号列表查询商品列表信息
	 * @param set
	 * @return
	 */
	List<GoodsInfo> queryGoodsByGoodsNums(@Param("set") Set<String> set);

	CountGoodsVO countBy();

	/**
	 * 查询所有商品信息
	 */
	List<GoodsInfo> queryAll();

	/**
	 * 根据id查询
	 * @param goodsId
	 * @return
	 */
	GoodsInfo selectById(Integer goodsId);
}