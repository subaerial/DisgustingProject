package com.mhc.yunxian.bean.coupon;

import com.mhc.yunxian.bean.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class CanOrderCouponListResponse extends BaseResponse {

    private int canOrderNum;

    private List<CanUseCouponDetail> canUseCouponDetailList;

}
