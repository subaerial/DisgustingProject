package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.DragonDraft;

public interface DragonDraftDao {
    int deleteByPrimaryKey(Integer id);

    int deleteByOpenid(String openid);

    int insertSelective(DragonDraft record);

    DragonDraft selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DragonDraft record);

    int updateByPrimaryKey(DragonDraft record);

    int updateStatusByOpenid(String openid);

    DragonDraft selectByOpenid(String openid);
}