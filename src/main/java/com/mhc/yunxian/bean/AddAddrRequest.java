package com.mhc.yunxian.bean;

import lombok.Data;



@Data
public class AddAddrRequest extends BaseRequest {

    private String openid;
    private String sessionId;
    private String addrNum;
    private String userName;
    private String phone;
    private String addr;
    private Integer isDefault;
}
