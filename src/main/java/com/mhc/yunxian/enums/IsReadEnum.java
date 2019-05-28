package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.enums
 * @author: 昊天
 * @date: 2019/1/24 6:15 PM
 * @since V1.1.0-SNAPSHOT
 */
public enum IsReadEnum {

    UNREAD(0,"未读"),
    READ(1,"已读");

    @Getter
    private Integer code;
    @Getter
    private String  msg;

    IsReadEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
