package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private Integer id;

    private String openid;

    /**
     * 订单编号
     */
    private String dragonNum;

    /**
     * 评论内容
     */
    private String comment;

    private Date updateTime;

    private Date createTime;
}