package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GetAllOrder implements Serializable {

    //卖家信息
    private String headImg;
    private String nickName;
    private String sellerPhone;

    private String orderNum;//订单号
    private String createTime;//订单创建时间

    List<Goods> goodsList;//商品信息列表

    private Integer goodsType;//商品种类数目
    private Integer totalMoney;
    private Integer realMoney;

    private Integer orderStatus;//订单状态

    private Integer isCOD;//是否货到付款

    private String dragonAddr;


    //买家地址
    private String userName;
    private String phone;
    private String addr;
    private String remark;
    private String buyOpenid;

    //补差价订单ID
    private String difOrderNum;

    private Integer isPayLater;

    private Integer sendNoticeNum;

    private Integer isDifOrder = 0;

    private String dragonNum;

    private String dragonTitle;

    private Integer isUsed;

    private Integer couponMoney;

    private String sellerDesc;

    private Integer deliveryType;

	/**
	 * 子订单
	 */
	private SubBizOrder subBizOrder;

}
