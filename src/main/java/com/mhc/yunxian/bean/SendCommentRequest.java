package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class SendCommentRequest extends BaseRequest{

    private String sessionId;

    private String openid;

    private String dragon_num;

    private String comment;
}
