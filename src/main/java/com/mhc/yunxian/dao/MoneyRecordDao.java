package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.MoneyRecord;

import java.util.List;

public interface MoneyRecordDao {
//    int deleteByPrimaryKey(Integer id);
//
//    int insert(MoneyRecord record);

    int insertSelective(MoneyRecord record);

    int updateByDrawMoneyId(MoneyRecord moneyRecord);

    int updateByOrderNum(MoneyRecord moneyRecord);

//    MoneyRecord selectByPrimaryKey(Integer id);
//
//    int updateByPrimaryKeySelective(MoneyRecord record);
//
    int updateByPrimaryKey(MoneyRecord record);

    MoneyRecord selectRecordByDrawMoneyId(String drawMoneyId);

    MoneyRecord selectRecordByOrderNum(String orderNum);

    MoneyRecord selectById(Integer id);


    List<MoneyRecord> selectByOpenid(String openid);

    MoneyRecord selectRefundedRecordByOrderNum(String orderNum);

    List<MoneyRecord> listByOrderNum(String orderNum);
}