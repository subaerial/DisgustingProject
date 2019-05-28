package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class HistoryGoodsListRequest extends BaseRequest {

    private String sessionId;

    private Integer page = 1;

    private Integer size = 20;

}
