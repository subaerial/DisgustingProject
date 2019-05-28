package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/1/24 6:51 PM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class MsgRecordRequest extends BaseRequest implements Serializable  {

    /**
     * 角色（0买家，1卖家）
     */
    private Integer role;

    /**
     * 会话id
     */
    private String  sessionId;

    /**
     * 是否已读（0未读，1已读）
     */
    private Integer isRead;


}
