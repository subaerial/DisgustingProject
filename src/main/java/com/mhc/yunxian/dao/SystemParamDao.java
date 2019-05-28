package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.SystemParam;

import java.util.List;

public interface SystemParamDao {
    int deleteByPrimaryKey(Long sysParamId);

    int insertSelective(SystemParam record);

    SystemParam selectByPrimaryKey(Long sysParamId);

    int updateByPrimaryKeySelective(SystemParam record);

    List<SystemParam> selectByParamGroup(String paramGroup);

    SystemParam selectOneByParamGroup(String paramGroup);
}