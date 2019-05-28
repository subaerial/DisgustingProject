package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMyUserRequest extends BaseRequest {

    private String openid;
    private String sessionId;

    private String nickName;

    private String year;

    private String month;

    private String day;

    private String week;

    private int state = 0;

    private Integer page = 1;

    private Integer size = 20;

    private Integer sort = 0;
}
