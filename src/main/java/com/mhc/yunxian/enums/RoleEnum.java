package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.enums
 * @author: 昊天
 * @date: 2019/1/24 6:20 PM
 * @since V1.1.0-SNAPSHOT
 */
public enum RoleEnum {

    BUYER(0,"买家"),
    SELLER(1,"卖家");

    @Getter
    private Integer code;
    @Getter
    private String  msg;

    RoleEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
