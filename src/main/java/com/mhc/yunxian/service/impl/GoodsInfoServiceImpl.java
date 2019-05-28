package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.Goods;
import com.mhc.yunxian.bean.request.param.GoodsRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.GoodsInfoDao;
import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.enums.IsDeleteEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.GoodsInfoService;
import com.mhc.yunxian.service.StatisticsService;
import com.mhc.yunxian.utils.KeyTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsInfoServiceImpl implements GoodsInfoService {


	@Autowired
	GoodsInfoDao goodsInfoDao;

	@Autowired
	private StatisticsService statisticsService;


	@Override
	public GoodsInfo getGoods(String goodsNum) {
		return goodsInfoDao.selectGoods(goodsNum);
	}

	@Override
	public boolean updateGoods(GoodsInfo goodsInfo) {
		return goodsInfoDao.updateGoods(goodsInfo) > 0;
	}

	@Override
	public boolean delGoods(String goodsNum) {
		if (goodsInfoDao.deleteGoods(goodsNum) < 1) {
			return false;
		}
		return true;
	}

	@Override
	public List<GoodsInfo> getGoodsListByUser(String openId) {
		return goodsInfoDao.getGoodsListByUser(openId);
	}

	@Override
	@Transactional(rollbackFor = {Exception.class})
	public BaseResponse addGoods(GoodsRequestParam param) {
		BaseResponse baseResponse = new BaseResponse();
		try {
			for (Goods goods : param.getGoods()) {
				GoodsInfo goodsInfo = new GoodsInfo();
				goodsInfo.setGoodsName(goods.getGoodsName());
				goodsInfo.setGoodsImg(goods.getGoodsImgs());
				goodsInfo.setPrice(goods.getPrice());
				goodsInfo.setTotalNumber(goods.getTotalNumber());
				goodsInfo.setSpecification(goods.getSpecification());
				goodsInfo.setGoodsNum(KeyTool.createOrderNo());
				goodsInfo.setCreateTime(new Date());
				goodsInfo.setCreatorOpenId(goods.getCreatorOpenId());
				//插入商品信息表
				if (goodsInfoDao.insertGoods(goodsInfo) < 1) {
					throw new DataException("插入商品信息失败");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return baseResponse.build(RespStatus.DB_INSERT_FAILED);
		}
		return baseResponse.build(RespStatus.SUCCESS);
	}

	@Override
	public List<GoodsInfo> queryGoodsListByCreatorOpenId(String openid) {
		return goodsInfoDao.queryGoodsListByCreatorOpenId(openid);
	}

	@Override
	public List<GoodsInfoResponse> countGoodsRepurchase(ShopInfoQuery query) {
		return statisticsService.countGoodsRepurchase(query);
	}

	@Override
	public List<GoodsInfo> queryGoodsByGoodsNums(Set<String> set) {

		return goodsInfoDao.queryGoodsByGoodsNums(set);
	}

	@Override
	public List<GoodsInfo> filterAreadyDeletedGoods(List<GoodsInfo> list) {
		// 过滤已删除的商品
		return list.stream().filter(goodsInfo -> {
			Boolean deleted = goodsInfo.getDeleted();
			int flag = 0;
			if (deleted) {
				flag = 1;
			}
			boolean equals = IsDeleteEnum.IS_NOT_DELETE.getCode().equals(Optional.ofNullable(flag).orElse(0));
			return equals;
		}).collect(Collectors.toList());
	}
}
