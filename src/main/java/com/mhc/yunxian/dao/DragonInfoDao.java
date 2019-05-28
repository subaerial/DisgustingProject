package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.DragonOrderCount;
import com.mhc.yunxian.bean.OperationStatisticsRequest;
import com.mhc.yunxian.bean.ShopDragonInfo;
import com.mhc.yunxian.dao.model.DragonInfo;
import com.mhc.yunxian.dao.query.DragonInfoQuery;
import com.mhc.yunxian.vo.CountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DragonInfoDao {
//    int deleteByPrimaryKey(Integer id);
//
//    int insert(DragonInfoVO record);
//
//    int insertSelective(DragonInfoVO record);
//
//    DragonInfoVO selectByPrimaryKey(Integer id);
//
//    int updateByPrimaryKeySelective(DragonInfoVO record);
//
//    int updateByPrimaryKey(DragonInfoVO record);


    int selectAllDragonIndex();

    DragonInfo selectDragonBySubDragonNum(String dragonNum);

    List<DragonInfo> selectAllDragon(Integer pageNum);

    List<DragonInfo> selectDragonByStatusAndOpenid(DragonInfo dragonInfo);

    List<DragonInfo> selectAllDragonInfo();

    int insertDragon(DragonInfo dragonInfo);

    DragonInfo selectDragon(String dragonNum);

    List<DragonInfo> selectDragonByOpenid(String openid);

    List<DragonInfo> selectNoEndDragonByOpenid(String openid);

    int updateDragon(DragonInfo dragonInfo);

    List<DragonInfo> getAllDragonWithEndTime();

    int delDragon(String dragonNum);

    int countSellerNumByTime(OperationStatisticsRequest request);

    List<String> selectAllDragonOpenid();

    //根据接龙状态统计接龙
    List<CountVO> countBy();

    List<String> selectLatestThreeShopOfDragon();

    /**
     * 分页查询最新的接龙
     * @param dragonInfoQuery
     * @return
     */
    List<DragonInfo> queryLatestDragon(DragonInfoQuery dragonInfoQuery);

    /**
     * 获取在进行中的周期行发货的接龙
     * @return
     */
    List<DragonInfo> selectDragonByStatusAndCycle();

    /**
     * 接龙下个人订单统计
     * @param dragonNum
     * @return
     */
    List<DragonOrderCount> selectDragonOrderCountInfo(@Param("dragonNum") String dragonNum);
}