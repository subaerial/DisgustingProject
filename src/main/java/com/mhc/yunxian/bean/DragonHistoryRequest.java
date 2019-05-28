package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class DragonHistoryRequest extends BaseRequest{

    private Integer page = 1;

    private Integer size = 20;

    private String dragonNum;

}
