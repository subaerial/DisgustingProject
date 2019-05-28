package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.param.ShopInfoRequestParam;
import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.dao.model.Shop;

import java.util.List;

/**
 * Created by Administrator on 2018/12/7.
 *
 * @author Alin
 */

public interface ShopService {

	Shop selectByPrimaryKey(Long shopId);

	Shop queryByShopId(Long shopId);
	/**
	 * 添加店铺信息
	 *
	 * @return
	 */
	Boolean addShopInfo(ShopInfoRequestParam param);

	/**
	 * 更新店铺信息
	 *
	 * @return
	 */
	Boolean updateShopInfo(ShopInfoRequestParam param);

	/**
	 * 查询店铺信息
	 *
	 * @return
	 */
	BaseResponse queryShopInfo(ShopInfoQuery query);

	/**
	 * 根据条件查询店铺信息
	 * @param query
	 * @return
	 */
	List<Shop> queryShop(ShopInfoQuery query);

	/**
	 * 统计店铺相关信息
	 *
	 * @return
	 */
	Shop statisticsShopInfo(ShopInfoQuery query);

	/**
	 * 统计店铺的复购口碑商品,并返回商品信息列表
	 */
	List<GoodsInfoResponse> countGoodsRepurchase(ShopInfoQuery query);

	BaseResponse dragonInProgress(ShopInfoQuery query);
}
