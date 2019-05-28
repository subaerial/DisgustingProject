package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class DragonHistory {
	/**
	 * 用户openId
	 */
	private String openid;

	private String orderNum;

	private String nickName;

	private String headImg;

	private String dragonAddr;

	/**
	 * 复购次数 默认 0
	 */
	private Integer repurchaseCount = 0;

	/**
	 * 接龙编号
	 */

	private String dragonNum;
	private List<GetMyUserOrderGoods> goodsList;

}
