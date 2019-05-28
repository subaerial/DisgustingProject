package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class OrderStatisticsListRequest extends BaseRequest {

    private String sessionId;

    private String startDate;

    private String endDate;

    private Integer OrderStatus;

    private String orderNum;

    private String nickName;

    private String openid;

}
