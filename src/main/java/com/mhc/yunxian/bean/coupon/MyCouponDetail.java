package com.mhc.yunxian.bean.coupon;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MyCouponDetail {

    private int couponType;

    private int couponAmount;

    /**
     * 总数量
     */
    private int totalAmount;

    /**
     * 被领取数
     */
    private int hasGotAmount;

    private Date gmtStartTime;

    private Date gmtEndTime;

    private Date gmtCreateTime;

    private List<String> goodsNameList;

    private String goodsNum;

    private int state;

    private int couponId;

    private Integer userLimitState;

    private Integer userLimitNum;

    /**
     * 已使用数
     */
    private int hasUseAmount;

    /**
     * 是否已经领取
     */
    private Integer isHasGot;

}
