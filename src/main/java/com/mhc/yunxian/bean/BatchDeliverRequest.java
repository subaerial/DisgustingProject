package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class BatchDeliverRequest extends BaseRequest{

    private String sessionId;

    private String dragonNum;

}
