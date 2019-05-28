package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.SendAddrDao;
import com.mhc.yunxian.dao.model.SendAddr;
import com.mhc.yunxian.service.SendAddrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SendAddrServiceImpl implements SendAddrService {

    @Autowired
    SendAddrDao sendAddrDao;


    @Override
    public List<SendAddr> getAddr(String openid) {
        return sendAddrDao.selectAddr(openid);
    }

    @Override
    public boolean addAddr(SendAddr sendAddr) {

        if (sendAddrDao.insertAddr(sendAddr) < 1){
            return false;
        }

        return true;
    }

    @Override
    public boolean updateAddr(SendAddr sendAddr) {
        if (sendAddrDao.updateAddr(sendAddr) < 1){
            return false;
        }
        return true;
    }

    @Override
    public boolean delAddr(String addrNum) {

        if (sendAddrDao.delAddr(addrNum) < 1){
            return false;
        }
        return true;
    }

    @Override
    public SendAddr getAddrByAddrNum(String addrNum) {
        return sendAddrDao.selectAddrByAddrNum(addrNum);
    }
}
