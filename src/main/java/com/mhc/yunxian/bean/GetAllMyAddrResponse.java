package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.SendAddr;
import lombok.Data;

import java.util.List;

@Data
public class GetAllMyAddrResponse extends BaseResponse {


    List<SendAddr> data;
}
