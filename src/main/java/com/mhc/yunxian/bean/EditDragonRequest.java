package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class EditDragonRequest extends BaseRequest {


    private String dragonNum;

    private int ignoreExpired ;
}
