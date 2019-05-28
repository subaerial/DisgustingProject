package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.BaseRequest;
import lombok.Data;

@Data
public class UpdateOrderRequest extends BaseRequest {

    private String orderNum;

    private Integer orderStatus;//要改为什么状态

    private String username;

    private Integer isSendMsg = 0;

}
