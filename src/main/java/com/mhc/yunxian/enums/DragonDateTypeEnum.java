package com.mhc.yunxian.enums;

/**
 * @author Administrator
 */

public enum DragonDateTypeEnum {
    /**
     * 截单时间类型
     */
    DRAGON_DATE_TYPE_CUT_OFF_TIME(1, "截单时间类型"),
    /**
     * 发货时间类型
     */
    DRAGON_DATE_TYPE_DELIVERY_TIME(2, "发货时间类型"),
    ;
    Integer code;
    String desc;

    DragonDateTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }}
