package com.mhc.yunxian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.DeliveryCycle;
import com.mhc.yunxian.bean.FindDragon;
import com.mhc.yunxian.bean.ShopDragonInfo;
import com.mhc.yunxian.bean.index.DragonInfoVO;
import com.mhc.yunxian.bean.index.GetIndexDragonListResponse;
import com.mhc.yunxian.bean.request.param.ShopInfoRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.BrowsedDragonResponse;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.WxUserDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.DragonDateTypeEnum;
import com.mhc.yunxian.enums.DragonStatusEnum;
import com.mhc.yunxian.service.*;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2018/12/7.
 *
 * @author Alin
 */
@Slf4j
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private DragonService dragonService;
    @Autowired
    private OrderInfoDao orderInfoDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    private WxUserDao wxUserDao;
    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private StatisticsService statisticsService;

    @Override
    public Shop selectByPrimaryKey(Long shopId) {
        return shopMapper.selectByPrimaryKey(shopId);
    }

    @Override
    public Shop queryByShopId(Long shopId) {

        return shopMapper.selectByPrimaryKey(shopId);
    }

    @Override
    public Boolean addShopInfo(ShopInfoRequestParam param) {
        // 添加前校验是否已经存在
        Shop shop = new Shop();
        BeanUtils.copyProperties(param, shop);
        int i = shopMapper.insertSelective(shop);
        if (i > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean updateShopInfo(ShopInfoRequestParam param) {
        Shop shop = new Shop();
        BeanUtils.copyProperties(param, shop);
        int i = shopMapper.updateByPrimaryKeySelective(shop);
        if (i > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public BaseResponse queryShopInfo(ShopInfoQuery query) {
        BaseResponse response = new BaseResponse();
        List<Shop> shops = shopMapper.queryShopInfo(query);
        Map<String, Object> map = new HashMap<>();
        map.put("shopList", shops);
        response.setMap(map);
        return response;
    }

    @Override
    public List<Shop> queryShop(ShopInfoQuery query) {
        return shopMapper.queryShopInfo(query);
    }

    @Override
    public Shop statisticsShopInfo(ShopInfoQuery query) {
        return statisticsService.countShopSalesAmountInfo(query);
    }

    /**
     * 返回店铺的商品列表(包含复购次数)
     * 商品复购次数=包含该商品的总订单数-购买用户的数量
     *
     * @param query
     * @return
     */
    @Override
    public List<GoodsInfoResponse> countGoodsRepurchase(ShopInfoQuery query) {
        // 根据shopId查询店铺信息
        Shop shop = shopService.queryByShopId(query.getShopId());
        query.setShopkeeperOpenId(shop.getShopkeeperOpenId());
        return statisticsService.countGoodsRepurchase(query);
    }

    /**
     * 我的店铺页面,进行中的接龙
     *
     * @param query
     * @return
     */
    @Override
    public BaseResponse dragonInProgress(ShopInfoQuery query) {
        BrowsedDragonResponse dragonResponse = new BrowsedDragonResponse();
        Shop shopInfo = shopMapper.selectByPrimaryKey(query.getShopId());
        DragonInfo dragon = new DragonInfo();
        dragon.setOpenid(shopInfo.getShopkeeperOpenId());
        dragon.setDragonStatus(DragonStatusEnum.IN_PROGRESS.getStatus());
        List<DragonInfo> dragonInfos = dragonService.selectDragonByStatusAndOpenid(dragon);
        // 查询复购订单数 需要优化
        Integer count = statisticsService.countDragonRepurchase(query);
        //List<ShopDragonInfo> shopDragonInfoList = new ArrayList<>();
        //ShopDragonInfo shopDragonInfo = new ShopDragonInfo();
        List<DragonInfoVO> dragonInfoVOList = new ArrayList<>();
        for (DragonInfo dragonInfo : dragonInfos) {
            DragonInfoVO dragonInfoVO = new DragonInfoVO();
            List<DragonAddr> dragonAddrList = dragonService.selectByDragonNum(dragonInfo.getDragonNum());
            if (1 > dragonAddrList.size() && 1 != dragonInfo.getIsDelivery() && CollectionUtils.isNotEmpty(dragonAddrList)) {
                List<String> addrs = Arrays.asList(dragonInfo.getAddr().split("&"));
                for (String addr : addrs) {
                    DragonAddr dragonAddr = new DragonAddr();
                    dragonAddr.setAddr(addr);
                    dragonAddrList.add(dragonAddr);
                }

            }
            dragonInfoVO.setDragonAddrs(dragonAddrList);
            dragonInfoVO.setIsDelivery(dragonInfo.getIsDelivery());
            dragonInfoVO.setCreateTime(dragonInfo.getCreateTime());
            dragonInfoVO.setDragonImg(dragonInfo.getDragonImg());
            dragonInfoVO.setDragonNum(dragonInfo.getDragonNum());
            //如果不是周期性发货
            dragonInfoVO.setSendTime(dragonInfo.getSendTime());
            dragonInfoVO.setCutOffTime(dragonInfo.getEndTime());
            //周期性发货
            String deliveryCycle = dragonInfo.getDeliveryCycle();
            if (StringUtils.isNotBlank(deliveryCycle)) {
                // 下一期截单时间
                Date cutOffDate = dragonService.getDragonDeliveryCycleDate(deliveryCycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME);
                if (cutOffDate != null) {
                    dragonInfoVO.setCutOffTime(DateUtil.SDF.format(cutOffDate));
                }
                // 下一期发货时间
                Date deliveryDate = dragonService.getDragonDeliveryCycleDate(deliveryCycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME);
                if (deliveryDate != null) {
                    dragonInfoVO.setSendTime(DateUtil.SDF.format(deliveryDate));
                }
            }

            //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			/*Date date = null;
			try {
				date = simpleDateFormat.parse(dragonInfo.getEndTime());
			} catch (ParseException e) {
				log.error("接龙详情结束时间有误:" + e);
				return dragonResponse.build(RespStatus.SYSTEM_ERROR);
			}*/
            dragonInfoVO.setEndTime(dragonInfo.getEndTime());
            dragonInfoVO.setTitle(dragonInfo.getDragonTitle());
            dragonInfoVO.setRemark(dragonInfo.getDragonDesc());
            // List<OrderInfo> orderInfos = orderInfoDao.selectOrderBySellerIdIndex(dragonInfo.getDragonNum());
            List<String> headImgs = new ArrayList<>();
            /*int flag = 0;
            int partyNumber = 0;
            for (OrderInfo orderInfo : orderInfos) {
                if (orderInfo.getParentOrderNum() != null) {
                    continue;
                }
                if (flag <= 10) {
                    WxUser buyer = wxUserDao.selectByOpenid(orderInfo.getOpenid());
                    if (buyer == null) {
                        return dragonResponse.build(RespStatus.USER_NOT_EXIST);
                    }
                    headImgs.add(buyer.getHeadImgUrl());
                    flag++;
                }
                partyNumber++;
            }*/

            List<FindDragon> partDragonUser = dragonService.getPartDragonUser(dragonInfo.getDragonNum(), null);
            for (FindDragon findDragon : partDragonUser) {
                headImgs.add(findDragon.getHeadImg());
            }

            dragonInfoVO.setSendTime(dragonInfoVO.getSendTime().substring(0, dragonInfoVO.getSendTime().lastIndexOf(":")));
            if(StringUtils.isNotBlank(dragonInfoVO.getCutOffTime())){
                //把时间截取到分
                dragonInfoVO.setCutOffTime(dragonInfoVO.getCutOffTime().substring(0,dragonInfoVO.getCutOffTime().lastIndexOf(":")));
            }
            dragonInfoVO.setPartyNumber(partDragonUser.size());
            dragonInfoVO.setHeadImg(headImgs);
            dragonInfoVO.setOrderNumber(orderService.countOrderNumByDragon(dragonInfo.getDragonNum()));
            dragonInfoVOList.add(dragonInfoVO);
            dragonResponse.setDragonInfoVOList(dragonInfoVOList);

        }
        // 获取店铺信息展示
        dragonResponse.setDragonInfoVOListSize(dragonInfoVOList.size());
        dragonResponse.setShopId(shopInfo.getShopId());
        dragonResponse.setShopName(shopInfo.getShopName());
        dragonResponse.setShopkeeperOpenId(shopInfo.getShopkeeperOpenId());
        dragonResponse.setShopHeadPicture(shopInfo.getShopHeadPicture());
        dragonResponse.setRepurchaseCount(count);
        return dragonResponse;
    }
}
