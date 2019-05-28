package com.mhc.yunxian.dao;


import com.mhc.yunxian.dao.model.RefundRecord;
import com.mhc.yunxian.dao.model.RefundRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RefundRecordDao {
    int countByExample(RefundRecordExample example);

    int deleteByExample(RefundRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RefundRecord record);

    int insertSelective(RefundRecord record);

    List<RefundRecord> selectByExample(RefundRecordExample example);

    RefundRecord selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RefundRecord record, @Param("example") RefundRecordExample example);

    int updateByExample(@Param("record") RefundRecord record, @Param("example") RefundRecordExample example);

    int updateByPrimaryKeySelective(RefundRecord record);

    int updateByPrimaryKey(RefundRecord record);
}