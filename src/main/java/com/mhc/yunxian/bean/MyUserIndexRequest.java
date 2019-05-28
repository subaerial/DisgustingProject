package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class MyUserIndexRequest extends BaseRequest{

    private String sessionId;

    private String year;

    private String month;

    private String week;

    private String day;

}
