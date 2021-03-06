package com.mhc.yunxian.enums;

import lombok.Getter;

public enum DifPriceEnum {


    DIF_PRICE_MIN(1,"最低范围"),
    DIF_PRICE_MAX(999999,"最高范围");

    @Getter
    Integer code;
    @Getter
    String msg;

    DifPriceEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
