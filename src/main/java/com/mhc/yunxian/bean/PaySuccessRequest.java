package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class PaySuccessRequest extends  BaseRequest {


    private String orderNum;

    private Integer money;//付款金额

    private String mark;//订单备注

    private String sellSessionId;//卖家sessionid

    private String buySessionId;//买家sessionId

}
