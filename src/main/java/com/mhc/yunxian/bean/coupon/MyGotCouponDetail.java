package com.mhc.yunxian.bean.coupon;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MyGotCouponDetail {

    /**
     * 券id
     */
    private Integer couponId;

    private int couponType;

    private int couponAmount;

    private Date gmtEndTime;

    private List<String> goodsNameList;

    private int state;

    private int isUsed;

    /**
     * 店主openId
     */
    private String sellerOpenid;

    private Date gmtStartTime;

    private String goodsNum;

    private String dragonNum;
    /**
     * 店铺Id
     */
    private Long shopId;
    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 红包已使用数量
     */
    private Integer alreadyUseNum;

    /**
     * 接龙列表
     */
    private List<String> dragonNumList;


}
