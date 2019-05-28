package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class OrderStatisticsRequest extends BaseRequest {

    private String year;

    private String month;

    private String day;

    private String week;

    private String sessionId;

    private String openid;

}
