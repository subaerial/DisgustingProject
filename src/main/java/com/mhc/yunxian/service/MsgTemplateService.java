package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.MsgTemplate;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service
 * @author: 昊天
 * @date: 2019/1/24 2:03 PM
 * @version: 0.0.1-vserion
 */
public interface MsgTemplateService {

    /**
     * 根据消息类型查询消息模版 (最新的)
     * @param msgType
     * @return
     */
    List<MsgTemplate> getByMsgType(Integer msgType);
}
