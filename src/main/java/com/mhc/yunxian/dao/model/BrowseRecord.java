package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class BrowseRecord {
    private Integer id;

    private Integer userId;

    private String dragonNum;

    private Date createTime;

    private String sellerOpenid;

}