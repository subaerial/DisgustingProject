package com.mhc.yunxian.bean;

import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.enums.ResultCodeEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;

@Data
public class BaseResponse implements Serializable {

    private int code = 200;

    private String msg = "";
    private Map<String, Object> map;


    public BaseResponse build(RespStatus respStatus){
        this.code = respStatus.getKey();
        this.msg = respStatus.getDesc();
        return this;
    }

    public BaseResponse build(String msg){
        this.code = 500;
        this.msg = msg;
        return this;
    }

    public BaseResponse build(int code ,String msg){
        this.code = code;
        this.msg = msg;
        return this;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public BaseResponse build(RespStatus respStatus, String msg){
        this.code = respStatus.getKey();
        if(StringUtils.isNoneBlank(msg)){
            this.msg = msg;
        } else {
            this.msg = respStatus.getDesc();
        }
        return this;
    }

    public static BaseResponse error(Integer code, String msg){
        BaseResponse result = new BaseResponse();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static BaseResponse error(ResultCodeEnum resultCodeEnum){
        BaseResponse result = new BaseResponse();
        result.setCode(resultCodeEnum.getCode());
        result.setMsg(resultCodeEnum.getMsg());
        return result;
    }

}
