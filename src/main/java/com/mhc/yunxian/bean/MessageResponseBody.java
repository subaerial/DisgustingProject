package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
public class MessageResponseBody implements Serializable {
    /**
     * 服务通知响应码
     */
    private Integer errcode;
    /**
     * 服务通知响应消息
     */
    private String errmsg;
}
