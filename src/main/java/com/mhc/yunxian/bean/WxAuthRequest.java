package com.mhc.yunxian.bean;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxAuthRequest extends  BaseRequest{
    @NotBlank
    private String code; //session code


    //private String source;



}
