package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMyBalanceRequest extends BaseRequest {

    //private String openid;
    private String orderNum;
    private String sessionId;
    private Integer orderStatus;
    private String formId;
    private Integer refundStatus;

}
