package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.Balance;

public interface BalanceService {

    int insertBalance(Balance balance);
    Balance getBalance(String openid);
    boolean updateBalanceByOpenid(Balance balance);

    Balance lockBalance(Integer id);
}
