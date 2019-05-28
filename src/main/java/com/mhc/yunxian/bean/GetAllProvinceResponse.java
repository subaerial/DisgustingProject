package com.mhc.yunxian.bean;

import com.mhc.yunxian.dao.model.City;
import lombok.Data;

import java.util.List;

@Data
public class GetAllProvinceResponse extends BaseResponse {

    private List<City> cityList;

}
