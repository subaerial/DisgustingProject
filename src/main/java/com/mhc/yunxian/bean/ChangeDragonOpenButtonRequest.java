package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class ChangeDragonOpenButtonRequest extends BaseRequest{

    private String sessionId;

    private Integer dragonButIsOpen;


}
