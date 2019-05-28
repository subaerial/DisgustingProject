package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.GoodsInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReceiveDragonRequest extends BaseRequest {

    private String openid;
    private String sessionId;


    /*
    马上接龙的时候校验限购商品情况
    */

    private String dragonNum;

    List<GoodsInfo> goodsList;

}
