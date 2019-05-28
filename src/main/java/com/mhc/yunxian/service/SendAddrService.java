package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.SendAddr;

import java.util.List;

public interface SendAddrService {




    List<SendAddr> getAddr(String openid);


    boolean addAddr(SendAddr sendAddr);

    boolean updateAddr(SendAddr sendAddr);

    boolean delAddr(String addrNum);

    SendAddr getAddrByAddrNum(String addrNum);
}
