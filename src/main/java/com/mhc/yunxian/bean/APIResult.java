package com.mhc.yunxian.bean;

import com.mhc.yunxian.constants.RespStatus;

import java.io.Serializable;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/1/24 6:35 PM
 * @since V1.1.0-SNAPSHOT
 */
public class APIResult<T> implements Serializable {

    private boolean success = false;
    private String  code;
    private String  message;
    private T   data;

    public APIResult() {
    }

    public APIResult(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> APIResult<T> create(){
        return new APIResult();
    }

    public static <T> APIResult<T> create(T data) {
        APIResult<T> result = create();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <T> APIResult<T> create(T data, String code, String message) {
        APIResult<T> result = create();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> APIResult<T> create(T data, boolean isSuccess, String code, String message) {
        APIResult<T> result = create();
        result.setSuccess(isSuccess);
        result.setData(data);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> APIResult<T> create(String code, String message) {
        APIResult<T> result = create();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> APIResult<T> create(RespStatus respStatus) {
        APIResult<T> result = create();
        result.setSuccess(false);
        result.setCode(respStatus.getKey().toString());
        result.setMessage(respStatus.getDesc());
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
