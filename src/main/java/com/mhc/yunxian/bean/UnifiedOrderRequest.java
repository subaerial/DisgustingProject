package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * Created by Administrator on 2018/5/22.
 */
@Data
public class UnifiedOrderRequest extends BaseRequest {

	//private String openid;
	private String sessionId;
	//商品名称
	private String goodsName;
	//接龙编号
	private String dragonNum;
	//支付价格(分)
	private Integer paymentPrice;
	//备注信息
	private String remark;
	//订单编号
	private String orderNum;

	private String formId;



	//private String buySessionId;//买家sessionId





}
