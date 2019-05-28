package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/2/19 10:05 AM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class DragonOrderCount {

    /**
     * 用户头像
     */
    private String headImg;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 收货名称
     */
    private String addrName;

    /**
     * 收货电话
     */
    private String addrPhone;

    /**
     * 自提地址
     */
    private String dragonAddr;

    /**
     * 快递地址/送货上门地址
     */
    private String deliveryAddr;

    /**
     * 配送方式(0自提，1快递，2送货上们)
     */
    private Integer isDelivery;

    /**
     * 订单商品列表
     */
    private List<DragonOrderCountGood> goodsList;

}
