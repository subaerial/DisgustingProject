package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class GetAllOrderResponse extends BaseResponse {


    List<GetAllOrder> data;


}
