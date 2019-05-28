package com.mhc.yunxian.bean.coupon;

import com.mhc.yunxian.dao.model.Shop;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CanUseCouponDetail {

    private Integer couponType;

    private Integer couponAmount;

    private Date gmtEndTime;

    private List<String> goodsNameList;

    private String goodsNum;

    private List<String> goodsNumList;

    private Integer state;

    private Integer isUsed;

    private Integer couponUserId;

    /**
     * 券id
     */
    private Integer couponId;

    private Date gmtStartTime;

    private Date gmtCreateTime;

    /**
     * 店铺信息
     */
    private Shop shop;

}
