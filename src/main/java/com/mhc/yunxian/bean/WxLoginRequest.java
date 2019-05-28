package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.Date;

@Data
public class WxLoginRequest extends BaseRequest {

    private String openid;
    private String sessionId;

    private String nickName;
    private String avatarUrl;
    private Integer gender;
    private String city;
    private String province;
    private String country;
    private String language;
    //private String source;

    private String version;
    private String clientip;

    private Integer userStatus;

    private String phone;

    private Date updateTime;


}
