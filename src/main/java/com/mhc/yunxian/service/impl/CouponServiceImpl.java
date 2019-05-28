package com.mhc.yunxian.service.impl;

import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.coupon.*;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.CouponDao;
import com.mhc.yunxian.dao.CouponUserDao;
import com.mhc.yunxian.dao.GoodsInfoDao;
import com.mhc.yunxian.dao.WxUserDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.DragonStatusEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.CouponService;
import com.mhc.yunxian.service.DragonGoodsService;
import com.mhc.yunxian.service.DragonService;
import com.mhc.yunxian.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mmdzm
 */
@Service
@Slf4j
public class CouponServiceImpl implements CouponService {

    @Autowired
    private WxUserDao wxUserDao;

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private CouponUserDao couponUserDao;

    @Autowired
    private GoodsInfoDao goodsInfoDao;

    @Autowired
    private ShopService shopService;

    @Autowired
    private DragonGoodsService dragonGoodsService;

    @Autowired
    private DragonService dragonService;

    @Autowired
    RemoteCache redisCache;
    @Value("coupon.key")
    private String couponKey;

    @Override
    public Coupon getCoupon(int couponId) {
        return couponDao.selectByPrimaryKey(couponId);
    }

    @Override
    public Boolean addCoupon(AddCouponRequest request) {
        Coupon coupon = new Coupon();

        WxUser wxUser = wxUserDao.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return false;
        }

        coupon.setTotalAmount(request.getTotalAmount());
        coupon.setHasGotAmount(0);
        coupon.setCouponAmount(request.getCouponAmount());
        coupon.setGmtStartTime(request.getStartTime());
        coupon.setGmtEndTime(request.getEndTime());
        coupon.setGmtCreateTime(new Date());
        coupon.setCouponType(request.getCouponType());
        coupon.setUserId(wxUser.getId());
        coupon.setState(0);
        coupon.setUserLimitState(request.getUserLimitState());
        coupon.setUserLimitNum(request.getUserLimitNum());

        if (!CollectionUtils.isEmpty(request.getGoodsNumList())) {
            StringBuilder goodsNums = new StringBuilder();

            int index = 0;
            for (String goodsNum : request.getGoodsNumList()) {
                if (index != 0) {
                    goodsNums.append(",");
                }
                goodsNums.append(goodsNum);
                index++;
            }

            coupon.setGoodsNum(goodsNums.toString());
        }


        if (couponDao.insertSelective(coupon) < 1 && coupon.getCouponId() == null) {
            log.error("红包生成失败,红包信息:{}", coupon.toString());
            return false;
        }

        redisCache.set(couponKey + coupon.getCouponId(), String.valueOf(request.getTotalAmount()));
        redisCache.expireAt(couponKey + coupon.getCouponId(), request.getEndTime());

