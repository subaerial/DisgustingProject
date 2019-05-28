package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseRequest;
import lombok.Data;

@Data
public class OrderDetailsRequest extends BaseRequest {

    private String buyerName;

    private String buyerPhone;

    private String sellerName;

    private String sellerPhone;

    private Integer status;

    private String startDate;

    private String endDate;
}
