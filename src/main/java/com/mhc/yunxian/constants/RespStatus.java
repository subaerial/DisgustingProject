package com.mhc.yunxian.constants;

public enum RespStatus implements Type<Integer> {

	SUCCESS(200, ""),
	SYSTEM_ERROR(500, "数据请求失败"),
	NOT_LONGIN(501, "登陆失效，请重新登陆"),


	/**
	 * 微信用户Status
	 */
	SESSION_ID_EXPIRE(1000, "sessionId过期"),
	USER_NOT_EXIST(1001, "用户不存在"),
	WX_GETOPENID_ERROR(1002, "获取用户openid错误"),
	ILLEGAL_ARGUMENT(1003, "参数错误"),
	USER_REG_ERROR(1004, "注册用户失败"),
	ACCOUNT_IS_BANNED(1005, "该账号已被封禁，请联系客服解封"),
	YOU_NOT_LOGIN(1006, "你未登录，不能发布接龙"),
	GET_ACCESS_TOKEN_ERROR(1007, "获取ACCESS_TOKEN失败"),
	GET_USER_INFO_ERROR(1008, "获取用户信息失败"),
	ADD_ADMIN_FAIL(1009, "添加微信用户失败"),
	PROGRAM_NOT_EXIST(1010, "小程序尚未授权"),


	/**
	 * dragon
	 */
	CURRENT_NOT_DRAGON(2001, "当前没有接龙信息"),
	ADD_DRAGON_FAIL(2002, "添加接龙失败"),
	DRAGON_NOT_EXIST(2003, "接龙不存在"),
	COMMENT_ADD_FAIL(2004, "评论失败"),
	COMMENT_LENGTH_TOLONG(2005, "评论字数过长，请保持在120字以下"),
	DRAGON_IS_EXIST(2006, "该接龙已存在，请勿重复发布"),
	NOT_DRAGON(2007, "你还未发起接龙"),
	DRAGON_IS_END(2008, "该接龙已结束"),
	DRAGON_ALREADY_EXIST_ORDER(2009, "该接龙已有人下单，不能编辑"),
	UPDATE_DRAGON_ERROR(2010, "接龙修改失败"),
	DRAGON_ADD_ERROR(2011, "接龙发布失败，请重新发布"),
	ADDR_IS_NULL(2012, "自提地址不能为空"),
	GLOBAL_LIMIT_AND_GOODS_LIMIT_CANNOT_EXIST(2013, "商品限购和全局限购不能同时存在"),
	ADDR_NAME_LIMIT(2014,"自提点名称不能超过20字"),
	DRAGON_TITLE_LIMIT(2015,"接龙名称太长，请保持在24字以下"),
	DRAGON_REMARK_LIMIT(2016,"接龙详情描述太长，请保持在200字以下"),
	DRAGON_PHONE_LIMIT(2017,"手机号码格式不匹配"),
	CUT_TIME_LIMIT(2018,"结束时间应在发货时间之前24h内"),
	END_TIME_LIMIT(2019,"结束时间不能早于当前时间"),
	DRAGON_IMAGE_LIMIT(2020,"接龙图片请在2～9之间"),

	/**
	 * goods
	 */
	GOODS_SELL_OUT(3001, "商品已卖完"),
	GOODS_IS_NOT_EXIST(3002, "该商品不存在"),
	SEND_GOODS_SUCCESS(3003, "发货成功"),
	GOODS_NUM_ERROR(3004, "商品数量修改错误"),
	GOODS_NOT_NULL(3005, "商品信息为空"),
	GOODS_BUY_NUM_TOO_MANY(3006, "购买数量超过库存数量"),
	MANY_THAN_GLOBAL_LIMIT(3007, "购买数量超过全局限购数量"),
	GOODS_PRICE_LIMIT(3009, "商品价格必须在0.01到9999.99之间"),
	GOODS_NUM_LIMIT(3010, "商品库存必须在1到9999之间"),
	GOODS_SPECIFICATION_LIMIT(3011, "商品规格长度必须在12位"),
	GOODS_NAME_LIMIT(3012, "商品名称不能超过12位"),
	GOODS_NAME_EMOJI(3013, "商品名称不能输入表情包"),
	GOODS_NAME_CANT_UPDATE(3014, "商品名称不能被修改"),



	/**
	 * shop
	 */
    SHOP_SHOPINTRO_LIMIT(3501,"店铺简介长度不能超出140"),

	/**
	 * 订单
	 */
	UNIFIED_ORDER_SUCCESS(200, "统一下单成功!"),
	UNIFIED_ORDER_FAILURE(4001, "统一下单失败!"),
	DRAW_MONEY_ERROR(4002, "上次提现还未处理完毕，请等待完成后再提现！"),
	BALANCE_IS_ZERO(4003, "余额为零，无法提现"),
	CURRENT_NOT_DETAIL(4004, "当前没有提现纪录"),
	BLANCE_IS_NULL(4005, "当前没有余额"),
	CURRENT_NOT_ORDER(4006, "当前没有参与的订单"),

