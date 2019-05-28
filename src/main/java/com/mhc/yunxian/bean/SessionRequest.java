package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class SessionRequest extends BaseRequest {

    private String sessionId;

    private String openid;

}
