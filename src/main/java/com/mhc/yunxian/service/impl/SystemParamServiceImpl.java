package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.SystemParamDao;
import com.mhc.yunxian.dao.model.SystemParam;
import com.mhc.yunxian.service.SystemParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SystemParamServiceImpl implements SystemParamService {

    @Autowired
    SystemParamDao systemParamDao;


    @Override
    public SystemParam selectOneByParamGroup(String paramGroup){
        return systemParamDao.selectOneByParamGroup(paramGroup);
    }

    @Override
    public List<SystemParam> selectByParamGroup(String paramGroup){
        return systemParamDao.selectByParamGroup(paramGroup);
    }

    @Override
    public int updateSystemParam(SystemParam systemParam) {

        return systemParamDao.updateByPrimaryKeySelective(systemParam);
    }
}
