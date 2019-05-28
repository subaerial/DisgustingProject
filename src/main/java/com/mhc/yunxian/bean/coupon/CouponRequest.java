package com.mhc.yunxian.bean.coupon;

import lombok.Data;

import java.util.Date;

@Data
public class CouponRequest {

    private String sessionId;
    /**
     * 券id
     */
    private Integer couponId;

    /**
     * 总数量
     */
    private Integer totalAmount;

    /**
     * 已领取数量
     */
    private Integer hasGotAmount;

    /**
     * 优惠金额
     */
    private Integer couponAmount;

    /**
     * 开始时间
     */
    private Date gmtStartTime;

    /**
     * 结束时间
     */
    private Date gmtEndTime;

    /**
     * 创建时间
     */
    private Date gmtCreateTime;

    /**
     * 券类型
     */
    private Integer couponType;

    private Integer userId;
    /**
     * 券状态
     */
    private Integer state;

    private String goodsNum;

    private Date gmtUpdateTime;

    /**
     * 使用用户类型
     */
    private Integer userLimitState;

    private Integer userLimitNum;
}
