package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class ReceiveDragonResponse extends BaseResponse {


    private String name;
    private String addr;
    private String phone;
    private String orderNum;

    private String[] goodsNumList;


}
