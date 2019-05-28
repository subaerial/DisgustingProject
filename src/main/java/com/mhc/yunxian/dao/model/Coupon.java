package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class Coupon {

    /**
     * 主键id
     */
    private Integer couponId;

    /**
     * 总数
     */
    private Integer totalAmount;

    /**
     * 已经领取数
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
     * 红包类型（0：直减全场 1：直减指定商品 2：折扣全场 3：折扣指定商品）
     */
    private Integer couponType;

    /**
     * 创建人id
     */
    private Integer userId;

    /**
     * 红包状态 （0可用 1下架）
     */
    private Integer state;

    /**
     * 商品列表 以逗号分割
     */
    private String goodsNum;

    /**
     * 修改时间
     */
    private Date gmtUpdateTime;

    /**
     * 用户限制类型0:无 1:新用户 2:老用户
     */
    private Integer userLimitState;

    /**
     * 用户限制购买数量
     */
    private Integer userLimitNum;

}