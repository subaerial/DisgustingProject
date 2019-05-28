package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseRequest;
import lombok.Data;

@Data
public class ManagerLoginRequest extends BaseRequest {


    private String username;
    private String password;
}
