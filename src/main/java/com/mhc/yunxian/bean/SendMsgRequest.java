package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class SendMsgRequest extends BaseRequest{

    private String title;
    private String dragonNum;
    private String addr;

    private String orderNum;




}
