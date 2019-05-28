package com.mhc.yunxian.bean.request.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author Administrator
 * @date 2018/12/7
 */
@Data
@ApiModel(value = "ShopInfoQuery", description = "店铺信息查询封装对象")
public class ShopInfoQuery extends BaseQuery {

	/**
	 * 店铺ID
	 */
	@ApiModelProperty(value = "shopId", notes = "店铺ID")
	private Long shopId;

	/**
	 * 店主UserId
	 */
	@ApiModelProperty(value = "shopkeeperUserId", notes = "店主shopkeeperUserId")
	private Long shopkeeperUserId;

	/**
	 * 店家接龙详情接龙编号
	 */
	@ApiModelProperty(value = "dragonNum", notes = "dragonNum")
	private String dragonNum;

	/**
	 * 店主openID
	 */
	@ApiModelProperty(value = "shopkeeperOpenId", notes = "店主openID")
	private String shopkeeperOpenId;

	/**
	 * 用户openID  用户关注店铺和浏览商家店铺时使用
	 */
	@ApiModelProperty(value = "userId", notes = "用户userId")
	private Long userId;

	/**
	 * 店主昵称
	 */
	@ApiModelProperty(value = "shopkeeperNickname", notes = "店主昵称")
	private String shopkeeperNickname;

	/**
	 * 店铺名称
	 */
	@ApiModelProperty(value = "shopName", notes = "店铺名称")
	private String shopName;

	/**
	 * 店铺简介
	 */
	@ApiModelProperty(value = "shopIntro", notes = "店铺简介")
	private String shopIntro;


	/**
	 * 复购订单数量
	 */
	@ApiModelProperty(value = "repurchaseOrderCount", notes = "复购订单数量")
	private Long repurchaseOrderCount;

	/**
	 * 关注人数
	 */
	@ApiModelProperty(value = "followersCount", notes = "关注人数")
	private Long followersCount;

	/**
	 * 显示地址
	 */
	@ApiModelProperty(value = "showAddr", notes = "显示地址")
	private String showAddr;


	/**
	 * 店铺状态
	 */
	@ApiModelProperty(value = "status", notes = "店铺状态")
	private Integer status;

	/**
	 * 逻辑删除
	 */
	@ApiModelProperty(value = "isDeleted", notes = "逻辑删除")
	private Boolean isDeleted;

	private Date gmtCreate;

	private Date gmtModified;
}
