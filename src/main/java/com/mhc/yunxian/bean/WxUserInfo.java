package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class WxUserInfo implements Serializable {

    private String openid;
    private String nickName;
    private String avatar;
    private String version;
    private int sex;
    private String language;
    private String city;
    private String provice;
    private String country;
    private String ip;
    private String source;
//    private String createTime;
//    private String updateTime;
}
