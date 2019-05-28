package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class GZHMessageRequest implements Serializable {

    private String touser;
    private String template_id;
    private String url;
    private MiniProgram miniprogram;
    private MessageKeyWordRequest data;

}