	BACK_MONEY_FILE(4007, "退款失败"),
	ORDER_NOT_EXIST(4008, "订单不存在"),
	BALANCE_UPDATE_ERROR(4009, "余额更新失败"),
	BALANCE_NOT_ENOUGH(4010, "申请提现金额超过可提现余额"),

	BALANCE_FAIL(4011, "余额未超过500元，无法提现"),
	REFUND_FAIL(4012, "退款失败"),

	ADD_RECORD_ERROR(4013, "添加交易纪录错误"),

	CURRENT_NOT_RECORD(4014, "当前没有交易纪录"),

	REVISE_ORDER_MONEY_ERROR(4015, "修改订单金额错误"),

	COMMIT_ORDER_ERROR(4016, "订单确认失败"),

	ORDER_PAY_ERROR(4017, "该订单为后付款订单，商家未确认，无法付款"),

	GOODS_NULL_ORDER_FAIL(4018, "订单未及时付款，该商品已卖完"),

	DIF_PRICE_TOO_MUCH(4019, "退款差价不能超过订单总额"),

	SEND_NOTICE_NUM_EMPTY(4020, "发送通知次数已空"),

	DRAGON_GOODS_NUM_TO_MANY(4021, "订单商品数量超过限购数量"),

	ORDER_CANCEL_FAILED(4022, "取消失败"),

	ORDER_MONEY_CHANGE_FAILED(4023, "只有待付款订单可以修改价格"),

	CHANGE_MONEY_TO_MUCH(4024, "差价不能超过订单总额"),

	DIF_ORDER_CREATE_FAILED(4025, "不能对补差订单执行补差操作"),

	DIF_ORDER_CREATE_ONLY_ONE(4026, "同一订单只能补差一次"),

	ORDER_NOT_EXIST_RE_CRY(4027, "该订单可能被调价，请重试"),

	BUYER_INFO(4028, "买家收货信息请填写完整"),

	NOT_LONGIN_CANNOT_ORDER(4029, "您未登陆，无法下单！"),

	ORDER_ALREADY_REFUNDED(4030, "订单已退款, 请勿重复退款!"),

	DIF_PRICE_LIMIT(4031,"退补差金额必须在0.01到9999.99之间"),

	ADD_MSG_RECORD_FAIL(4888,"添加记录失败"),


	/*后台管理*/
	OLD_PWD_ERROR(5001, "旧密码填写错误"),
	KEYWORD_EXIST(5002, "该微信关键词已经存在"),

	/*用户信息*/
	NOT_BANK_INFO(6001, "未补全银行卡信息"),
	NOT_BANK_CC(6002, "不支持信用卡"),
	NOT_BANK_YINGLIAN(6003, "不支持非银联银行卡"),

	/*提现*/
	DRAW_MONEY_FAIL(7001, "提现失败"),
	DRAW_MONEY_HAD_COMPELETED(7002, "该提现记录已成功提现"),
	DRAW_MONEY_TOO_LARGE(7003, "由于微信限制，提现金额不超过20000元"),

	/*红包(由于前端限制，领取红包接口返回的code需要为200)*/
	AMOUNT_CAN_NOT_LESS_THAN_ONE(8001, "红包发放数量不能小于1，优惠金额不能小于1分"),
	GOODS_NUM_CAN_NOT_BLANK(8002, "发放单品红包时，商品编号不能为空"),
	DATE_CAN_NOT_BLANK(8003, "开始时间和结束时间不能为空"),
	START_TIME_ERROR(8004, "开始时间不能早于当前时间"),
	END_TIME_ERROR(8005, "结束时间不能早于当前时间"),
	TIME_ERROR(8006, "结束时间不能早已开始时间"),
	COUPON_ADD_ERROR(8007, "红包添加失败"),
	GOODS_NUM_CAN_NOT_BE_BLANK(8008,"券类型为指定商品的折扣券或红包时,商品编号列表不能为空!"),
	GOODS_NOT_EXIST_OR_DELETED(8009,"指定的商品编号列表商品不存在或已删除!"),
	COUPON_NOT_EXIST(400, "红包不存在"),
	COUPON_EXPIRE(401, "红包已过期"),
	HAS_GOT_COUPON(402, "你已经抢过这个红包了"),
	COUPON_IS_BLANK(403, "红包派发完了"),
	COUPON_GET_FAILED(404, "红包领取失败"),
	SELLER_CAN_NOT_GET_COUPON(405, "买家不能领取自己派发的红包"),
	GOODS_TOO_MANY(406, "绑定商品最多添加10种"),
	COUPON_FAILED(407,"券领取失败!"),

	NONO_NAME_CAN_COPY(404,"没有买家昵称可复制"),

	OPERATION_SUCCEED(200,"操作成功!"),
	OPERATION_FAILURE(9001, "操作失败!"),
	/**
	 * DB相关
	 */
	DB_UPDATE_FAILED(9002, "更新数据失败!"),
	DB_QUERY_FAILED(9003, "查询失败!"),
	DB_INSERT_FAILED(9004, "插入失败!"),;
	public final int key;
	public final String desc;

	RespStatus(int key, String desc) {
		this.key = key;
		this.desc = desc;
	}


	@Override
	public Integer getKey() {
		return key;
	}

	@Override
	public String getDesc() {
		return desc;
	}
}
