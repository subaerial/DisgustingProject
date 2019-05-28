package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class TransfersReponse {

    private Boolean result;

    private String msg;

    private String paymentNo;

    private String paymentTime;

}
