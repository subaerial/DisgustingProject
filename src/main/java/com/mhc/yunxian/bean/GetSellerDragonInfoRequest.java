package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetSellerDragonInfoRequest extends BaseRequest{

    private Integer dragonStatus = 0;

    private String openid;

    private String sessionId;

}
