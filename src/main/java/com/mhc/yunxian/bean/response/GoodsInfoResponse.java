package com.mhc.yunxian.bean.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2018/12/11.
 *
 * @author Alin
 */
@Data
@ApiModel
public class GoodsInfoResponse {

	/**
	 * 商品Id
	 */
	private Integer id;

	/**
	 * 商品编号
	 */
	private String goodsNum;

	/**
	 * 商品名称
	 */
	private String goodsName;

	/**
	 * 商品价格
	 */
	private int price;

	/**
	 * 商品总数量
	 */
	private Integer totalNumber;

	/**
	 * 商品图片
	 */
	private String goodsImg;

	/**
	 * 购买数量,查询订单返回时使用,插入数据可忽略
	 */
	private Integer buyNumber;

	/**
	 * 购买重量
	 */
	private Float buyWeight;

	/**
	 * 限制购买数量
	 */
	private int limitBuyNum;

	/**
	 * 销量
	 */
	private int salesVolume;


	/**
	 * 创建人(卖家)openID
	 */
	private String creatorOpenId;

	/**
	 * 复购口碑
	 */
	private Integer repurchase;
	/**
	 * 商品上架的接龙编号列表
	 */
	private List<String> dragonNums;
}
