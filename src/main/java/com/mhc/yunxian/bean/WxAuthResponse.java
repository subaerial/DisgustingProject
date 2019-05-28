package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class WxAuthResponse extends  BaseResponse{


    private String openid;
    private String sessionKey;
    private String unionid;
    private String sessionId;



}
