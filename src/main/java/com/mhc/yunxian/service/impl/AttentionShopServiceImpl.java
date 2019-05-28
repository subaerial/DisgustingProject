package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.index.DragonInfoVO;
import com.mhc.yunxian.bean.request.param.ShopInfoRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.AttentionShopVO;
import com.mhc.yunxian.bean.response.MyAttentionShopResponse;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.AttentionShopMapper;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.DragonStatusEnum;
import com.mhc.yunxian.enums.ShopEnum;
import com.mhc.yunxian.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/12/9.
 *
 * @author Alin
 */
@Slf4j
@Service
public class AttentionShopServiceImpl implements AttentionShopService {

	@Autowired
	private ShopMapper shopMapper;
	@Autowired
	private ShopService shopService;
	@Autowired
	private WxUserService wxUserService;
	@Autowired
	private DragonService dragonService;
	@Autowired
	private PartyInfoService partyInfoService;
	@Autowired
	private StatisticsService statisticsService;
	@Autowired
	private OrderInfoDao orderInfoDao;
	@Autowired
	private AttentionShopMapper attentionShopMapper;

	@Override
	public BaseResponse attentionShop(ShopInfoRequestParam param) {
		BaseResponse baseResponse = new BaseResponse();
		// 查询卖家店铺信息,填充参数
		Shop shop = shopMapper.selectByPrimaryKey(param.getShopId());
		ShopInfoQuery query = new ShopInfoQuery();
		query.setShopId(shop.getShopId());
		query.setShopkeeperOpenId(shop.getShopkeeperOpenId());
		query.setUserId(param.getUserId());
		List<AttentionShop> attentionShops = attentionShopMapper.queryAttentionShopList(query);
		// 为空时增加关注记录
		if (CollectionUtils.isEmpty(attentionShops)) {
			AttentionShop attentionShop = new AttentionShop();
			attentionShop.setStatus(ShopEnum.ALREADY_ATTENTION.getCode());
			attentionShop.setShopId(shop.getShopId());
			attentionShop.setGmtCreate(new Date());
			attentionShop.setShopHeadPicture(shop.getShopHeadPicture());
			attentionShop.setShopkeeperNickname(shop.getShopkeeperNickname());
			attentionShop.setShopkeeperOpenId(shop.getShopkeeperOpenId());
			attentionShop.setShopName(shop.getShopName());
			attentionShop.setUserId(param.getUserId());
			int i = attentionShopMapper.insertSelective(attentionShop);
			if (i > 0) {
				return baseResponse.build(RespStatus.OPERATION_SUCCEED);
			}
		} else {
			// 不为空时判断是否已经关注
			AttentionShop attentionShop = attentionShops.get(0);
			if (Integer.valueOf(ShopEnum.ALREADY_ATTENTION.getCode()).equals(attentionShop.getStatus())) {
				attentionShop.setStatus(ShopEnum.CANCEL_ATTENTION.getCode());
			} else {
				attentionShop.setStatus(ShopEnum.ALREADY_ATTENTION.getCode());
			}
			int i = attentionShopMapper.updateByPrimaryKeySelective(attentionShop);
			if (i > 0) {
				return baseResponse.build(RespStatus.OPERATION_SUCCEED);
			}
		}
		return baseResponse.build(RespStatus.OPERATION_FAILURE);
	}

	@Override
	public BaseResponse myAttentionShop(ShopInfoQuery query) {
		MyAttentionShopResponse response = new MyAttentionShopResponse();
		List<AttentionShop> attentionShops = attentionShopMapper.queryAttentionShopList(query);
		if (CollectionUtils.isEmpty(attentionShops)) {
			return response.build(RespStatus.SUCCESS);
		}
		List<AttentionShopVO> attentionShopVOList = new ArrayList<>();
		for (AttentionShop attentionShop : attentionShops) {
			AttentionShopVO attentionShopVO = new AttentionShopVO();
			// 根据店铺信息查询商家的进行中的接龙
			DragonInfo dragon = new DragonInfo();
			dragon.setOpenid(attentionShop.getShopkeeperOpenId());
			dragon.setDragonStatus(DragonStatusEnum.IN_PROGRESS.getStatus());
			List<DragonInfo> dragonInfos = dragonService.selectDragonByStatusAndOpenid(dragon);
			List<DragonInfoVO> dragonInfoVOList = new ArrayList<>();
			// 遍历正在进行的接龙列表,统计每个接龙的参与人数
			for (DragonInfo dragonInfo : dragonInfos) {
				List<PartInfo> partyUser = partyInfoService.getPartyUser(dragon.getDragonNum());
				DragonInfoVO dragonInfoVO = new DragonInfoVO();
				dragonInfoVO.setDragonImg(dragon.getDragonImg());
				dragonInfoVO.setDragonNum(dragon.getDragonNum());
				dragonInfoVO.setPartyNumber(partyUser.size());
				dragonInfoVO.setTitle(dragon.getDragonTitle());
				dragonInfoVOList.add(dragonInfoVO);
			}
			// 统计商家的复购口碑(复购订单数),正在进行的接龙数
			// 查询卖家发布的所有接龙生成的订单
			List<OrderInfo> orderInfos = orderInfoDao.selectOrderBySellerId(query.getShopkeeperOpenId());
			// 根据下单用户openID分组
			Map<String, List<OrderInfo>> stringListMap = orderInfos.stream().collect(Collectors.groupingBy(OrderInfo::getOpenid));
			// 复购订单= 总订单数-下单用户数
			Integer repurchaseOrder = Integer.valueOf(orderInfos.size() - stringListMap.size());
			attentionShopVO.setShopId(attentionShop.getShopId());
			attentionShopVO.setShopName(attentionShop.getShopName());
			attentionShopVO.setShopHeadPicture(attentionShop.getShopHeadPicture());
			attentionShopVO.setRepurchase(repurchaseOrder);
			attentionShopVO.setDragonInProcessCount(dragonInfoVOList.size());
			attentionShopVO.setDragonInfoVOList(dragonInfoVOList);
			attentionShopVO.setGmtCreate(attentionShop.getGmtCreate());
			attentionShopVOList.add(attentionShopVO);
		}
		// 关注店铺排序,关注时间逆序
		List<AttentionShopVO> collect = attentionShopVOList.stream().sorted(Comparator.comparing(AttentionShopVO::getGmtCreate).reversed())
				.collect(Collectors.toList());
		response.setAttentionShopVOS(collect);

		return response;
	}

	@Override
	public BaseResponse browseShop(ShopInfoQuery query) {

		BaseResponse baseResponse = new BaseResponse();
		Map<String, Object> hashMap = new HashMap<>();
		Shop shop = shopMapper.selectByPrimaryKey(query.getShopId());
		hashMap.put("shopInfo", shop);
		baseResponse.setMap(hashMap);
		return baseResponse;
	}

	@Override
	public List<AttentionShop> queryAttentionShopList(ShopInfoQuery query) {
		return attentionShopMapper.queryAttentionShopList(query);
	}

}
