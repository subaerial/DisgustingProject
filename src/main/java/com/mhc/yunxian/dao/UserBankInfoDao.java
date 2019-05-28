package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.UserBankInfoVO;
import com.mhc.yunxian.dao.model.UserBankInfo;

public interface UserBankInfoDao {
    int deleteByPrimaryKey(Integer id);

    int insert(UserBankInfo record);

    int insertSelective(UserBankInfo record);

    UserBankInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserBankInfo record);

    int updateByPrimaryKey(UserBankInfo record);

    UserBankInfoVO selectUserBankInfoVOByOpenid(String openid);

    UserBankInfo getBankInfo(String openid);
}