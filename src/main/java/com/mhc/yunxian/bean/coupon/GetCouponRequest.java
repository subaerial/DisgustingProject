package com.mhc.yunxian.bean.coupon;

import lombok.Data;

@Data
public class GetCouponRequest {

    private String sessionId;

    private Integer couponId;

}
