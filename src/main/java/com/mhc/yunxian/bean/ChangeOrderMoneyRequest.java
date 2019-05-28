package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class ChangeOrderMoneyRequest extends BaseRequest {

    private String sessionId;

    private String orderNum;

    private Integer state;

    private Integer money;

}
