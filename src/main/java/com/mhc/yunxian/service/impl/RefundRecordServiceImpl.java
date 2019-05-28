package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.RefundRecordDao;
import com.mhc.yunxian.dao.model.RefundRecord;
import com.mhc.yunxian.dao.model.RefundRecordExample;
import com.mhc.yunxian.service.RefundRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RefundRecordServiceImpl implements RefundRecordService {

    @Autowired
    RefundRecordDao refundRecordDao;

    @Override
    public void createRefundRecord(RefundRecord refundRecord) {
        refundRecordDao.insertSelective(refundRecord);
    }

    @Override
    public void updateRefundRecord(RefundRecord refundRecord) {
        refundRecordDao.updateByPrimaryKeySelective(refundRecord);
    }

    @Override
    public List<RefundRecord> findAllRefundRecord(Integer orderId) {
        RefundRecordExample example = new RefundRecordExample();
        RefundRecordExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        return refundRecordDao.selectByExample(example);
    }

    @Override
    public RefundRecord findLatestRefundRecord(Integer orderId) {
        List<RefundRecord> list = findAllRefundRecord(orderId);
        if (list.isEmpty()){
            return null;
        }

        long latestCreateTime = 0L;
        RefundRecord latestRecord = null;
        for (RefundRecord refundRecord : list) {
            long createTime = refundRecord.getCreateTime().getTime();
            if (createTime>latestCreateTime){
                latestCreateTime = createTime;
                latestRecord = refundRecord;
            }
        }

        return latestRecord;
    }
}
