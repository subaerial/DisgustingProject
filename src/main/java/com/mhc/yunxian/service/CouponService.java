package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.coupon.*;
import com.mhc.yunxian.dao.model.Coupon;

import java.util.List;
import java.util.Map;

/**
 * 优惠券服务
 *
 * @author
 */
public interface CouponService {

    /**
     * 获取红包
     */
    Coupon getCoupon(int couponId);

    /**
     * 添加红包
     */
    Boolean addCoupon(AddCouponRequest request);

    /**
     * 获取卖家红包列表
     */
    List<MyCouponDetail> myCouponList(Integer uid);

    /**
     * 获取买家红包列表
     */
    List<MyGotCouponDetail> myGotCouponList(Integer uid);

    List<MyGotCouponDetail> getAllCouponList(Integer uid);

    /**
     * 红包领取列表
     */
    List<BuyerDetail> getBuyerList(Integer couponId);

    /**
     * 买家是否已经领取过红包
     */
    Boolean isGotCoupon(Integer uid, Integer couponId);

    /**
     * 领取红包
     */
    Boolean receiveCoupon(Integer uid, Integer couponId);

    /**
     * 下单可使用红包
     */
    List<CanUseCouponDetail> selectCanUseCoupon(Map map);

    /**
     * 下单可使用红包数量
     */
    int selectCanUseCouponNum(Map map);

    BaseResponse getEffectCoupon(CanOrderCouponListRequest request);

    /**
     * 查询我创建的所有优惠券(含失效)
     *
     * @param userId
     * @return
     */
    List<MyCouponDetail> getAllMyCouponList(int userId);

    /**
     * 更新券
     *
     * @param coupon
     * @return
     */
    int update(Coupon coupon);

    List<String> inProcessDragon(String goodsNums);

}
