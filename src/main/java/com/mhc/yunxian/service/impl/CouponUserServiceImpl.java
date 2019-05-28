package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.coupon.MyGotCouponDetail;
import com.mhc.yunxian.dao.CouponUserDao;
import com.mhc.yunxian.service.CouponUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service.impl
 * @author: 昊天
 * @date: 2019/2/21 1:38 PM
 * @since V1.1.0-SNAPSHOT
 */
@Service
public class CouponUserServiceImpl implements CouponUserService {

    @Autowired
    private CouponUserDao couponUserDao;

    @Override
    public List<MyGotCouponDetail> getMyCouponList(Integer buyerId) {
        return couponUserDao.myGotCouponList(buyerId);
    }
}
