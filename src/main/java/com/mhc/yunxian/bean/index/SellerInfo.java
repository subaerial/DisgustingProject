package com.mhc.yunxian.bean.index;

import lombok.Data;

import java.util.List;

@Data
public class SellerInfo {

    private String nickName;

    private String head;

    private int orderNumber;

    List<DragonInfoVO>  dragonInfos;

}
