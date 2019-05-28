package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMyUserOrderListRequest extends BaseRequest{

    private String sessionId;

    private String openid;

    private Integer page = 1;

    private Integer size = 20;
}
