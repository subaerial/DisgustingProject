package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/1/25 3:05 PM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class MsgListResponse extends BaseResponse {

    /**
     * 消息总条数
     */
    private Integer count;

    /**
     * 消息记录列表
     */
    private List<MsgRecordVO> recordVOS;
}
