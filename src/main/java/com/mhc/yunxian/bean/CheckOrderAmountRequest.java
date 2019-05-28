package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.util.List;

@Data
public class CheckOrderAmountRequest {

    private Integer couponId;

    private String sessionId;

    private List<GoodsInfo> goodsList;
}
