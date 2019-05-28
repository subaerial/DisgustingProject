package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 *
 * @author Administrator
 * @date 2018/12/26
 */
@Getter
public enum CouponTypeEnum {
	/**
	 * 0金额（全场通用）
	 */
	MONEY_OVER_ALL(0,"金额类型,全场通用"),
	/**
	 * 1金额（指定商品）
	 */
	MONEY_ASSIGN_GOODS(1,"金额类型,指定商品"),
	/**
	 * 2 折扣（全场通用）
	 */
	DISCOUNT_OVER_ALL(2,"折扣（全场通用）"),
	/**
	 * 3 折扣（指定商品）
	 */
	DISCOUNT_ASSIGN_GOODS(3,"折扣（指定商品）"),
	;
	private Integer type;
	private String desc;

	CouponTypeEnum(Integer type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	/**
	 * 类型匹配
	 * @param type
	 * @return
	 */
	public static CouponTypeEnum matchType(Integer type){
		if(null == type){
			return null;
		}
		for (CouponTypeEnum couponTypeEnum : CouponTypeEnum.values()){
			if(type.equals(couponTypeEnum.getType())){
				return couponTypeEnum;
			}
		}
		return null;
	}


}
