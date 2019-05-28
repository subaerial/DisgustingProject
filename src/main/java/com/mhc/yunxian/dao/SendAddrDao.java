package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.SendAddr;

import java.util.List;

public interface SendAddrDao {
//    int deleteByPrimaryKey(Integer id);
//
//    int insert(SendAddr record);
//
//    int insertSelective(SendAddr record);
//
//    SendAddr selectByPrimaryKey(Integer id);
//
//    int updateByPrimaryKeySelective(SendAddr record);
//
//    int updateByPrimaryKey(SendAddr record);



    List<SendAddr> selectAddr(String openid);


    int insertAddr(SendAddr sendAddr);

    int updateAddr(SendAddr sendAddr);

    int delAddr(String addrNum);

    SendAddr selectAddrByAddrNum(String addrNum);
}