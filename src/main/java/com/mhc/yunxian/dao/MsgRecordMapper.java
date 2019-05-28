package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.request.query.MsgRecordQuery;
import com.mhc.yunxian.dao.model.MsgRecord;

import java.util.List;

public interface MsgRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MsgRecord record);

    int insertSelective(MsgRecord record);

    MsgRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MsgRecord record);

    int updateByPrimaryKey(MsgRecord record);

    /**
     * 按条件查询
     * @param query
     * @return
     */
    List<MsgRecord> queryMsgRecord(MsgRecordQuery query);
}