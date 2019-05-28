package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailsResponse extends BaseResponse {


    private List<OrderDetails> data;

    private long total = 0;

}
