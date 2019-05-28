package com.mhc.yunxian.bean.coupon;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@ApiModel
public class AddCouponRequest {

	@NotBlank
	private String sessionId;

	/**
	 * 发放数量
	 */

	@NotNull
	private int totalAmount;
	/**
	 * 红包金额
	 */
	@NotNull
	private int couponAmount;
	/**
	 * 开始时间
	 */
	private Date startTime;
	/**
	 * 结束时间
	 */
	private Date endTime;
	/**
	 * 红包类型
	 */
	@NotNull
	private int couponType;
	/**
	 * 绑定商品编号
	 */
	private List<String> goodsNumList;

	/**
	 * 0:无 1:新用户 2:老用户
	 */
	private Integer userLimitState;
	/**
	 * 用户限制购买数量
	 */
	private Integer userLimitNum;

	/**
	 * 券的状态
	 * 0 可用 -1不可用
	 */
	private Integer state;
}
