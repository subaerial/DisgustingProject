package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseRequest;
import lombok.Data;

@Data
public class ChageUserStatusRequest extends BaseRequest {

    private String openid;

    private String username;
}
