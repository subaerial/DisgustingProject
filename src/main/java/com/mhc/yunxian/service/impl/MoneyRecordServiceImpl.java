package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.MoneyRecordDao;
import com.mhc.yunxian.dao.model.MoneyRecord;
import com.mhc.yunxian.service.MoneyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MoneyRecordServiceImpl implements MoneyRecordService {


    @Autowired
    MoneyRecordDao moneyRecordDao;

    @Override
    public boolean add(MoneyRecord moneyRecord) {
        return moneyRecordDao.insertSelective(moneyRecord) > 0;
    }

    @Override
    public List<MoneyRecord> getRecordList(String openid) {
        return moneyRecordDao.selectByOpenid(openid);
    }

    @Override
    public MoneyRecord selectRecordByDrawMoneyId(String drawMoneyId){
        return moneyRecordDao.selectRecordByDrawMoneyId(drawMoneyId);
    }

    @Override
    public MoneyRecord selectRecordByOrderNum(String orderNum){
        return moneyRecordDao.selectRecordByOrderNum(orderNum);
    }

    @Override
    public MoneyRecord selectRecordById(Integer id){
        return moneyRecordDao.selectById(id);
    }

    @Override
    public List<MoneyRecord> listRecordByOrderNum(String orderNum) {
        return moneyRecordDao.listByOrderNum(orderNum);
    }

    @Override
    public boolean updateByDrawMoneyId(MoneyRecord moneyRecord){
        return moneyRecordDao.updateByDrawMoneyId(moneyRecord)>0 ;
    }

    @Override
    public boolean updateByOrderNum(MoneyRecord moneyRecord){
        return moneyRecordDao.updateByOrderNum(moneyRecord)>0 ;
    }
}
