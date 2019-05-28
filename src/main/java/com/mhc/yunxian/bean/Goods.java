package com.mhc.yunxian.bean;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
public class Goods implements Serializable {

	/**
	 * 商品Id
	 */
	private Integer goodsId;
	private String goodsNum;
	private String goodsName;
	private Integer price;
	private Integer realPrice;
	private Integer totalNumber;
	private String goodsImgs;
	private Integer limitBuyNum;
	private Integer isBuyIt = 0;

	private String creatorOpenId;

	@Length(min = 1, max = 50, message = "规格长度需要在1-50字符")
	private String specification = "无";
	/**
	 * 商品再次购买跳转的接龙编号
	 */
	private String dragonNum;
	/**
	 * 逻辑删除
	 */
	private boolean deleted;

	/**
	 *
	 */
	private Integer buyNum;

}
