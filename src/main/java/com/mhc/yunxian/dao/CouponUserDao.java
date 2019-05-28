package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.coupon.BuyerDetail;
import com.mhc.yunxian.bean.coupon.CanUseCouponDetail;
import com.mhc.yunxian.bean.coupon.MyGotCouponDetail;
import com.mhc.yunxian.dao.model.CouponUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CouponUserDao {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(CouponUser record);

    CouponUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CouponUser record);

    List<BuyerDetail> selectBuyerDetailByCouponId(Integer couponId);

    CouponUser selectByUidAndCouponId(Integer uid,Integer couponId);

    List<MyGotCouponDetail> myGotCouponList(Integer uid);

    List<CanUseCouponDetail> selectCanUseCoupon(Map map);

    int selectCanUseCouponNum(Map map);

    int getEffectCoupon(@Param("sellId") Integer Id, @Param("buyId") Integer id);

    List<CanUseCouponDetail> listEffectCoupon(@Param("sellId") Integer sellId, @Param("buyId") Integer buyId);

    List<MyGotCouponDetail> getAllCouponList(Integer uid);

    Integer countCouponUsedAmount(@Param("couponId") Integer couponId);
}