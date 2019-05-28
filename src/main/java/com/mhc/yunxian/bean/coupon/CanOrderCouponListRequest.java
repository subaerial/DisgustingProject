package com.mhc.yunxian.bean.coupon;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

@Data
public class CanOrderCouponListRequest {

    private int page = 1;

    private int pageSize = 20;

    @NotBlank
    private String sessionId;

    @NotBlank
    private String openid;

    private List<String> goodsNumList;

}
