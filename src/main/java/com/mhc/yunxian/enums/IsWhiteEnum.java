package com.mhc.yunxian.enums;

/**
 * Created by Administrator on 2018/12/17.
 */
public enum IsWhiteEnum {
	/**
	 * 不是白名单用户
	 */
	IS_NOT_WHITE(0, "不是白名单用户"),
	/**
	 * 是白名单用户
	 */
	IS_WHITE(1, "是白名单用户"),;

	private Integer code;
	private String desc;

	IsWhiteEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
