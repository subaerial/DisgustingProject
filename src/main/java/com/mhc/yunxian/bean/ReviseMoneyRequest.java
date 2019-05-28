package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class ReviseMoneyRequest extends BaseRequest {
    private String orderNum;
    private Integer money;
}
