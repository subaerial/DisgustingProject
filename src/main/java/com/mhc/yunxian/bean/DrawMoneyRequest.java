package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class DrawMoneyRequest extends BaseRequest{

    private String sessionId;

    private Integer drawMoneyId;

}
