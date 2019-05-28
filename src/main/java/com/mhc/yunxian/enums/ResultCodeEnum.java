package com.mhc.yunxian.enums;

public enum  ResultCodeEnum implements EnumMsg<Integer> {


    SERVER_ERROR(500, "服务器异常")
    ;


    private Integer code;

    private String msg;

    ResultCodeEnum(Integer code, String msg){
        this.code = code;

        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
