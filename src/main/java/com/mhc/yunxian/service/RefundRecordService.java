package com.mhc.yunxian.service;

import com.mhc.yunxian.dao.model.RefundRecord;

import java.util.List;

public interface RefundRecordService {

    void createRefundRecord(RefundRecord refundRecord);

    void updateRefundRecord(RefundRecord refundRecord);

    List<RefundRecord> findAllRefundRecord(Integer orderId);

    RefundRecord findLatestRefundRecord(Integer orderId);

}
