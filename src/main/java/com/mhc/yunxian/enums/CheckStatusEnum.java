package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
public enum CheckStatusEnum {

    START_CHECK(1, "开启审核"),
    CLOSE_CHECK(0, "关闭审核");

    private Integer code;
    private String desc;

    CheckStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
