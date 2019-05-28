package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class DeliveryNoticeRequest extends BaseRequest {


    private String formId;

    private String orderNum;
}
