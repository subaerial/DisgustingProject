package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.Balance;

public interface BalanceDao {
//    int deleteByPrimaryKey(Integer id);
//
    int insert(Balance record);
//
//    int insertSelective(Balance record);
//
//    Balance selectByPrimaryKey(Integer id);
//
//    int updateByPrimaryKeySelective(Balance record);
//
//    int updateByPrimaryKey(Balance record);


    int updateByOpenId(Balance balance);
    Balance  selectBalance(String openid);

    Balance lockBalance(Integer id);
}