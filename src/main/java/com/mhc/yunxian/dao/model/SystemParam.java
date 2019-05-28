package com.mhc.yunxian.dao.model;

import lombok.Data;

import java.util.Date;

@Data
public class SystemParam {
    private Long sysParamId;

    private String paramGroup;

    private String paramKey;

    private String paramValue;

    private String paramDesc;

    private Byte status;

    private Date gmtCreate;

    private Date gmtModified;

    private String buttonImg;

    private String buttonImgHighlighting;


}