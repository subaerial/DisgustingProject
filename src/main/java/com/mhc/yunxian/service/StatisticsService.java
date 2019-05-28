package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.dao.model.Shop;

import java.util.List;

/**
 * Created by Administrator on 2018/12/5.
 *
 * @author Alin
 * 用于数据的各类统计
 */
public interface StatisticsService {
    /**
     * 统计复购(接龙)订单数 re-purchase
     */
    Integer countDragonRepurchase(ShopInfoQuery query);

    /**
     * 统计商品的复购次数 re-purchase
     */
    List<GoodsInfoResponse> countGoodsRepurchase(ShopInfoQuery query);

    /**
     * 统计不同截单时间内的接龙人数
     */
    Integer countDragonUsersInCutOffTime();

    /**
     * 统计累计接龙人数
     */
    Integer countDragonAllUsers(String dragonNum);

    /**
     * 统计累计收入
     */
    Integer countDragonAllIncome(ShopInfoQuery query);

    /**
     * 统计不同截单时间段内的收入
     */
    Integer countDragonAllIncomeInCutOffTime();

    /**
     * 统计优惠券的使用人数
     */
    Integer countAlreadyInUseCoupons();

    /**
     * 统计店铺月成交订单量
     * 统计店铺本月销售额
     * 统计店铺复购口碑
     * 统计店铺已提现金额
     * 统计店铺余额(可提现)
     */
    Shop countShopSalesAmountInfo(ShopInfoQuery query);

    /**
     * 从缓存获取复购次数
     *
     * @param shopkeeperOpenId
     * @return
     */
    Long getShopRepurchaseNum(String shopkeeperOpenId);

    /**
     * 同步复购缓存
     *
     * @return
     */
    Boolean syncShopRepurchase();

    /**
     * 更新缓存
     *
     * @param shopkeeperOpenId
     * @return
     */
    Long updateShopRepurchase(String shopkeeperOpenId);


    /**
     * <p> 获取商品复购 </p>
     * @param goodsNum
     * @return Integer
     * @author 昊天 
     * @date  
     * @since V1.1.0-SNAPSHOT
     *
     */
    Integer getGoodsRepurchase(String goodsNum);
    
    /**
     * <p> 更新商品缓存 </p>
     * @param goodsNum
     * @return void
     * @author 昊天 
     * @date  
     * @since V1.1.0-SNAPSHOT
     *
     */
    void updateGoodsRepurchase(String goodsNum);
    
    /**
     * <p> 获得用户管理下的复购缓存 </p>
     * @param buyerOpenId
     * @param sellerOpenId
     * @return Integer
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     *
     */
    Integer getUserRepurchase(String buyerOpenId,String sellerOpenId);

    /**
     * <p> 更新用户管理下复购缓存 </p>
     * @param buyerOpenId
 * @param sellerOpenId
     * @return void
     * @author 昊天 
     * @date  
     * @since V1.1.0-SNAPSHOT
     *
     */
    void updateUserRepurchase(String buyerOpenId,String sellerOpenId);

}
