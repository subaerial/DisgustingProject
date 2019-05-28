package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/5/29.
 */
@Data
public class OrdersResponse extends BaseResponse {
	//购买者
	private String buyer;

	private String buyerPhone;

	private String orderNum;
	//收货人
	private String Consignee;
	private String dragonNum;

	private String transactionId;

	private Integer orderStatus;

	private String updateTime;//付款时间

	private String createTime;//下单时间

	private String orderRemark;

	private int orderMoney;

	private String addrNum;

	private String sendTime;//发货时间

	private String comfirmTime;//确认收货时间
	List<GoodsInfo> list;
}
