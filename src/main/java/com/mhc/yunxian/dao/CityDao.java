package com.mhc.yunxian.dao;

import com.mhc.yunxian.dao.model.City;

import java.util.List;

public interface CityDao {
    int deleteByPrimaryKey(Integer id);

    int insert(City record);

    int insertSelective(City record);

    City selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(City record);

    int updateByPrimaryKey(City record);

    List<City> selectAllProvince();

    List<City> selectByProvince(Integer parentCode);
}