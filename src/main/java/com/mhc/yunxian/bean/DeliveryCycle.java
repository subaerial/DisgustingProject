package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * Created by Administrator on 2018/12/13.
 */
@Data
public class DeliveryCycle {
	/**
	 * 发货周期类型
	 * 0 不是周期性发货
	 * 1.每天
	 * 2.每周
	 * 3.每月
	 */
	private Integer cycleType;

	/**
	 * 每周日--->周六 ===>   0-6
	 */
	private Integer[] dayOfWeek;

	/**
	 * 每月 1-31 ==> 0-30
	 */
	private Integer[] dayOfMonth;

	/**
	 * 发货:时
	 */
	private Integer hour;

	/**
	 * 发货:分
	 */
	private Integer minute;

	/**
	 * 截单:小时
	 * 早于周期发货时间几小时
	 * 前端已计算
	 */
	private Integer cutHour;

}
