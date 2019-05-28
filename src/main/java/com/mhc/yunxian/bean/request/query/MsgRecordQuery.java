package com.mhc.yunxian.bean.request.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.bean.request.query
 * @author: 昊天
 * @date: 2019/1/25 10:21 AM
 * @since V1.1.0-SNAPSHOT
 */
@Data
@ApiModel(value = "MsgRecordQuery",description = "消息记录查询封装对象")
public class MsgRecordQuery extends BaseQuery{

    /**
     * 消息ID
     */
    @ApiModelProperty(value = "id", notes = "消息ID")
    private Integer id;

    /**
     * 用户openId
     */
    @ApiModelProperty(value = "openId", notes = "用户openId")
    private String openId;

    /**
     * 角色（0买家，1卖家）
     */
    @ApiModelProperty(value = "role", notes = "角色")
    private Integer role;

    /**
     * 消息内容
     */
    @ApiModelProperty(value = "content", notes = "消息内容")
    private String  content;

    /**
     * 需要跳转的链接，用http：//xxx的方式
     */
    @ApiModelProperty(value = "jumpUrl", notes = "需要跳转的链接")
    private String  jumpUrl;

    /**
     * 是否已读（0未读，1已读）
     */
    @ApiModelProperty(value = "isRead", notes = "是否已读")
    private Integer isRead;

    /**
     * 消息类型
     */
    @ApiModelProperty(value = "msgType", notes = "消息类型")
    private Integer msgType;

    /**
     * 是否删除（0未删，1已删）
     */
    @ApiModelProperty(value = "isDelete", notes = "是否删除")
    private Integer isDelete;

    /**
     * 消息阅读时间
     */
    @ApiModelProperty(value = "readTime", notes = "消息阅读时间")
    private Date readTime;

    /**
     * 消息创建时间
     */
    @ApiModelProperty(value = "createTime", notes = "消息创建时间")
    private Date  createTime;
}
