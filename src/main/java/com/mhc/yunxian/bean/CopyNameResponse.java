package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/2/19 11:04 AM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class CopyNameResponse extends BaseResponse{

    /**
     * 复制的用户名列表，以，分割
     */
    private String userName;

    /**
     * 自提地点
     */
    private List<String> addrs;

    /**
     * 发货时间
     */
    private Date sendTime;
}
