package com.mhc.yunxian.bean;

import lombok.Data;

import java.util.List;

@Data
public class DragonHistoryResponse extends  BaseResponse{

    private List<DragonHistory> data;
}
