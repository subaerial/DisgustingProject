package com.mhc.yunxian.enums;

/**
 * Created by Administrator on 2018/12/25.
 * @author
 * @date
 */
public enum DragonDeliveryEnum {
	/**
	 * 0 自提
	 */
	PICK_UP_By_SELF(0,"自提"),
	/**
	 * 1 快递
	 */
	EXPRESS_DELIVERY(1,"快递"),

	/**
	 * 2自提+快递
	 */
	PICK_UP_AND_EXPRESS(2,"自提+快递"),
	/**
	 * 3 送货上门
	 */
	HOME_DELIVERY_SERVICE(3,"送货上门服务"),
	/**
	 * 4 自提+送货上门
	 */
	PICK_UP_By_SELF_AND_HOME_DELIVERY_SERVICE(4,"自提+送货上门"),
	/**
	 * 5 快递+送货上门
	 */
	EXPRESS_DELIVERY_AND_HOME_DELIVERY_SERVICE(5,"快递+送货上门"),
	/**
	 * 6 自提+快递+送货上门
	 */
	PICK_UP_AND_EXPRESS_HOME_DELIVERY_SERVICE(6,"自提+快递+送货上门")
;
	private Integer code;
	private String msg;

	DragonDeliveryEnum(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
