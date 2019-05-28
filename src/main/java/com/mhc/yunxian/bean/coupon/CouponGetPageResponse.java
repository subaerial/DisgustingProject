package com.mhc.yunxian.bean.coupon;

import com.mhc.yunxian.bean.BaseResponse;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CouponGetPageResponse extends BaseResponse {

    private String sellerName;

    private String sellerHeadImage;

    private int couponAmount;

    private int couponType;

    private int totalAmount;

    private int hasGotAmount;

    private Date gmtStartTime;

    private Date gmtEndTime;

    private List<BuyerDetail> buyerDetailList;

    private int isSeller;

    private int isGot;

    private Long totalGotTime;

    private String openId;

    private String dragonNum;

    private List<String> goodsNameList;

    private Integer userLimitState;

    private Integer userLimitNum;

    private Integer orderNum;

    /**
     * 已使用数
     */
    private Integer alreadyUseNum;

    private Long shopId;
    /**
     * 券状态 0 可用 -1 不可用
     */
    private Integer state;

    /**
     * 接龙列表
     */
    private List<String> dragonNumList;

}
