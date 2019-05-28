/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao.model;

import java.io.Serializable;
import java.util.Date;

public class Shop implements Serializable {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 店铺ID  默认值：null
	 */
	private Long shopId;

	/**
	 * 店主openID  默认值：null
	 */
	private String shopkeeperOpenId;

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
	private Integer canWithdrawCash;

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

	private Date gmtCreate;

	private Date gmtModified;

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	/**
	 * 获取 店铺ID tbl_shop.shop_id
	 *
	 * @return 店铺ID
	 */
	public Long getShopId() {
		return shopId;
	}

	/**
	 * 设置 店铺ID tbl_shop.shop_id
	 *
	 * @param shopId 店铺ID
	 */
	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}

	public String getShopkeeperOpenId() {
		return shopkeeperOpenId;
	}

	public void setShopkeeperOpenId(String shopkeeperOpenId) {
		this.shopkeeperOpenId = shopkeeperOpenId;
	}

	/**
	 * 获取 店主昵称 tbl_shop.shopkeeper_nickname
	 *
	 * @return 店主昵称
	 */
	public String getShopkeeperNickname() {
		return shopkeeperNickname;
	}

	/**
	 * 设置 店主昵称 tbl_shop.shopkeeper_nickname
	 *
	 * @param shopkeeperNickname 店主昵称
	 */
	public void setShopkeeperNickname(String shopkeeperNickname) {
		this.shopkeeperNickname = shopkeeperNickname == null ? null : shopkeeperNickname.trim();
	}

	/**
	 * 获取 店铺名称 tbl_shop.shop_name
	 *
	 * @return 店铺名称
	 */
	public String getShopName() {
		return shopName;
	}

	/**
	 * 设置 店铺名称 tbl_shop.shop_name
	 *
	 * @param shopName 店铺名称
	 */
	public void setShopName(String shopName) {
		this.shopName = shopName == null ? null : shopName.trim();
	}

	/**
	 * 获取 店铺简介 tbl_shop.shop_intro
	 *
	 * @return 店铺简介
	 */
	public String getShopIntro() {
		return shopIntro;
	}

	/**
	 * 设置 店铺简介 tbl_shop.shop_intro
	 *
	 * @param shopIntro 店铺简介
	 */
	public void setShopIntro(String shopIntro) {
		this.shopIntro = shopIntro == null ? null : shopIntro.trim();
	}

	/**
	 * 获取 店铺头像 tbl_shop.shop_head_picture
	 *
	 * @return 店铺头像
	 */
	public String getShopHeadPicture() {
		return shopHeadPicture;
	}

	/**
	 * 设置 店铺头像 tbl_shop.shop_head_picture
	 *
	 * @param shopHeadPicture 店铺头像
	 */
	public void setShopHeadPicture(String shopHeadPicture) {
		this.shopHeadPicture = shopHeadPicture == null ? null : shopHeadPicture.trim();
	}

	/**
	 * 获取 店铺二维码 tbl_shop.shop_qr_code
	 *
	 * @return 店铺二维码
	 */
	public String getShopQrCode() {
		return shopQrCode;
	}

	/**
	 * 设置 店铺二维码 tbl_shop.shop_qr_code
	 *
	 * @param shopQrCode 店铺二维码
	 */
	public void setShopQrCode(String shopQrCode) {
		this.shopQrCode = shopQrCode == null ? null : shopQrCode.trim();
	}

	/**
	 * 获取 店铺已成交订单数(不包含子订单,退款订单,撤销订单) tbl_shop.shop_finished_order
	 *
	 * @return 店铺已成交订单数(不包含子订单, 退款订单, 撤销订单)
	 */
	public Long getShopFinishedOrder() {
		return shopFinishedOrder;
	}

	/**
	 * 设置 店铺已成交订单数(不包含子订单,退款订单,撤销订单) tbl_shop.shop_finished_order
	 *
	 * @param shopFinishedOrder 店铺已成交订单数(不包含子订单,退款订单,撤销订单)
	 */
	public void setShopFinishedOrder(Long shopFinishedOrder) {
		this.shopFinishedOrder = shopFinishedOrder;
	}

	/**
	 * 获取 复购订单数量 tbl_shop.repurchase_order_count
	 *
	 * @return 复购订单数量
	 */
	public Long getRepurchaseOrderCount() {
		return repurchaseOrderCount;
	}

	/**
	 * 设置 复购订单数量 tbl_shop.repurchase_order_count
	 *
	 * @param repurchaseOrderCount 复购订单数量
	 */
	public void setRepurchaseOrderCount(Long repurchaseOrderCount) {
		this.repurchaseOrderCount = repurchaseOrderCount;
	}

	/**
	 * 获取 关注人数 tbl_shop.followers_count
	 *
	 * @return 关注人数
	 */
	public Long getFollowersCount() {
		return followersCount;
	}

	/**
	 * 设置 关注人数 tbl_shop.followers_count
	 *
	 * @param followersCount 关注人数
	 */
	public void setFollowersCount(Long followersCount) {
		this.followersCount = followersCount;
	}

	/**
	 * 获取 经度 tbl_shop.longitude
	 *
	 * @return 经度
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * 设置 经度 tbl_shop.longitude
	 *
	 * @param longitude 经度
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude == null ? null : longitude.trim();
	}

	/**
	 * 获取 纬度 tbl_shop.latitude
	 *
	 * @return 纬度
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * 设置 纬度 tbl_shop.latitude
	 *
	 * @param latitude 纬度
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude == null ? null : latitude.trim();
	}

	/**
	 * 获取 显示地址 tbl_shop.show_addr
	 *
	 * @return 显示地址
	 */
	public String getShowAddr() {
		return showAddr;
	}

	/**
	 * 设置 显示地址 tbl_shop.show_addr
	 *
	 * @param showAddr 显示地址
	 */
	public void setShowAddr(String showAddr) {
		this.showAddr = showAddr == null ? null : showAddr.trim();
	}

	/**
	 * 获取 本月成交订单数量 tbl_shop.monthly_finished_order
	 *
	 * @return 本月成交订单数量
	 */
	public Long getMonthlyFinishedOrder() {
		return monthlyFinishedOrder;
	}

	/**
	 * 设置 本月成交订单数量 tbl_shop.monthly_finished_order
	 *
	 * @param monthlyFinishedOrder 本月成交订单数量
	 */
	public void setMonthlyFinishedOrder(Long monthlyFinishedOrder) {
		this.monthlyFinishedOrder = monthlyFinishedOrder;
	}

	/**
	 * 获取 本月销售额(分) tbl_shop.monthly_sales
	 *
	 * @return 本月销售额(分)
	 */
	public Integer getMonthlySales() {
		return monthlySales;
	}

	/**
	 * 设置 本月销售额(分) tbl_shop.monthly_sales
	 *
	 * @param monthlySales 本月销售额(分)
	 */
	public void setMonthlySales(Integer monthlySales) {
		this.monthlySales = monthlySales;
	}

	/**
	 * 获取 已提现金额 tbl_shop.already_withdraw_cash
	 *
	 * @return 已提现金额
	 */
	public Integer getAlreadyWithdrawCash() {
		return alreadyWithdrawCash;
	}

	/**
	 * 设置 已提现金额 tbl_shop.already_withdraw_cash
	 *
	 * @param alreadyWithdrawCash 已提现金额
	 */
	public void setAlreadyWithdrawCash(Integer alreadyWithdrawCash) {
		this.alreadyWithdrawCash = alreadyWithdrawCash;
	}

	/**
	 * 获取 店铺可提现金额 tbl_shop.can_withdraw_cash
	 *
	 * @return 店铺可提现金额
	 */
	public Integer getCanWithdrawCash() {
		return canWithdrawCash;
	}

	/**
	 * 设置 店铺可提现金额 tbl_shop.can_withdraw_cash
	 *
	 * @param canWithdrawCash 店铺可提现金额
	 */
	public void setCanWithdrawCash(Integer canWithdrawCash) {
		this.canWithdrawCash = canWithdrawCash;
	}

	/**
	 * 获取 今日成交订单数 tbl_shop.today_finished_order
	 *
	 * @return 今日成交订单数
	 */
	public Long getTodayFinishedOrder() {
		return todayFinishedOrder;
	}

	/**
	 * 设置 今日成交订单数 tbl_shop.today_finished_order
	 *
	 * @param todayFinishedOrder 今日成交订单数
	 */
	public void setTodayFinishedOrder(Long todayFinishedOrder) {
		this.todayFinishedOrder = todayFinishedOrder;
	}

	/**
	 * 获取 今日成交额 tbl_shop.turnover_today
	 *
	 * @return 今日成交额
	 */
	public Integer getTurnoverToday() {
		return turnoverToday;
	}

	/**
	 * 设置 今日成交额 tbl_shop.turnover_today
	 *
	 * @param turnoverToday 今日成交额
	 */
	public void setTurnoverToday(Integer turnoverToday) {
		this.turnoverToday = turnoverToday;
	}

	/**
	 * 获取 店铺状态 tbl_shop.status
	 *
	 * @return 店铺状态
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 设置 店铺状态 tbl_shop.status
	 *
	 * @param status 店铺状态
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 获取 逻辑删除 tbl_shop.is_deleted
	 *
	 * @return 逻辑删除
	 */
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * 设置 逻辑删除 tbl_shop.is_deleted
	 *
	 * @param isDeleted 逻辑删除
	 */
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	/**
	 * 获取 json扩展字段 tbl_shop.json_attribute
	 *
	 * @return json扩展字段
	 */
	public String getJsonAttribute() {
		return jsonAttribute;
	}

	/**
	 * 设置 json扩展字段 tbl_shop.json_attribute
	 *
	 * @param jsonAttribute json扩展字段
	 */
	public void setJsonAttribute(String jsonAttribute) {
		this.jsonAttribute = jsonAttribute == null ? null : jsonAttribute.trim();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" [");
		sb.append("Hash = ").append(hashCode());
		sb.append(", shopId=").append(shopId);
		sb.append(", shopkeeperOpenId=").append(shopkeeperOpenId);
		sb.append(", shopkeeperNickname=").append(shopkeeperNickname);
		sb.append(", shopName=").append(shopName);
		sb.append(", shopIntro=").append(shopIntro);
		sb.append(", shopHeadPicture=").append(shopHeadPicture);
		sb.append(", shopQrCode=").append(shopQrCode);
		sb.append(", shopFinishedOrder=").append(shopFinishedOrder);
		sb.append(", repurchaseOrderCount=").append(repurchaseOrderCount);
		sb.append(", followersCount=").append(followersCount);
		sb.append(", longitude=").append(longitude);
		sb.append(", latitude=").append(latitude);
		sb.append(", showAddr=").append(showAddr);
		sb.append(", monthlyFinishedOrder=").append(monthlyFinishedOrder);
		sb.append(", monthlySales=").append(monthlySales);
		sb.append(", alreadyWithdrawCash=").append(alreadyWithdrawCash);
		sb.append(", canWithdrawCash=").append(canWithdrawCash);
		sb.append(", todayFinishedOrder=").append(todayFinishedOrder);
		sb.append(", turnoverToday=").append(turnoverToday);
		sb.append(", status=").append(status);
		sb.append(", isDeleted=").append(isDeleted);
		sb.append(", jsonAttribute=").append(jsonAttribute);
		sb.append("]");
		return sb.toString();
	}
}