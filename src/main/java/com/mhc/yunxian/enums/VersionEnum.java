package com.mhc.yunxian.enums;

import lombok.Getter;

@Getter
public enum VersionEnum {
    RELEASE(0, "release"),
    PRODUCT(1, "product");

    VersionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;

    private String msg;


}

