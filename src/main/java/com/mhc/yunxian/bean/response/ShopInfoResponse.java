package com.mhc.yunxian.bean.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by Administrator on 2018/12/7.
 * @author Alin
 */
@Data
@ApiModel(value = "ShopInfoResponse",description = "店铺信息响应体")
public class ShopInfoResponse {
	/**
	 * 店铺ID  默认值：null
	 */
	private Long shopId;

	/**
	 * 店主openID  默认值：null
	 */
	private Long shopkeeperOpenId;

	/**
	 * 店主昵称  默认值：null
	 */
	private String shopkeeperNickname;

	/**
	 * 店铺名称  默认值：null
	 */
	private String shopName;

	/**
	 * 店铺简介  默认值：null
	 */
	private String shopIntro;

	/**
	 * 店铺头像  默认值：null
	 */
	private String shopHeadPicture;

	/**
	 * 店铺二维码  默认值：null
	 */
	private String shopQrCode;

	/**
	 * 店铺已成交订单数(不包含子订单,退款订单,撤销订单)  默认值：null
	 */
	private Long shopFinishedOrder;

	/**
	 * 复购订单数量  默认值：null
	 */
	private Long repurchaseOrderCount;

	/**
	 * 关注人数  默认值：null
	 */
	private Long followersCount;

	/**
	 * 经度  默认值：null
	 */
	private String longitude;

	/**
	 * 纬度  默认值：null
	 */
	private String latitude;

	/**
	 * 显示地址  默认值：null
	 */
	private String showAddr;

	/**
	 * 本月成交订单数量  默认值：null
	 */
	private Long monthlyFinishedOrder;

	/**
	 * 本月销售额(分)  默认值：null
	 */
	private Integer monthlySales;

	/**
	 * 已提现金额  默认值：null
	 */
	private Integer alreadyWithdrawCash;

	/**
	 * 店铺可提现金额  默认值：null
	 */
	private String canWithdrawCash;

	/**
	 * 今日成交订单数  默认值：null
	 */
	private Long todayFinishedOrder;

	/**
	 * 今日成交额  默认值：null
	 */
	private Integer turnoverToday;

	/**
	 * 店铺状态  默认值：null
	 */
	private Integer status;

	/**
	 * 逻辑删除  默认值：null
	 */
	private Boolean isDeleted;

	/**
	 * json扩展字段  默认值：null
	 */
	private String jsonAttribute;
}
