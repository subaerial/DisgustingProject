package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.BalanceDao;
import com.mhc.yunxian.dao.model.Balance;
import com.mhc.yunxian.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {


    @Autowired
    BalanceDao balanceDao;


    @Override
    public int insertBalance(Balance balance) {

        return balanceDao.insert(balance);
    }

    @Override
    public Balance getBalance(String openid) {

        return balanceDao.selectBalance(openid);
    }

    @Override
    public boolean updateBalanceByOpenid(Balance balance) {
        int flag=balanceDao.updateByOpenId(balance);
        if (flag>0){
            return true;
        }
        return false;
    }

    @Override
    public Balance lockBalance(Integer id) {
        return balanceDao.lockBalance(id);
    }


}
