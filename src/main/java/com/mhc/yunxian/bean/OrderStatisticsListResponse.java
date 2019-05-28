package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class OrderStatisticsListResponse extends BaseResponse {

    private List<OrderStatisticsListDetail> orderList;

    private long total;

}