        return true;
    }

    @Override
    public List<MyCouponDetail> myCouponList(Integer uid) {

        List<MyCouponDetail> result = couponDao.getMyCouponList(uid);

        for (MyCouponDetail detail : result) {
            if (!StringUtils.isBlank(detail.getGoodsNum())) {

                List<String> goodsNameList = new ArrayList<>();

                List<String> goodsNumList = Arrays.asList(detail.getGoodsNum().split(","));

                for (String goodsNum : goodsNumList) {
                    GoodsInfo goodsInfo = goodsInfoDao.selectGoods(goodsNum);

                    if (goodsInfo != null) {
                        goodsNameList.add(goodsInfo.getGoodsName());
                    }
                }

                detail.setGoodsNameList(goodsNameList);
            }
        }

        return result;
    }

    /**
     * 买家我的红包页面，返回可用红包
     *
     * @param uid
     * @return
     */
    @Override
    public List<MyGotCouponDetail> myGotCouponList(Integer uid) {
        List<MyGotCouponDetail> list = couponUserDao.myGotCouponList(uid);
        Iterator<MyGotCouponDetail> iterator = list.iterator();
        while (iterator.hasNext()) {
            MyGotCouponDetail myGotCouponDetail = iterator.next();
            if (myGotCouponDetail.getIsUsed() == 0
                    && myGotCouponDetail.getState() == 0
                    && myGotCouponDetail.getGmtEndTime().getTime() > System.currentTimeMillis()) {
                continue;
            } else {
                iterator.remove();
            }
        }
        return list;
    }

    /**
     * 买家我的红包页面，返回所有红包
     *
     * @param uid
     * @return
     */
    @Override
    public List<MyGotCouponDetail> getAllCouponList(Integer uid) {
        List<MyGotCouponDetail> list = couponUserDao.getAllCouponList(uid);
        return list;
    }

    @Override
    public List<BuyerDetail> getBuyerList(Integer couponId) {
        return couponUserDao.selectBuyerDetailByCouponId(couponId);
    }

    @Override
    public Boolean isGotCoupon(Integer uid, Integer couponId) {
        if (couponUserDao.selectByUidAndCouponId(uid, couponId) != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public Boolean receiveCoupon(Integer uid, Integer couponId) {
        CouponUser couponUser = new CouponUser();

        Coupon coupon = couponDao.selectByPrimaryKey(couponId);

        if (coupon == null) {
            log.error("红包为空:couponId:" + couponId);
            return false;
        }

        couponUser.setCouponId(couponId);
        couponUser.setUserId(uid);
        couponUser.setGmtCreateTime(new Date());

        if (couponUserDao.insertSelective(couponUser) < 1) {
            log.error("红包联系表插入失败:{}", couponUser);
            throw new DataException("红包联系表插入失败");
        }

        List<BuyerDetail> buyerDetails = couponUserDao.selectBuyerDetailByCouponId(couponId);

        coupon.setHasGotAmount(buyerDetails.size());
        coupon.setGmtUpdateTime(new Date());

        if (couponDao.updateByPrimaryKeySelective(coupon) < 1) {
            return false;
        }

        return true;
    }

    @Override
    public List<CanUseCouponDetail> selectCanUseCoupon(Map map) {
        return couponUserDao.selectCanUseCoupon(map);
    }

    @Override
    public int selectCanUseCouponNum(Map map) {
        return couponUserDao.selectCanUseCouponNum(map);
    }

    @Override
    public BaseResponse getEffectCoupon(CanOrderCouponListRequest request) {
        CanOrderCouponListResponse response = new CanOrderCouponListResponse();
        //获取用户信息
        WxUser wxUser = wxUserDao.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return response.build(RespStatus.USER_NOT_EXIST);
        }
        //获取商家id
        WxUser sell = wxUserDao.selectByOpenid(request.getOpenid());
        // Shop shop = shopService.queryByShopId(sell.get)
        ShopInfoQuery query = new ShopInfoQuery();
        query.setShopkeeperOpenId(sell.getOpenid());
        Shop shop = shopService.statisticsShopInfo(query);

        if (sell == null) {
            return response.build(RespStatus.USER_NOT_EXIST);
        }
        //查询红包列表
        List<CanUseCouponDetail> list = listEffectCoupon(sell.getId(), wxUser.getId());
        List<CanUseCouponDetail> resultList = new ArrayList<>();
        List<CanUseCouponDetail> finalResult = new ArrayList<>();

        for (CanUseCouponDetail e : list) {

            //判断红包生效期
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis < e.getGmtStartTime().getTime()
                    || currentTimeMillis > e.getGmtEndTime().getTime()) {
                continue;
            }
            e.setShop(shop);
            List<String> couponGoodsNumList = e.getGoodsNumList();

            if (couponGoodsNumList != null && couponGoodsNumList.size() > 0 && isHasGoods(couponGoodsNumList, request.getGoodsNumList())) {
                List<String> goodsNameList = new ArrayList<>();
                for (String goodsNum : couponGoodsNumList) {
                    GoodsInfo goodsInfo = goodsInfoDao.selectGoods(goodsNum);
                    if (goodsInfo != null) {
                        goodsNameList.add(goodsInfo.getGoodsName());
                    }
                    DragonGoods byGoodsNum = dragonGoodsService.findByGoodsNum(goodsNum);
                    if (byGoodsNum != null) {
                        resultList.add(e);
                    }
                }
                e.setGoodsNameList(goodsNameList);

            } else if (couponGoodsNumList == null) {
                resultList.add(e);
            }

        }
        for (CanUseCouponDetail couponDetail :
                resultList) {
            if (!finalResult.contains(couponDetail)) {
                finalResult.add(couponDetail);
            }
        }
        List<CanUseCouponDetail> collect = finalResult.stream().sorted(Comparator.comparing(CanUseCouponDetail::getGmtCreateTime).reversed()).collect(Collectors.toList());

        response.setCanOrderNum(collect.size());
        response.setCanUseCouponDetailList(collect);
        return response;
    }

    @Override
    public List<MyCouponDetail> getAllMyCouponList(int userId) {
        return couponDao.getAllMyCouponList(userId);
    }

    @Override
    public int update(Coupon coupon) {
        return couponDao.updateByPrimaryKeySelective(coupon);
    }

    /**
     * 领取红包时获取红包可使用的接龙列表
     *
     * @param goodsNumsList
     * @return
     */
    @Override
    public List<String> inProcessDragon(String goodsNumsList) {

        List<String> goodsNums = Arrays.asList(goodsNumsList.split(","));
        List<String> dragonNums = Lists.newArrayList();
        List<String> dragonNumsAllGoods = Lists.newArrayList();

        for (String goodsNum : goodsNums) {
            List<DragonGoods> dragonGoodsList = dragonGoodsService.getByGoodsNum(goodsNum);
            if (!CollectionUtils.isEmpty(dragonGoodsList)) {
                dragonGoodsList.forEach(dragonGoods -> {
                    DragonInfo dragon = dragonService.getDragon(dragonGoods.getDragonNum());
                    if (dragon != null) {

                        if (!dragonNums.contains(dragonGoods.getDragonNum())
                                && DragonStatusEnum.IN_PROGRESS.getStatus().equals(dragon.getDragonStatus())) {
                            dragonNums.add(dragonGoods.getDragonNum());
                        }

                        List<DragonGoods> goodsNUM = dragonGoodsService.getGoodsNUM(dragonGoods.getDragonNum());
                        List<String> dragonGoodsNum = Lists.newArrayList();
                        if (goodsNUM != null) {
                            goodsNUM.forEach(dragonGoods1 -> {
                                dragonGoodsNum.add(dragonGoods1.getGoodsNum());
                            });

                            Boolean isLarge = Boolean.FALSE;
                            if (goodsNums.size() > dragonGoodsNum.size()) {
                                isLarge = goodsNums.containsAll(dragonGoodsNum);
                            } else {
                                isLarge = dragonGoodsNum.containsAll(goodsNums);
                            }

                            if (isLarge && !dragonNumsAllGoods.contains(dragonGoods.getDragonNum())
                                    && DragonStatusEnum.IN_PROGRESS.getStatus().equals(dragon.getDragonStatus())) {
                                dragonNumsAllGoods.add(dragonGoods.getDragonNum());
                            }

                        }
                    }
                });
            }
        }

        if (dragonNumsAllGoods != null) {
            return dragonNumsAllGoods;
        } else {
            return dragonNums;
        }


    }

    //    boolean isHasGoods(String goodsNums,List<String> goodsNumList){
    boolean isHasGoods(List<String> couponGoodsNumList, List<String> goodsNumList) {
//        List<String> couponGoodsNumList = Arrays.asList(goodsNums.split(","));

        for (String a : couponGoodsNumList) {
            for (String b : goodsNumList) {
                if (a.equals(b)) {
                    return true;
                }
            }
        }
        return false;
    }


    List<CanUseCouponDetail> listEffectCoupon(Integer sellId, Integer buyId) {
        List<CanUseCouponDetail> list = couponUserDao.listEffectCoupon(sellId, buyId);
        for (CanUseCouponDetail c : list) {
            String goodsNumString = c.getGoodsNum();
            if (goodsNumString != null && goodsNumString.length() > 0) {
                c.setGoodsNumList(Arrays.asList(c.getGoodsNum().split(",")));
            }
        }
        return list;
    }

}