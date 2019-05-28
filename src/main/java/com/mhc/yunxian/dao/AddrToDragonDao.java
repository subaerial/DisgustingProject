package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.AddrToDragon;

public interface AddrToDragonDao {
    int deleteByPrimaryKey(Integer id);

    int deleteByDragonNum(String dragonNum);

    int insert(AddrToDragon record);

    int insertSelective(AddrToDragon record);

    AddrToDragon selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AddrToDragon record);

    int updateByPrimaryKey(AddrToDragon record);
}