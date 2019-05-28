package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.SystemParam;

import java.util.List;

public interface SystemParamService {

    SystemParam selectOneByParamGroup(String paramGroup);

    List<SystemParam> selectByParamGroup(String paramGroup);

    int updateSystemParam(SystemParam systemParam);


}
