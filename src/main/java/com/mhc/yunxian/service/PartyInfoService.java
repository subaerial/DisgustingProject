package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.PartInfo;

import java.util.List;

public interface PartyInfoService  {


    PartInfo getPartByOrderNum(String orderNum);

    List<PartInfo> getPartyUser(String dragonNum);

    boolean addPartInfo(PartInfo partInfo);

    boolean existPartInfo(PartInfo partInfo);

    boolean updatePartInfo(PartInfo partInfo);

    boolean updatePartInfoById(PartInfo partInfo);

}
