package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.util.List;

@Data
public class GetGoodsInfoRequest extends BaseRequest {

    private List<GoodsInfo> data;

    private String orderNum;

    private Integer money;
}
