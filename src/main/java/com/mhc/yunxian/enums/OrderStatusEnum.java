package com.mhc.yunxian.enums;

public enum OrderStatusEnum implements EnumMsg<Integer>{

    PENDING_PAYMENT(0,"待付款"),
    PENDING_DELIVERY(1,"待发货"),
    PENDING_RECEIVED(2,"待收货"),
    COMPELETED(3,"已完成(确认收货)"),
    REFUNDING(4,"退款中"),
    REFUNDED(5,"退款成功"),
    CANCELLED(6,"已取消");

    private Integer code;

    private String msg;

    private OrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
