package com.mhc.yunxian.bean.coupon;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CouponGetPageRequest {

    private String sessionId;

    @NotNull
    private Integer couponId;

    private int page = 1;

    private int pageSize = 40;

}
