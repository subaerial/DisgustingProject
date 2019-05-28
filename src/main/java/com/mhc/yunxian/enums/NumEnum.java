package com.mhc.yunxian.enums;

import lombok.Getter;

public enum NumEnum {

    NUM_MIN(1,"最低范围"),
    NUM_MAX(9999,"最高范围");

    @Getter
    Integer code;
    @Getter
    String msg;

    NumEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
