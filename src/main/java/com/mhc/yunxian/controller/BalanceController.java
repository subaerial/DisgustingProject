package com.mhc.yunxian.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.MoneyRecordEnum;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;
    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private DrawMoneyService drawMoneyService;
    @Autowired
    private OrderService orderService;

    @Autowired
    MoneyRecordService moneyRecordService;

    @Autowired
    OrderGoodsService orderGoodsService;

    @Autowired
    GoodsInfoService goodsInfoService;

    @Autowired
    DragonService dragonService;


    /**
     * 我的用户页面需要返回的数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/getMyBalanceAndOrderCount", method = RequestMethod.POST)
    public GetMyBalanceResponce findBalance(@RequestBody GetMyBalanceRequest request) {
        final GetMyBalanceResponce responce = new GetMyBalanceResponce();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return (GetMyBalanceResponce) responce.build(RespStatus.USER_NOT_EXIST);
        }
        //查询余额
        Balance balance = balanceService.getBalance(wxUser.getOpenid());
        responce.setBalance(balance.getBalance());

        List<OrderInfo> orderInfos = orderService.getOrderBySellerId(wxUser.getOpenid());

        int realBalance = balance.getBalance();

        for (OrderInfo orderInfo : orderInfos) {
            if (orderInfo.getOrderStatus() == OrderStatusEnum.REFUNDING.getCode() ||
                    orderInfo.getOrderStatus() == OrderStatusEnum.PENDING_DELIVERY.getCode() ||
                    orderInfo.getOrderStatus() == OrderStatusEnum.PENDING_RECEIVED.getCode()) {
                realBalance -= orderInfo.getOrderMoney();
            }
        }

        responce.setRealBalance(realBalance);

        List<DragonInfo> dragonInfos = dragonService.getDragonByOpenid(wxUser.getOpenid());

        if (dragonInfos.size() > 0 || wxUser.getIsWhite() == 1) {
            responce.setIsSeller(1);
        }


        //log.info("余额--->"+balance);
        Map<String, Object> map = new HashMap<>();
        if (null == balance) {
            return (GetMyBalanceResponce) responce.build(RespStatus.BLANCE_IS_NULL);
        }
        /**
         * 应急↓
         */
        //查询待付款订单列表
        List<OrderInfo> unpay = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.PENDING_PAYMENT.getCode());
        // log.info("待付款订单列表"+unpay);

        //查询未发货的订单
        List<OrderInfo> unshipped = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.PENDING_DELIVERY.getCode());
        //log.info("未发货的订单"+unshipped);

        //查询已发货的订单
        List<OrderInfo> shipped = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.PENDING_RECEIVED.getCode());
        // log.info("待付款订单列表"+shipped);

        //查询已完成的订单
        List<OrderInfo> finish = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.COMPELETED.getCode());
        // log.info("已完成的订单"+finish);

        //查询退款订单
        List<OrderInfo> refund = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.REFUNDING.getCode());
        // log.info("退款订单列表"+refund);

        map.put("unpay", unpay.size());
        map.put("unshipped", unshipped.size());
        map.put("shipped", shipped.size());
        map.put("finish", finish.size());
        map.put("refund", refund.size());
        responce.setMap(map);
        responce.build(200, "请求成功!");
        return responce;
    }

    /**
     * 我的用户页面需要返回的数据V3
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/v3/getMyBalanceAndOrderCount", method = RequestMethod.POST)
    public GetMyBalanceResponce findBalanceV3(@RequestBody GetMyBalanceRequest request) {
        final GetMyBalanceResponce responce = new GetMyBalanceResponce();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return (GetMyBalanceResponce) responce.build(RespStatus.USER_NOT_EXIST);
        }
        //查询余额
        Balance balance = balanceService.getBalance(wxUser.getOpenid());
        responce.setBalance(balance.getBalance());
        responce.setDragonButIsOpen(wxUser.getDragonButIsOpen());
        responce.setIsWhite(wxUser.getIsWhite());

        List<OrderInfo> orderInfos = orderService.getOrderBySellerId(wxUser.getOpenid());

        int realBalance = balance.getBalance();

        for (OrderInfo orderInfo : orderInfos) {
            if (orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode()) ||
                    orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode()) ||
                    orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_RECEIVED.getCode())) {
                realBalance -= orderInfo.getOrderMoney();
            }
        }

        responce.setRealBalance(realBalance);
        //log.info("余额--->"+balance);
        Map<String, Object> map = new HashMap<>();
        if (null == balance) {
            return (GetMyBalanceResponce) responce.build(RespStatus.BLANCE_IS_NULL);
        }
        /**
         * 应急↓
         */
        //查询待付款订单列表
        List<OrderInfo> unpay = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.PENDING_PAYMENT.getCode());
        // log.info("待付款订单列表"+unpay);

        //查询未发货的订单
        List<OrderInfo> unshipped = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.PENDING_DELIVERY.getCode());
        //log.info("未发货的订单"+unshipped);

        //查询已发货的订单
        List<OrderInfo> shipped = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.PENDING_RECEIVED.getCode());
        // log.info("待付款订单列表"+shipped);

        //查询已完成的订单
        List<OrderInfo> finish = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.COMPELETED.getCode());
        // log.info("已完成的订单"+finish);

        //查询退款订单
        List<OrderInfo> refund = orderService.getOrdersByOrderStatus(wxUser.getOpenid(), OrderStatusEnum.REFUNDING.getCode());
        // log.info("退款订单列表"+refund);

        map.put("unpay", unpay.size());
        map.put("unshipped", unshipped.size());
        map.put("shipped", shipped.size());
        map.put("finish", finish.size());
        map.put("refund", refund.size());
        responce.setMap(map);
        responce.build(200, "请求成功!");
        return responce;
    }

    /**
     * 提现明细
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/DrawMoneyList", method = RequestMethod.POST)
    public GetMyDrawMoneyDetailResponse getMyDrawMoneyDetail(@RequestBody GetMyDrawMoneyDetailRequest request) {
        final GetMyDrawMoneyDetailResponse response = new GetMyDrawMoneyDetailResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        final List<DrawMoney> list = drawMoneyService.getDrawMoneyDetail(wxUser.getOpenid());
        if (0 == list.size()) {
            return (GetMyDrawMoneyDetailResponse) response.build(RespStatus.CURRENT_NOT_DETAIL);
        }
        response.setData(list);
        return response;
    }

    /**
     * 我的余额
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/myBalance", method = RequestMethod.POST)
    public GetMyBalanceResponce getMyBalance(@RequestBody GetMyBalanceRequest request) {
        GetMyBalanceResponce responce = new GetMyBalanceResponce();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        //查询余额
        Balance balance = balanceService.getBalance(wxUser.getOpenid());
        responce.setBalance(balance.getBalance());
        responce.build(200, "ok");
        return responce;
    }

    /**
     * 收入明细
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/v2/incomeDetailList", method = RequestMethod.POST)
    public GetMoneyRecordListResponse getIncomeDetailList(@RequestBody GetMoneyRecordListRequest request) {
        final GetMoneyRecordListResponse response = new GetMoneyRecordListResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (null == wxUser) {
            return (GetMoneyRecordListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        PageHelper.startPage(request.getPage(), request.getSize());
        List<MoneyRecord> list = moneyRecordService.getRecordList(wxUser.getOpenid());

        for (MoneyRecord moneyRecord : list) {
            if (moneyRecord.getRecordType() == MoneyRecordEnum.IMCOME.getCode()) {
                moneyRecord.setStatus(0);
            } else {
                moneyRecord.setStatus(1);
            }
        }


        PageInfo<MoneyRecord> pageInfo = new PageInfo<>(list);


        if (null == list) {
            return (GetMoneyRecordListResponse) response.build(RespStatus.CURRENT_NOT_RECORD);
        }
        response.setData(pageInfo);
        return response;
    }

    /**
     * 展示或发布接龙按钮
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/v3/changeDragonOpenButton", method = RequestMethod.POST)
    public BaseResponse changeDragonOpenButton(@RequestBody ChangeDragonOpenButtonRequest request) {
        final BaseResponse response = new BaseResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        wxUser.setDragonButIsOpen(request.getDragonButIsOpen());
        if (wxUserService.updateUserByOpenid(wxUser)) {
            return response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }


    /**
     * 余额详情
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/balance/detail", method = RequestMethod.POST)
    public MoneyRecordDetailResponse balanceDetail(@RequestBody MoneyRecordDetailRequest request) {
        final MoneyRecordDetailResponse response = new MoneyRecordDetailResponse();

        if (request.getMoneyRecordId() == null) {
            return (MoneyRecordDetailResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        //查找交易记录表
        MoneyRecord moneyRecord = moneyRecordService.selectRecordById(request.getMoneyRecordId());

        if (moneyRecord == null) {
            return (MoneyRecordDetailResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        response.setBalance(Integer.valueOf(moneyRecord.getBalance()));

        response.setCreateTime(moneyRecord.getCreateTime());

        response.setRecordType(moneyRecord.getRecordType());

        if (moneyRecord.getOrderNum() != null) {

            OrderInfo orderInfo = orderService.getOrderByOrderNum(moneyRecord.getOrderNum());

            if (orderInfo == null) {
                return (MoneyRecordDetailResponse) response.build(RespStatus.ORDER_NOT_EXIST);
            }

            WxUser buyerInfo = wxUserService.getWxUser(orderInfo.getOpenid());


            response.setOrderNum(moneyRecord.getOrderNum());

            if (buyerInfo == null) {
                return (MoneyRecordDetailResponse) response.build(RespStatus.SYSTEM_ERROR);
            }

            MoneyRecordDetailResponse.OrderInfo resultOrder = response.getNewOrderInfo();

            resultOrder.setNickName(buyerInfo.getNickName());

            List<OrderGoodsInfo> orderGoodsInfos = orderGoodsService.getOrderGoods(moneyRecord.getOrderNum());

            if (orderGoodsInfos.size() < 1) {
                return (MoneyRecordDetailResponse) response.build(RespStatus.GOODS_NOT_NULL);
            }

            List<MoneyRecordDetailResponse.GoodsInfo> goodsInfos = response.getNewGoodsInfoList();

            for (OrderGoodsInfo e : orderGoodsInfos) {
                MoneyRecordDetailResponse.GoodsInfo goodsInfo = response.getNewGoodsInfo();

                goodsInfo.setBuyNumber(e.getBuyNumber());
                goodsInfo.setGoodsName(e.getGoodsName());

                GoodsInfo good = goodsInfoService.getGoods(e.getGoodsNum());

                if (good == null) {
                    return (MoneyRecordDetailResponse) response.build(RespStatus.GOODS_NOT_NULL);
                }


                goodsInfo.setSpecification(good.getSpecification());

                goodsInfos.add(goodsInfo);
            }

            resultOrder.setGoodsInfos(goodsInfos);

            response.setOrderInfo(resultOrder);


        }

        return response;
    }


}
