package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.Goods;
import com.mhc.yunxian.bean.request.param.GoodsRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.dao.model.GoodsInfo;

import java.util.List;
import java.util.Set;

public interface GoodsInfoService {


	GoodsInfo getGoods(String goodsNum);

	boolean updateGoods(GoodsInfo goodsInfo);

	boolean delGoods(String goodsNum);

	List<GoodsInfo> getGoodsListByUser(String openId);

	BaseResponse addGoods(GoodsRequestParam param);

	List<GoodsInfo> queryGoodsListByCreatorOpenId(String openid);

	List<GoodsInfoResponse> countGoodsRepurchase(ShopInfoQuery query);

	List<GoodsInfo> queryGoodsByGoodsNums(Set<String> set);

	/**
	 * 过滤已删除的商品
	 * @param list 未过滤的商品信息列表
	 * @return 已过滤的商品信息列表
	 */
	List<GoodsInfo> filterAreadyDeletedGoods(List<GoodsInfo> list);

	}
