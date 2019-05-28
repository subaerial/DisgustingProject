package com.mhc.yunxian.bean;

import lombok.Data;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 * 接龙订单统计  商品信息
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/2/20 1:57 PM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class DragonOrderCountGood {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 购买次数
     */
    private Integer buyNumber;
}
