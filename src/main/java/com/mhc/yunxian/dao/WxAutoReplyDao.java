package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.WxAutoReply;

import java.util.List;

public interface WxAutoReplyDao {
    int deleteByPrimaryKey(Integer id);

    int insert(WxAutoReply record);

    int insertSelective(WxAutoReply record);

    WxAutoReply selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WxAutoReply record);

    int updateByPrimaryKey(WxAutoReply record);

    WxAutoReply selectByKeyword(String keyword);

    List<WxAutoReply> selectAll();

}