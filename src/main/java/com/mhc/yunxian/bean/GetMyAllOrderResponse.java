package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class GetMyAllOrderResponse extends BaseResponse {

    private List<GetMyAllOrder> data;
}
