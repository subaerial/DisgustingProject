package com.mhc.yunxian.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class KeyWordRequest implements  java.io.Serializable{

    @JSONField(name="value")
    private String val;
    private String color="#2a5caa";




}
