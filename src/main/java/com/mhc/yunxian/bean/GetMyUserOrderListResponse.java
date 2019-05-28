package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class GetMyUserOrderListResponse extends BaseResponse{

    private List<GetMyUserOrder> data;

}
