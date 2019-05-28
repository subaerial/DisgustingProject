package com.mhc.yunxian.enums;
@Deprecated

/**
 * 请使用DragonDeliveryEnum
 */

public enum DeliveryTypeEnum implements EnumMsg<Integer> {

    ITSELF(0,"自己提货"),
    HIMSELF(1,"别人送货"),
    OTHERSELF(2,"等别人送货上门")



    ;

    private Integer code;
    private String msg;

    DeliveryTypeEnum(Integer code,String msg){
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
