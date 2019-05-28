package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.Banner;
import lombok.Data;

import java.util.List;
/*
* author xuzhongming
* createtime 2018/06/04
*/

@Data
public class BroadcastResponse extends  BaseResponse{

    List<Banner> bannerList;


}
