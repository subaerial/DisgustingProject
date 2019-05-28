package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.Logistics;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderDetailResponse extends BaseResponse {


    private String orderNum;//订单编号
    private Date createTime;//下单时间
    private Integer orderStatus;//订单状态

    private List<Goods> goodsList;

    private Integer totalMoney;//订单金额
    private Integer realMoney;//订单实际支付价格

    //卖家信息
    private String headImg;
    private String nickName;
    private String sellerPhone;
    /**
     * 卖家openid
     */
    private String sellerOpenId;
    private Integer repurchase;
    private Long shopId;

    /**
     * 店家备注
     */
    private String sellerRemark;

    //买家信息
    private String userName;
    private String phone;
    private String addr;

    /**
     * 下单用户备注
     */
    private String remark;

    private String dragonNum;

    private String difOrderNum;

    private String parentOrderNum;

    private String dragonAddr;

    private int isDifOrder;

    private int isPayLater;

    private Date sendTime;
    /**
     * 是否使用券
     */
    private Integer isUsed;

    /**
     * 券的金额
     */
    private Integer couponMoney;

    /**
     * 配送方式 发货方式 0:自提 1:快递 2:送货上门
     */
    private Integer deliveryType;

    /**
     * 接龙标题
     */
    private String dragonTitle;

    /**
     * 退补差价
     */
    private Integer refundMoney = Integer.valueOf(0);
    /**
     * 物流信息
     */
    private Logistics logistics;

    /**
     * 子订单
     */
    private SubBizOrder subBizOrder;
}
