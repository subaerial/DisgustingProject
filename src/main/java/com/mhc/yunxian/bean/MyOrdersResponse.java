package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.*;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2018/5/9.
 */
@Data
public class MyOrdersResponse extends BaseResponse {
	//买家昵称
	private String sellerName;

	//买家手机号码
	private String phoneNum;

	//购买订单编号
	private String orderNum;

	//下单时间
	private String orderCreateTime;

	//购买的商品
	private List<OrderGoodsInfo> goods;


}
