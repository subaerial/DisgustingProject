package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.*;

import java.util.Map;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service
 * @author: 昊天
 * @date: 2019/1/24 2:08 PM
 * @version: 0.0.1-vserion
 */
public interface MsgRecordService {

    /**
     * <p> 根据消息类型决定调用哪种转换方法 </p>
     * @param msgType     消息类型
     * @param convertArgs 转换参数
     * @return String     转换后的内容
     * @author 昊天
     * @date 2019/1/24 4:57 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    String getNewContent(Integer msgType, ConvertArgs convertArgs);

    /**
     * <p> 增加记录 </p>
     * @param openId
     * @param msgType
     * @param role
     * @param url
     * @param convertArgs
     * @return Map<String,Object>
     * @author 昊天
     * @date 2019/1/24 5:27 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    Map<String,Object> addMsgRecord(String openId, Integer msgType, Integer role, String url, ConvertArgs convertArgs);

    /**
     * <p> 把阅读状态改为已读 </p>
     * @param id
     * @return boolean
     * @author 昊天
     * @date 2019/1/28 2:27 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    boolean updateReadStatus(Integer id);
    

    /**
     * <p> 查询出消息列表 </p>
     * @param request
     * @return MsgListResponse
     * @author 昊天 
     * @date 2019/1/25 3:29 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    MsgListResponse listMsg(MsgRecordRequest request);
}
