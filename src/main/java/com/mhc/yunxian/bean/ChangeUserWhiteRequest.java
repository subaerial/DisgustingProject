package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class ChangeUserWhiteRequest extends BaseRequest {

    private String openid;

    private Integer isWhite;

}
