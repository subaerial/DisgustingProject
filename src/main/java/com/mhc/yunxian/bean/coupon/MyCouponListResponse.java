package com.mhc.yunxian.bean.coupon;

import com.mhc.yunxian.bean.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class MyCouponListResponse extends BaseResponse {

    private List couponDetails;


}
