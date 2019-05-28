package com.mhc.yunxian.enums;

/**
 * 子订单类型
 *
 * @Author MoXiaoFan
 * @Date 2019/1/22 9:21
 */
public enum OrderTypeEnum {
    /**
     * 正常订单默认为此类型
     */
    NORMAL_ORDER(0, "普通订单"),
    /**
     * 退差类型
     */
    REFUND_ORDER(1, "退差子订单"),

    /**
     * 补差子订单状态
     */
    DIF_ORDER(2, "补差子订单");


    /**
     * 类型编号
     */
    private Integer code;

    /**
     * 类型
     */
    private String type;

    OrderTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public Integer getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
