package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.DrawMoney;
import lombok.Data;

import java.util.List;

@Data
public class GetMyDrawMoneyDetailResponse extends BaseResponse {


    List<DrawMoney> data;

}
