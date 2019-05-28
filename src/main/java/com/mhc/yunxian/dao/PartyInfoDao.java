package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.PartInfo;

import java.util.List;

public interface PartyInfoDao {
    int deleteByPrimaryKey(Integer id);

    int insert(PartInfo record);

    int insertSelective(PartInfo record);

    PartInfo selectByPrimaryKey(Integer id);

    PartInfo selectByOrderNum(String orderNum);

    int updateByPrimaryKeySelective(PartInfo record);

    int updateByPrimaryKey(PartInfo record);


    List<PartInfo> selectParty(String dragonNum);

    int insertParty(PartInfo partInfo);

    int updatePartyByOrderNum(PartInfo record);

    int existPartyByOrderNum(PartInfo record);

    int updatePartyById(PartInfo record);

}