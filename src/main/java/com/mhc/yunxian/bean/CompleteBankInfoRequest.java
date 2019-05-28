package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class CompleteBankInfoRequest extends BaseRequest {

    private String sessionId;

    private String accountNo;

    private String accountName;

    private Integer bankId;

    private Integer cityCode;

    private String openid;

    private String bankAb;


}
