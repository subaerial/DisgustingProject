package com.mhc.yunxian.bean;

import com.github.pagehelper.PageInfo;
import com.mhc.yunxian.dao.model.MoneyRecord;
import lombok.Data;

import java.util.List;


@Data
public class GetMoneyRecordListResponse extends BaseResponse {

    PageInfo<MoneyRecord> data;
}
