package com.mhc.yunxian.exception;

import lombok.Data;


public class DataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String msg;

    public DataException(String msg){
        super(msg);
    }

    public DataException(Integer code, String msg){
        super(msg);
        this.code = code;
    }

    public int getCode(){return code;}

    public void setCode(int code){this.code = code;}

    public String getMsg(){return msg;}

    public void setMsg(String msg){this.msg = msg;}
}
