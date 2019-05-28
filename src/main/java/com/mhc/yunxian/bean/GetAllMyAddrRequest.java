package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetAllMyAddrRequest extends BaseRequest {

    private String openid;
    private String sessionId;
}
