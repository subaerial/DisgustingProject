package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.PartyInfoDao;
import com.mhc.yunxian.dao.model.PartInfo;
import com.mhc.yunxian.service.PartyInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PartyInfoServiceImpl implements PartyInfoService {

    @Autowired
    PartyInfoDao partyInfoDao;

    @Override
    public List<PartInfo> getPartyUser(String dragonNum) {
        return partyInfoDao.selectParty(dragonNum);
    }

    @Override
    public boolean addPartInfo(PartInfo partInfo) {

        return  partyInfoDao.insertParty(partInfo) > 0;
    }

    @Override
    public boolean existPartInfo(PartInfo partInfo) {
        return  partyInfoDao.existPartyByOrderNum(partInfo) > 0;
    }

    @Override
    public boolean updatePartInfo(PartInfo partInfo) {
        return  partyInfoDao.updatePartyByOrderNum(partInfo) > 0;
    }

    @Override
    public PartInfo getPartByOrderNum(String orderNum){
        return partyInfoDao.selectByOrderNum(orderNum);
    }

    @Override
    public boolean updatePartInfoById(PartInfo partInfo) {
        return  partyInfoDao.updatePartyById(partInfo) > 0;
    }

}
