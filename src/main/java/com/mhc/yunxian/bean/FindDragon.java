package com.mhc.yunxian.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FindDragon implements Serializable {

    private String headImg;
    private String nickName;
    private List<Goods> list;//购买商品信息
    private String dragonAddr;


}
