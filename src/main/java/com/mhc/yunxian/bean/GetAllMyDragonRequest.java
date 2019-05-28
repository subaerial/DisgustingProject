package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetAllMyDragonRequest extends BaseRequest {

    //private String openid;
    private String sessionId;

    private Integer dragonStatus;

    private Integer page = 1;

    private Integer size = 20;

}
