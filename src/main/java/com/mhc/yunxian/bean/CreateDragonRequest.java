package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.DragonAddr;
import lombok.Data;

import java.util.List;

@Data
public class CreateDragonRequest extends BaseRequest {


	private String openid;

	private String sessionId;

	private String title;

	private String remark;

	private String dragonImage;

	private Long endTime;

	private Integer isCOD;//是否货到付款

	private Long sendTime;

	private List<Integer> dragonAddrIds;

	private String dragonNum;//接龙号

	private String phone;

	private Integer dragonStatus;

	private String formId;

	private Integer isPayLater;

	private String parentDragonNum;//父接龙号

	List<Goods> data;

	private int isDelivery = 0;

	private int globalLimit;

	private byte isCoupon;

	/**
	 * 新增dragon_video字段
	 */
	private String dragonVideo;

	/**
	 * 发货周期
	 */
	private DeliveryCycle deliveryCycle;
	/**
	 * 选择周期性发货时的截单时间
	 * 一次性发货时,接龙结束时间为截单时间
	 */
	private String cutOffTime;

}
