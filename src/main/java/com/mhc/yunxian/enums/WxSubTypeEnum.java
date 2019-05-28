package com.mhc.yunxian.enums;

public enum WxSubTypeEnum implements EnumMsg<Byte>{

    SUBSCRIBE((byte)1,"已订阅"),
    UNSUBSCRIBE((byte)0,"未订阅");

    private Byte code;

    private String msg;

    WxSubTypeEnum (Byte code,String msg){
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public Byte getCode() {
        return code;
    }
}
