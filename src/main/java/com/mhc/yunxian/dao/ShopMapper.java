
package com.mhc.yunxian.dao;


import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.dao.model.Shop;
import groovyjarjarantlr.collections.impl.LList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 本文件由 mybatis-generator 自动生成
 *
 * @author Alin
 */
@Repository
public interface ShopMapper {
    /**
     * 根据主键删除数据库的记录
     *
     * @param shopId
     * @return
     */
    int deleteByPrimaryKey(Long shopId);

    /**
     * 新写入数据库记录
     *
     * @param record
     * @return
     */
    int insert(Shop record);

    /**
     * 动态字段,写入数据库记录
     *
     * @param record
     * @return
     */
    int insertSelective(Shop record);

    /**
     * 根据指定主键获取一条数据库记录
     *
     * @param shopId
     * @return
     */
    Shop selectByPrimaryKey(Long shopId);

    /**
     * 动态字段,根据主键来更新符合条件的数据库记录
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(Shop record);

    /**
     * 根据主键来更新符合条件的数据库记录
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(Shop record);

    /**
     * 根据条件查询店铺信息
     *
     * @param query 查询条件封装对象
     * @return 返回店铺信息列表
     */
    List<Shop> queryShopInfo(ShopInfoQuery query);

    Shop getShopByKeeperOpenid(@Param(value = "openid") String openid);

    List<Shop> queryAll();
}