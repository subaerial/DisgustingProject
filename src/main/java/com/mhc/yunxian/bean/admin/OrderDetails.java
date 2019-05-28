package com.mhc.yunxian.bean.admin;

import com.mhc.yunxian.bean.Goods;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetails implements Serializable {

    private String sellerOpenid;//卖家openid
    private String sellerName;
    private String buyerOpenid;//买家openid；
    private String buyerName;
    private String sellerPhone;
    private String buyerPhone;

    List<Goods> goodsList;

    private String orderNum;

    private Integer orderStatus;//
    private Date commitOrderTime;//下单时间
    private Date payTime;//付款时间
    private Date sendTime;//发货时间
    private Date comfirmTime;//确认收货时间

    private Integer orderMoney;
    private Integer goodsTypeNumber;
    private String addr;
    private String userName;
    private String dragonAddr;
    private Integer isCod;//是否货到付款

    /**
     * 配送方式
     */
    private Integer isDelivery;

}
