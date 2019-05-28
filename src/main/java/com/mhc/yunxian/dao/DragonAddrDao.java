package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.DragonAddr;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DragonAddrDao {

    int insertSelective(DragonAddr record);

    DragonAddr selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DragonAddr record);

    List<DragonAddr> selectByDragonNum(String dragonNum);

    List<DragonAddr> selectAllAddrByOpenid(String openid);

    List<DragonAddr> selectAllAndOrderByCreateTimeDesc(@Param("openid") String openid);

	List<DragonAddr> queryAll();
}