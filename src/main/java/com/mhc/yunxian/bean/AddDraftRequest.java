package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class AddDraftRequest extends BaseRequest{

    private String sessionId;

    private String dragonJson;


}
