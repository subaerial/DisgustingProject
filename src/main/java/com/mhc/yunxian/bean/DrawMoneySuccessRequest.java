package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class DrawMoneySuccessRequest extends BaseRequest {

    private String formId;
    private String openid;
    private Integer drawMoneyId;
    private Integer withdrawMoney;

}
