package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentInfo implements Serializable {


    private String headImg;
    private String nickName;
    private String comment;
}
