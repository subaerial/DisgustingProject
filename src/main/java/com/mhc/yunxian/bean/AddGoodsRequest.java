package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class AddGoodsRequest extends BaseRequest {

    //private String openid;
    private String sessionId;
    private String dragonNum;



}
