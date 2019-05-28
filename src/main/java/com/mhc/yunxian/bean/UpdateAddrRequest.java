package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class UpdateAddrRequest extends BaseRequest {

    private String addrNum;
    private String userName;
    private String addr;
    private String phone;

    private String openid;
}
