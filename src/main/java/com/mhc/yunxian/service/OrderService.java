package com.mhc.yunxian.service;

import com.github.pagehelper.PageInfo;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.admin.OrderDetailsRequest;
import com.mhc.yunxian.bean.pay.RefundMoneyRequest;
import com.mhc.yunxian.dao.model.Coupon;
import com.mhc.yunxian.dao.model.DragonInfo;
import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.dao.model.OrderInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface OrderService {

    List<OrderInfo> getOpenidByDragonNum(String dragonNum);

    int getAllOrder();

    boolean addOrder(OrderInfo orderInfo);

    List<OrderInfo> getOrdersByOrderStatus(String openid ,Integer orderStatus);
    boolean modifyOrderStatusByOrderNumAndOpenid(OrderInfo orderInfo);

    boolean updateOrder(OrderInfo orderInfo);

    boolean updateOrderById(OrderInfo orderInfo);


    List<OrderInfo> getOrderByOpenid(String openid);

    List<OrderInfo> selectOrderByOpenidAndStatus(String openid,Integer orderStatus);

    List<OrderInfo> selectOrderByDragonNumAndStatus(String dragonNum,Integer orderStatus);

    List<OrderInfo> selectCompeletedOrderByDragonNum(String dragonNum);

    List<OrderInfo> getOrderByOpenidAndDragonNum(String openid, String dragonNum);

    List<OrderInfo> getOrderBySellerId(String openid);

    List<OrderInfo> getOrder();

    PageInfo<OrderInfo> selectAdminOrder(OrderDetailsRequest request);

    OrderInfo getOrderByOrderNum(String orderNum);

    void add(CommitOrderRequest request);

    boolean createDifOrder(AddDifOrderRequest request);

    OrderInfo getDifOrderByParentOrderNum(String parentOrderNum);

    BaseResponse refundOrder(RefundMoneyRequest refundMoneyRequest,int state);

    Integer countOrderNumByDragon(String dragonNum);

//    Map refundMoney(RefundMoneyRequest refundMoneyRequest);

    List<OrderInfo> operationStatistics(OperationStatisticsRequest request);

    int countOrderNumber(Map param);

    int countOrderMoney(Map param);

    int countCompleteNumByOpenid(String openid);

    List<OrderStatisticsListDetail> selectOrderStatisticsListDetail(OrderStatisticsListRequest request);


    GetMyUserOrderListResponse getMyUserOrderList(GetMyUserOrderListRequest request);

    DragonHistoryResponse DragonHistory(DragonHistoryRequest request);

	DragonInfo getDragonInfoByCouponUserId(Integer couponUserId);

	List<OrderInfo> selectOrderBySellerIdIndex(String dragonNum);

    BaseResponse checkOrderAmount(Integer couponId, String sessionId, List<GoodsInfo> list);

    GetMyAllOrderResponse listByorderStatus(GetMyAllOrderRequest request);

    OrderDetailResponse detailByOrderNum(OrderDetailRequest request);

    Integer calculateDiscountAmount(Coupon coupon, List<GoodsInfo> list);

    List<OrderInfo> getOrderByDragonNum(String dragonNum);

    Integer getOrderType(String orderNum);
    /**
     * 根据接龙编号统计已完成业务订单数量
     * @param dragonNum
     * @return
     */
    Long countBizOrderByDragonNum(String dragonNum);

    /**
     * 根据订单状态获取订单
     * @param status
     * @return
     */
    List<OrderInfo> getOrdersByStatus(Integer status);
}
