package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.WxAccessToken;

public interface WxAccessTokenDao {
    int deleteByPrimaryKey(Integer id);

    int insert(WxAccessToken record);

    int insertSelective(WxAccessToken record);

    WxAccessToken selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WxAccessToken record);

    int updateByPrimaryKey(WxAccessToken record);

}