/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao.model;

import java.io.Serializable;
import java.util.Date;

public class AttentionShop implements Serializable {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 关注的店铺表ID  默认值：null
	 */
	private Long attentionShopId;

	/**
	 * 用户的openID  默认值：null
	 */
	private Long userId;

	/**
	 * 店铺的ID  默认值：null
	 */
	private Long shopId;

	/**
	 * 店铺的头像(暂与卖家头像一致)  默认值：null
	 */
	private String shopHeadPicture;

	/**
	 * 店铺的名称(暂与用户名称一致)  默认值：null
	 */
	private String shopName;

	/**
	 * 店主昵称  默认值：null
	 */
	private String shopkeeperNickname;

	/**
	 * 店主openID  默认值：null
	 */
	private String shopkeeperOpenId;

	/**
	 * 店主openID  默认值：null
	 */
	private Integer status;

	/**
	 * 创建时间  默认值：null
	 */
	private Date gmtCreate;

	/**
	 * 修改时间  默认值：null
	 */
	private Date gmtModified;

	/**
	 * 获取 关注的店铺表ID tbl_attention_shop.attention_shop_id
	 *
	 * @return 关注的店铺表ID
	 */
	public Long getAttentionShopId() {
		return attentionShopId;
	}

	/**
	 * 设置 关注的店铺表ID tbl_attention_shop.attention_shop_id
	 *
	 * @param attentionShopId 关注的店铺表ID
	 */
	public void setAttentionShopId(Long attentionShopId) {
		this.attentionShopId = attentionShopId;
	}

	/**
	 * 获取 用户的openID tbl_attention_shop.user_open_id
	 *
	 * @return 用户的openID
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * 设置 用户的openID tbl_attention_shop.user_open_id
	 *
	 * @param userId 用户的openID
	 */
	public void setUserId(Long userId) {
		this.userId = userId ;
	}

	/**
	 * 获取 店铺的ID tbl_attention_shop.shop_id
	 *
	 * @return 店铺的ID
	 */
	public Long getShopId() {
		return shopId;
	}

	/**
	 * 设置 店铺的ID tbl_attention_shop.shop_id
	 *
	 * @param shopId 店铺的ID
	 */
	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}

	/**
	 * 获取 店铺的头像(暂与卖家头像一致) tbl_attention_shop.shop_head_picture
	 *
	 * @return 店铺的头像(暂与卖家头像一致)
	 */
	public String getShopHeadPicture() {
		return shopHeadPicture;
	}

	/**
	 * 设置 店铺的头像(暂与卖家头像一致) tbl_attention_shop.shop_head_picture
	 *
	 * @param shopHeadPicture 店铺的头像(暂与卖家头像一致)
	 */
	public void setShopHeadPicture(String shopHeadPicture) {
		this.shopHeadPicture = shopHeadPicture == null ? null : shopHeadPicture.trim();
	}

	/**
	 * 获取 店铺的名称(暂与用户名称一致) tbl_attention_shop.shop_name
	 *
	 * @return 店铺的名称(暂与用户名称一致)
	 */
	public String getShopName() {
		return shopName;
	}

	/**
	 * 设置 店铺的名称(暂与用户名称一致) tbl_attention_shop.shop_name
	 *
	 * @param shopName 店铺的名称(暂与用户名称一致)
	 */
	public void setShopName(String shopName) {
		this.shopName = shopName == null ? null : shopName.trim();
	}

	/**
	 * 获取 店主昵称 tbl_attention_shop.shopkeeper_nickname
	 *
	 * @return 店主昵称
	 */
	public String getShopkeeperNickname() {
		return shopkeeperNickname;
	}

	/**
	 * 设置 店主昵称 tbl_attention_shop.shopkeeper_nickname
	 *
	 * @param shopkeeperNickname 店主昵称
	 */
	public void setShopkeeperNickname(String shopkeeperNickname) {
		this.shopkeeperNickname = shopkeeperNickname == null ? null : shopkeeperNickname.trim();
	}

	/**
	 * 获取 店主openID tbl_attention_shop.shopkeeper_open_id
	 *
	 * @return 店主openID
	 */
	public String getShopkeeperOpenId() {
		return shopkeeperOpenId;
	}

	/**
	 * 设置 店主openID tbl_attention_shop.shopkeeper_open_id
	 *
	 * @param shopkeeperOpenId 店主openID
	 */
	public void setShopkeeperOpenId(String shopkeeperOpenId) {
		this.shopkeeperOpenId = shopkeeperOpenId == null ? null : shopkeeperOpenId.trim();
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 获取 创建时间 tbl_attention_shop.gmt_create
	 *
	 * @return 创建时间
	 */
	public Date getGmtCreate() {
		return gmtCreate;
	}

	/**
	 * 设置 创建时间 tbl_attention_shop.gmt_create
	 *
	 * @param gmtCreate 创建时间
	 */
	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	/**
	 * 获取 修改时间 tbl_attention_shop.gmt_modified
	 *
	 * @return 修改时间
	 */
	public Date getGmtModified() {
		return gmtModified;
	}

	/**
	 * 设置 修改时间 tbl_attention_shop.gmt_modified
	 *
	 * @param gmtModified 修改时间
	 */
	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(" [");
		sb.append("Hash = ").append(hashCode());
		sb.append(", attentionShopId=").append(attentionShopId);
		sb.append(", userId=").append(userId);
		sb.append(", shopId=").append(shopId);
		sb.append(", shopHeadPicture=").append(shopHeadPicture);
		sb.append(", shopName=").append(shopName);
		sb.append(", shopkeeperNickname=").append(shopkeeperNickname);
		sb.append(", shopkeeperOpenId=").append(shopkeeperOpenId);
		sb.append(", gmtCreate=").append(gmtCreate);
		sb.append(", gmtModified=").append(gmtModified);
		sb.append("]");
		return sb.toString();
	}
}