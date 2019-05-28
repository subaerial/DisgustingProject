package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.coupon.MyGotCouponDetail;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service
 * @author: 昊天
 * @date: 2019/2/21 1:35 PM
 * @since V1.1.0-SNAPSHOT
 */
public interface CouponUserService {

    /**
     * 获取我领取的红包
     * @param buyerId
     * @return
     */
    List<MyGotCouponDetail> getMyCouponList(Integer buyerId);
}
