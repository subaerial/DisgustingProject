package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class GetWXACodeRequest {

    private String scene;
    private String page;
    private Integer width;

}
