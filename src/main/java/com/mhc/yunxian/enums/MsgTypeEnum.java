package com.mhc.yunxian.enums;

import lombok.Getter;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 * 消息记录类型枚举
 * @Package com.mhc.yunxian.enums
 * @author: 昊天
 * @date: 2019/1/24 11:46 AM
 * @version: 0.0.1-vserion
 */
public enum MsgTypeEnum {

    /**
     * 买家相关通知
     */
    BUYER_ORDER_SUCCESS(0,"下单成功通知"),
    BUYER_SHIPPING_REMINDER(1,"发货提醒通知"),
    BUYER_REFUND_TO_ACCUNT(2,"退款到账通知"),
    BUYER_REFUND_REJECTED(3,"退款被拒通知"),
    BUYER_PATCH_DIF(4,"补差价通知"),
    BUYER_RETREAT_DIF(5,"退差价通知"),
    BUYER_RECEIPT_REMINDER(6,"收货提醒通知"),
    /**
     * 卖家相关通知
     */
    SELLER_ORDER_NOTICE(7,"卖家下单通知"),
    SELLER_REFUND_APPLY(8,"卖家退款申请通知"),
    SELLER_WITHDRAWAL_TO_ACCUNT(9,"卖家提现到账通知"),
    SELLER_DRAGON_NOTICE(10,"卖家接龙通知");


    @Getter
    private Integer code;

    @Getter
    private String msg;

    MsgTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 根据消息类型获取标题
     * @param type
     * @return
     */
    public static String getTitleByType(Integer type){
        if(null == type){
            return "";
        }
        for (MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()){
            if(msgTypeEnum.getCode().equals(type)){
                return msgTypeEnum.getMsg();
            }
        }
        return "";
    }
}
