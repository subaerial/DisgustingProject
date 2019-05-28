package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.MoneyRecord;

import java.util.List;

public interface MoneyRecordService {



    boolean add(MoneyRecord moneyRecord);

    boolean updateByDrawMoneyId(MoneyRecord moneyRecord);

    boolean updateByOrderNum(MoneyRecord moneyRecord);

    List<MoneyRecord> getRecordList(String openid);

    MoneyRecord selectRecordByDrawMoneyId(String drawMoneyId);

    MoneyRecord selectRecordByOrderNum(String orderNum);

    MoneyRecord selectRecordById(Integer id);


    List<MoneyRecord> listRecordByOrderNum(String orderNum);
}
