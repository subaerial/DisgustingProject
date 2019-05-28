package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GetMyAllOrder {

    private String orderNum;//订单编号
    private Date createTime;//下单时间
    private Integer orderStatus;//订单状态

    private List<Goods> goodsList;

    private String dragonTitle;
    /**
     * 商品数量
     */
    private Integer goodsType;
    /**
     * 订单金额
     */
    private Integer totalMoney;
    /**
     * 订单实际支付价格
     */
    private Integer realMoney;
    /**
     * 卖家信息
     */
    private String headImg;
    private String nickName;
    private String sellerPhone;

    private String sellerDesc;

    private Integer isDifOrder;

    /**
     * 买家信息
     */
    private String userName;
    private String phone;
    private String addr;
    /**
     * 备注
     */
    private String remark;

    private String dragonNum;

    private String difOrderNum;

    private String parentOrderNum;

    private String dragonAddr;

    private Integer isUsed;

    private Integer couponMoney;

    private Integer deliveryType;

    private SubBizOrder subBizOrder;





//    private Integer isCOD;//是否货到付款


}
