package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MoneyRecordDetailResponse extends BaseResponse{

    private Integer recordType;

    private Date createTime;

    private String orderNum;

    private int balance;

    private OrderInfo orderInfo;



    @Data
    public class OrderInfo{
        private String nickName;

        private List<GoodsInfo> goodsInfos;
    }


    @Data
    public class GoodsInfo{
        private String goodsName;

        private int buyNumber;

        private String specification;
    }

    public OrderInfo getNewOrderInfo(){
        return new OrderInfo();
    }

    public GoodsInfo getNewGoodsInfo(){
        return new GoodsInfo();
    }

    public List<GoodsInfo> getNewGoodsInfoList(){
        return new ArrayList<GoodsInfo>();
    }

}
