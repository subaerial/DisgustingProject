package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class MessageRequest implements java.io.Serializable{

    private String touser;
    private String template_id;
    private String page;
    private String form_id;
    private MessageKeyWordRequest data;
//    private String url;
//    private String appid;
//    private String pagepath;
}
