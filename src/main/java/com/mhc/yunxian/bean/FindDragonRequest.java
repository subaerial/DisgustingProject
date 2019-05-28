package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class FindDragonRequest extends BaseRequest {

    private String dragonNum;//接龙号

    private String sessionId;//浏览者
}
