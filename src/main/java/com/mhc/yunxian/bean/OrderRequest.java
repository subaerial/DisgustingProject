package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * Created by Administrator on 2018/5/27.
 */
@Data
public class OrderRequest extends BaseRequest{
	private String sessionId;
	private Integer  orderStatus;
	private String orderNum;
}
