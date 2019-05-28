package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.DrawMoney;

import java.util.List;

public interface DrawMoneyDao {
    int deleteByPrimaryKey(Integer id);

    int insert(DrawMoney record);

    int insertSelective(DrawMoney record);

    DrawMoney selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DrawMoney record);

    DrawMoney selectByOpenIdAndFormId(String openId, String formId);

    List<DrawMoney> selectByStatus();

    List<DrawMoney> selectByOpenid(String openid);

    int updateDrawMoney(DrawMoney drawMoney);
}