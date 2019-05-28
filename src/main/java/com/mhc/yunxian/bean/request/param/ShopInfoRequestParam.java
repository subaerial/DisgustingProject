package com.mhc.yunxian.bean.request.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2018/12/7.
 *
 * @author Alin
 */
@Data
@ApiModel(value = "ShopInfoRequestParam", description = "添加店铺信息")
public class ShopInfoRequestParam extends BaseRequestParam {

	/**
	 * 店铺ID
	 */
	private Long shopId;

	/**
	 * 店主openID
	 */
	private String shopkeeperOpenId;
	/**
	 * 店主UserId
	 */
	private Long shopkeeperUserId;

	/**
	 * 用户userId
	 */
	@ApiModelProperty(value = "userId", notes = "用户userId")
	private Long userId;
	/**
	 * 店主昵称
	 */
	private String shopkeeperNickname;

	/**
	 * 店铺名称
	 */
	private String shopName;

	/**
	 * 店铺简介
	 */
	private String shopIntro;

	/**
	 * 店铺头像
	 */
	private String shopHeadPicture;

	/**
	 * 店铺二维码
	 */
	private String shopQrCode;

	/**
	 * 店铺已成交订单数(不包含子订单,退款订单,撤销订单)
	 */
	private Long shopFinishedOrder;

	/**
	 * 复购订单数量
	 */
	private Long repurchaseOrderCount;

	/**
	 * 关注人数
	 */
	private Long followersCount;

	/**
	 * 经度
	 */
	private String longitude;

	/**
	 * 纬度
	 */
	private String latitude;

	/**
	 * 显示地址
	 */
	private String showAddr;

	/**
	 * 本月成交订单数量
	 */
	private Long monthlyFinishedOrder;

	/**
	 * 本月销售额(分)
	 */
	private Integer monthlySales;

	/**
	 * 已提现金额
	 */
	private Integer alreadyWithdrawCash;

	/**
	 * 店铺可提现金额
	 */
	private String canWithdrawCash;

	/**
	 * 今日成交订单数
	 */
	private Long todayFinishedOrder;

	/**
	 * 今日成交额
	 */
	private Integer turnoverToday;

	/**
	 * 店铺状态
	 */
	private Integer status;

	/**
	 * 逻辑删除
	 */
	private Boolean isDeleted;

	/**
	 * json扩展字段
	 */
	private String jsonAttribute;
}
