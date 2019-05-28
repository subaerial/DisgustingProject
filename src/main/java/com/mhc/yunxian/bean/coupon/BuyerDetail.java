package com.mhc.yunxian.bean.coupon;

import lombok.Data;

import java.util.Date;

@Data
public class BuyerDetail {
	/**
	 * 是否已使用
	 */
	Integer isUsed;
	/**
	 * 券用户关联id
	 */
	private Integer couponUserId;

	String nickName;

	String headImage;

	Date createTime;
	/**
	 * 券使用的接龙编号
	 */
	private String dragonNum;
	/**
	 * 券使用的接龙名称
	 */
	private String dragonName;


}
