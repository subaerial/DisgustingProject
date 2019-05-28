package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class WxUnion {
    private Integer id;

    /**
     * 微信公众平台关联号
     */
    private String unionId;

    /**
     * 微信公众号openId
     */
    private String wxOpenId;

    private Date createTime;

    /**
     * 是否订阅公众号（0：未订阅；1：订阅）
     */
    private Byte isSub;
}