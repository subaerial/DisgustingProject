package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMyAllOrderRequest extends BaseRequest {

    private String sessionId;

    private Integer orderStatus;

    private Integer page = 1;

    private Integer size = 20;


    //private String openid;

}
