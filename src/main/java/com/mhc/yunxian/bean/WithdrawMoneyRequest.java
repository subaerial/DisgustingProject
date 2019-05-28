package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * Created by Administrator on 2018/5/29.
 */
@Data
public class WithdrawMoneyRequest extends BaseRequest{
	private String sessionId;

	private Integer money;

	private String formId;

	private Integer drawType;


}
