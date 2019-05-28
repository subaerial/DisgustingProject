package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class AdminChangePwdRequest extends BaseRequest{

    private String username;

    private String oldPwd;

    private String newPwd;

}
