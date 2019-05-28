package com.mhc.yunxian.bean;

import com.github.pagehelper.PageInfo;
import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.util.List;

@Data
public class HistoryGoodsListResponse extends BaseResponse {

    private PageInfo<GoodsInfo> pageInfo;

}
