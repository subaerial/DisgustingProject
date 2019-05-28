package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class MiniProgram implements Serializable {

    private String appid;
    private String pagepath;

}
