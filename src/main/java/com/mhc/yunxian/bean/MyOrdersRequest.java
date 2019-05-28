package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * Created by Administrator on 2018/5/9.
 */
@Data
public class MyOrdersRequest extends BaseRequest{

	private String openId;
	private String dragonNum;
	private Integer orderStatus;

}
