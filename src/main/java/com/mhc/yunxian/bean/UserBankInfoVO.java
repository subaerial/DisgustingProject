package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class UserBankInfoVO {

    private String accountNo;

    private String accountName;

    private String bankName;

    private String province;

    private String city;

    private Integer limit;

    private Integer usedLimit;

}
