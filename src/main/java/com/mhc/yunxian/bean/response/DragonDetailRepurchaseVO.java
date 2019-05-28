package com.mhc.yunxian.bean.response;

import lombok.Data;

/**
 * Created by Administrator on 2018/12/15.
 */
@Data
public class DragonDetailRepurchaseVO{
	/**
	 * 用户昵称
	 */
	private String userName;
	/**
	 * 用户头像
	 */
	private String headPicture;
	/**
	 * 商品名称
	 */
	private String goodsName;
	/**
	 * 商品编号
	 */
	private String goodsNum;
	/**
	 * 地址
	 */
	private String address;
	/**
	 * 复购次数
	 */
	private Integer repurchaseCount;
}
