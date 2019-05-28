package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class DragonDraft {
    private Integer id;

    private String dragonJson;

    private Date createTime;

    private String openid;

    private Integer isDelete;

}