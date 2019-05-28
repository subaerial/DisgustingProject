package com.mhc.yunxian.bean.coupon;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class MyCouponListRequest {

    @NotBlank
    private String sessionId;

    private int page = 1;

    private int pageSize = 20;

    private int couponType = 0;

    /**
     * 卖家优惠券的状态 -1 已失效 0 进行中
     */
    private Integer sellerCouponStatus;
    /**
     * 买家优惠券的状态 -1 已失效 0 进行中(可用) 1 已使用
     */
    private Integer couponStatus;

}
