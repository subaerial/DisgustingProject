package com.mhc.yunxian.enums;

/**
 * @author Administrator
 * @date 2018/12/27
 */
public enum CouponLimitStateEnum {
	/**
	 * 无限制(适用所有用户)
	 */
	ALL_USER(0, "无限制(适用所有用户)"),
	/**
	 * 新用户
	 */
	NEW_USER(1, "新用户"),
	/**
	 * 老用户
	 */
	OLD_USER(2, "老用户"),;
	private Integer state;
	private String desc;

	CouponLimitStateEnum(Integer state, String desc) {
		this.state = state;
		this.desc = desc;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
