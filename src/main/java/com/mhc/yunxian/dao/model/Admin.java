package com.mhc.yunxian.dao.model;


import lombok.Data;

import java.util.Date;

@Data
public class Admin {

    private Integer id;

    private String username;

    private String password;

    private Date updateTime;

    private Date createTime;

}
