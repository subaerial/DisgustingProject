package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.WxAdmin;

import java.util.List;

public interface WxAdminDao {
    int deleteByPrimaryKey(Integer id);

    int insert(WxAdmin record);

    int insertSelective(WxAdmin record);

    WxAdmin selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WxAdmin record);

    int updateByPrimaryKey(WxAdmin record);

    WxAdmin selectByOpenid(String openid);

    List<WxAdmin> selectAdmin();
}