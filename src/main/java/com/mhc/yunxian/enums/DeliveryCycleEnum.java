package com.mhc.yunxian.enums;

/**
 *
 * @author Administrator
 * @date 2018/12/15
 */
public enum DeliveryCycleEnum {
	/**
	 * 发货周期类型
	 * 0 不是周期性发货
	 * 1.每天
	 * 2.每周
	 * 3.每月
	 */
	NOT_DELIVERY_CYCLE(0,"不是周期性发货"),
	EVERY_DAY(1,"每天"),
	EVERY_WEEK(2,"每周"),
	EVERY_MONTH(3,"每月"),
	;
	private Integer code;
	private String desc;

	DeliveryCycleEnum(Integer code, String desc) {
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
