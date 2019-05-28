package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetAllOrderRequest extends BaseRequest {



    private String sessionId;
    private Integer orderStatus;

    private String dragonNum;

    /**
     * 接口的类型，1代表从消息中心进入，
     */
    private Integer urlType;

    /**
     * 消息id
     */
    private Integer msgId;

    private Integer pageNo = 1;

    private Integer pageSize = 20;
}
