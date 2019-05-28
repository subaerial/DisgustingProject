package com.mhc.yunxian.bean;

import lombok.Data;

@Data
public class WxKeywordRequest extends BaseRequest {

    private Integer id ;

    private String keyword;

    private String message;

}
