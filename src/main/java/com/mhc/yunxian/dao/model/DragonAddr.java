package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class DragonAddr {
    private Integer id;

    private String openid;

    private Date createTime;

    private String name;

    private String addr;

    private String longitude;

    private String latitude;

    private String detailAddr;

    private Boolean deleted;

}