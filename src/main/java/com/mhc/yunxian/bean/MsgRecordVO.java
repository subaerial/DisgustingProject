package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean
 * @author: 昊天
 * @date: 2019/1/25 9:46 AM
 * @since V1.1.0-SNAPSHOT
 */
@Data
public class MsgRecordVO implements Serializable {

    /**
     * 消息ID
     */
    private Integer id;
    /**
     * 角色（0买家，1卖家）
     */
    private Integer role;
    /**
     * 消息标题
     */
    private String  title;
    /**
     * 消息内容
     */
    private String  content;
    /**
     * 需要跳转的链接，用http：//xxx的方式
     */
    private String  jumpUrl;
    /**
     * 是否已读（0未读，1已读）
     */
    private Integer isRead;
    /**
     * 消息类型
     */
    private Integer msgType;
    /**
     * 是否删除（0未删，1已删）
     */
    private Integer isDeleted;
    /**
     * 消息阅读时间
     */
    private Long    readTime;
    /**
     * 消息创建时间
     */
    private Long    createTime;
}
