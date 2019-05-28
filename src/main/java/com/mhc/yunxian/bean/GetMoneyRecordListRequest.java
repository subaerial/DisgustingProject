package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetMoneyRecordListRequest extends BaseRequest {

    private String sessionId;

    private String openid;

}
