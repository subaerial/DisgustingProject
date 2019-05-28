package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.enums
 * @author: 昊天
 * @date: 2019-03-01 15:58
 * @since V1.1.0-SNAPSHOT
 */
public enum DifTypeEnum {


    /**
     * 子订单类型枚举
     */
    NOMAL_ORDER(0,"普通订单"),
    RETREAT_ORDER(1,"退差子订单"),
    REPAIR_ORDER(2,"补差子订单")
    ;


    @Getter
    private Integer type;
    @Getter
    private String desc;

    DifTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }}
