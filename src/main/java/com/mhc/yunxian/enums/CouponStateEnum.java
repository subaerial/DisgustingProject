package com.mhc.yunxian.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.enums
 * @author: 昊天
 * @date: 2019/2/21 1:45 PM
 * @since V1.1.0-SNAPSHOT
 */
@AllArgsConstructor
public enum CouponStateEnum {

    EXPIRE_COUPON(-1,"无效的红包"),
    VAILD_COUPON(0,"有效的红包");

    @Getter
    private Integer code;

    @Getter
    private String msg;
}
