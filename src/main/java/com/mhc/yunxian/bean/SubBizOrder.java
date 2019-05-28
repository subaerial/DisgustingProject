package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.OrderGoodsInfo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @date 2018/12/15
 */
@Data
public class SubBizOrder {

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String headImg;

    /**
     * 订单金额
     */
    private Integer orderMoney;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单类型
     */
    private Integer orderType;


    /**
     * 订单商品
     */
    private List<GetMyUserOrderGoods> goodsList;
    /**
     * 订单商品列表
     */
    private List<OrderGoodsInfo> orderGoodsInfoList;
}
