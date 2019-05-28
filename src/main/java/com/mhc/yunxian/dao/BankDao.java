package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.Bank;

import java.util.List;

public interface BankDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Bank record);

    int insertSelective(Bank record);

    Bank selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Bank record);

    int updateByPrimaryKey(Bank record);

    List<Bank> selectAll();
}