package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.MsgTemplate;

import java.util.List;

public interface MsgTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MsgTemplate record);

    int insertSelective(MsgTemplate record);

    MsgTemplate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MsgTemplate record);

    int updateByPrimaryKey(MsgTemplate record);

    /**
     * 根据消息类型查询消息模版(按时间降序)
     * @param msgType
     * @return
     */
    List<MsgTemplate> selectByMsgType(Integer msgType);

}