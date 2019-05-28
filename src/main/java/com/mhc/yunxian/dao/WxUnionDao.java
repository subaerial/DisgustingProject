package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.WxUnion;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WxUnionDao {

    int deleteByPrimaryKey(Integer id);

    int insert(WxUnion record);

    int insertSelective(WxUnion record);

    WxUnion selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WxUnion record);

    int updateByPrimaryKey(WxUnion record);

    void updateByOpenId(WxUnion wxUnion);

    WxUnion findByUnionId(@Param("unionId") String unionid);

    WxUnion findByOpneId(@Param("openId") String fromUserName);
}