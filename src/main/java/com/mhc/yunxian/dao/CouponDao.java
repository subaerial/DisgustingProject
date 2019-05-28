package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.coupon.MyCouponDetail;
import com.mhc.yunxian.dao.model.Coupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponDao {
	int deleteByPrimaryKey(Integer couponId);

	int insertSelective(Coupon record);

	Coupon selectByPrimaryKey(Integer couponId);

	int updateByPrimaryKeySelective(Coupon record);

	List<MyCouponDetail> getMyCouponList(int uid);

	List<MyCouponDetail> getAllMyCouponList(@Param("userId") int userId);
}