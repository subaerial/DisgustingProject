package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class WxUser {
    private Integer id;

    /**
     * 微信OpenId
     */
    private String openid;

    private String sessionId;
    /**
     * 外部登录号，如微信openid
     */
    private String outerTicket;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String headImgUrl;

    /**
     * 小程序版本
     */
    private String version;

    private Integer sex;

    private String language;

    private String city;

    private String province;

    private String country;

    private String unionid;

    private String ip;

    /**
     * 创建时间
     */
    private Date createTime;

    private Date updateTime;

    private Date expirationTime;

    private Integer userStatus;

    private Integer isWhite;

    private Integer dragonButIsOpen;

    private String phone;

    private Integer orderNumber;

    private String gzhOpenid;
}