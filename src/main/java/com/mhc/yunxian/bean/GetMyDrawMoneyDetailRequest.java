package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMyDrawMoneyDetailRequest extends BaseRequest {

    private String openid;
    private String sessionId;
}
