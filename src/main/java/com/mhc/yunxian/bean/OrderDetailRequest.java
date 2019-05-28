package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class OrderDetailRequest extends BaseRequest{

    private String sessionId;

    private String orderNum;

    private int state = 0;

    /**
     * 接口的类型，1代表从消息中心进入，
     */
    private Integer urlType;

    /**
     * 消息id
     */
    private Integer msgId;

}
