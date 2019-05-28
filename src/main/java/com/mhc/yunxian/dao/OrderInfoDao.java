package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.DragonHistory;
import com.mhc.yunxian.bean.GetMyUserOrder;
import com.mhc.yunxian.bean.OperationStatisticsRequest;
import com.mhc.yunxian.bean.OrderStatisticsListDetail;
import com.mhc.yunxian.bean.OrderStatisticsListRequest;
import com.mhc.yunxian.bean.admin.OrderDetailsRequest;
import com.mhc.yunxian.dao.model.DragonInfo;
import com.mhc.yunxian.dao.model.OrderInfo;
import com.mhc.yunxian.vo.CountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderInfoDao {
//    int deleteByPrimaryKey(Integer id);
//
//    int insert(OrderInfo record);
//
//
//
//    OrderInfo selectByPrimaryKey(Integer id);
//
//    int updateByPrimaryKeySelective(OrderInfo record);
//
//    int updateByPrimaryKey(OrderInfo record);


	OrderInfo selectOrderByOrderNum(String orderNum);

	OrderInfo selectOrderByParentOrderNum(String parentOrderNum);

	List<OrderInfo> selectOrder(String dragonNum);

	int selectAllOrder();

	int selectAllOrderIndex();

	int insertSelective(OrderInfo record);

	List<OrderInfo> getOrdersByOrderStatus(OrderInfo orderInfo);

	int modifyOrderStatusByOrderNum(OrderInfo orderInfo);

	int updateOrder(OrderInfo orderInfo);

	int updateOrderById(OrderInfo orderInfo);

	List<OrderInfo> selectOrderByOpenid(String openid);

	List<OrderInfo> selectOrderByOpenidAndStatus(String openid, String orderStatus);

	List<OrderInfo> selectOrderByDragonNumAndStatus(String dragonNum, String orderStatus);

	List<OrderInfo> selectCompeletedOrderByDragonNum(String dragonNum);

	List<OrderInfo> selectOrderByOpenidAndDragonNum(Map map);

	List<OrderInfo> selectOrderAll();

	List<OrderInfo> selectOrderBySellerId(String openid);

	List<OrderInfo> selectOrderBySellerIdIndex(String dragonNum);

	List<OrderInfo> selectOrderByOrderStatus(Integer orderStatus);

	List<OrderInfo> selectAdminOrder(OrderDetailsRequest request);

	List<OrderInfo> selectOrderStatistics(OperationStatisticsRequest request);

	int countOrderNumber(Map param);

	int countOrderMoney(Map param);

	int countCompleteNumByOpenid(String openid);

	List<OrderStatisticsListDetail> selectOrderStatisticsListDetail(OrderStatisticsListRequest request);

	List<GetMyUserOrder> selectMyUserOrder(Map map);

	List<DragonHistory> selectOrderDetailByDragonNum(String dragonNum);


	DragonInfo getDragonInfoByCouponUserId(@Param("couponUserId") Integer couponUserId);

    List<CountVO> countBy();

	/**
	 * 根据接龙编号和订单状态查询订单列表
	 * 若  orderStatus 为空 则查询该dragonNum下所有订单
	 * @param dragonNum	必须
	 * @param orderStatus	非必须
	 * @return
	 */
	List<OrderInfo> queryDragonOrdersByOrderStatus(String dragonNum, Integer orderStatus);

	/**
	 * 根据接龙编号的集合查询该接龙集合下的所有订单
	 *
	 * @param dragonNumList
	 * @return
	 */
	List<OrderInfo> queryByDragonNums(List<String> dragonNumList);

	/**
	 * 统计已完成的业务订单数量
	 * @param dragonNum
	 * @return
	 */
    Long countBizOrderByDragonNum(@Param("dragonNum") String dragonNum);

	/**
	 * 运营统计V2.0
	 * 查询订单
	 */
	List<OrderInfo> getOrdersByCondition(OperationStatisticsRequest request);
}