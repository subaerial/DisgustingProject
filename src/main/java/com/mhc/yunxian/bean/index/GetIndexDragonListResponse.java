package com.mhc.yunxian.bean.index;

import com.mhc.yunxian.bean.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class GetIndexDragonListResponse extends BaseResponse{

    private int totalPartyNumber;//所有参与人数

    private int totalNumber;//接龙总数

    List<SellerInfo> sellerInfos;

}
