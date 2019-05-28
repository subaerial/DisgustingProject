package com.mhc.yunxian.controller;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.admin.*;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.DragonAddrDao;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.SystemParamDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.IsWhiteEnum;
import com.mhc.yunxian.enums.MoneyRecordEnum;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.enums.VersionEnum;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.DateUtils;
import com.mhc.yunxian.utils.JsonUtils;
import com.mhc.yunxian.utils.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 后台管理
 */
@RestController
@Slf4j
@RequestMapping("/")
public class ManagerController {

    @Autowired
    WxUserService wxUserService;

    @Autowired
    OrderService orderService;

    @Autowired
    PartyInfoService partyInfoService;

    @Autowired
    DragonService dragonService;

    @Autowired
    AdminService adminService;

    @Autowired
    OrderGoodsService orderGoodsService;

    @Autowired
    GoodsInfoService goodsInfoService;

    @Autowired
    SendAddrService sendAddrService;

    @Autowired
    BalanceService balanceService;

    @Autowired
    TaskService taskService;

    @Autowired
    MoneyRecordService moneyRecordService;

    @Autowired
    SystemParamDao systemParamDao;

    @Autowired
    WxService wxService;

    @Autowired
    private DragonShowService dragonShowService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private DragonAddrDao dragonAddrDao;

    @Autowired
    private OrderInfoDao orderInfoDao;

    /**
     * 后台管理员登陆
     *
     * @param managerLoginRequest
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public ManagerLoginResponse login(@RequestBody ManagerLoginRequest managerLoginRequest, HttpServletRequest request,
                                      HttpServletResponse response) {

        final ManagerLoginResponse managerLoginResponse = new ManagerLoginResponse();


        if (!adminService.login(managerLoginRequest)) {
            return (ManagerLoginResponse) managerLoginResponse.build(RespStatus.SYSTEM_ERROR);
        }

        request.getSession().setAttribute("userName", managerLoginRequest.getUsername());
        request.getSession().setMaxInactiveInterval(24 * 60 * 60 * 30);
        log.info("session: " + request.getSession().getAttribute("userName"));
        return managerLoginResponse;
    }

    /**
     * 登出
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/admin/loginOut", method = RequestMethod.POST)
    public BaseResponse loginOut(HttpServletRequest request, HttpServletResponse response) {
        final BaseResponse result = new BaseResponse();
        HttpSession session = request.getSession(false);//防止创建Session
        if (session == null || session.getAttribute("userName") == null) {
            return result.build(RespStatus.NOT_LONGIN);
        }
        log.info("session: " + session.getAttribute("userName"));
        session.removeAttribute("userName");
        if (session.getAttribute("userName") != null) {
            return result.build(RespStatus.SYSTEM_ERROR);
        }
        return result;
    }

    /**
     * 用户列表页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/userList", method = RequestMethod.POST)
    public FindWxUserResponse findWxUser(@RequestBody FindWxUserRequest request) {
        final FindWxUserResponse response = new FindWxUserResponse();

        //按照注册时间查询用户
        PageHelper.startPage(request.getPage(), request.getSize());
        final List<WxUser> wxUserList = wxUserService.getAllUser(request);
        PageInfo<WxUser> pageInfo = new PageInfo<>(wxUserList);


        if (null == wxUserList) {
            return (FindWxUserResponse) response.build(RespStatus.SYSTEM_ERROR);
        }


        final List<FindWxUser> list = Lists.newArrayList();

        for (WxUser user : pageInfo.getList()) {
            int totalOrder = 0;//所有参与我发布的所有接龙的订单数量


            List<DragonInfo> dragonInfoList = dragonService.getDragonByOpenid(user.getOpenid());
            if (null == dragonInfoList) {
                return (FindWxUserResponse) response.build(RespStatus.SYSTEM_ERROR);
            }

            for (DragonInfo dragonInfo : dragonInfoList) {
                final List<OrderInfo> orderInfoList = orderService.getOpenidByDragonNum(dragonInfo.getDragonNum());
                if (null == orderInfoList) {
                    return (FindWxUserResponse) response.build(RespStatus.SYSTEM_ERROR);
                }
                totalOrder += orderInfoList.size();

            }

            List<OrderInfo> orderInfoList = orderService.getOrderByOpenid(user.getOpenid());
            if (null == orderInfoList) {
                return (FindWxUserResponse) response.build(RespStatus.SYSTEM_ERROR);
            }

            final Balance balance = balanceService.getBalance(user.getOpenid());
            if (balance == null) {
                return (FindWxUserResponse) response.build(RespStatus.SYSTEM_ERROR);
            }

            final FindWxUser findWxUser = new FindWxUser();
            findWxUser.setLastLoginTime(user.getUpdateTime());
            findWxUser.setOpenid(user.getOpenid());
            findWxUser.setPhone(user.getPhone());
            findWxUser.setRegTime(user.getCreateTime());
            findWxUser.setUserStatus(user.getUserStatus());
            findWxUser.setMyUserOrderNumber(totalOrder);
            findWxUser.setMyOrderNumber(orderInfoList.size());
            findWxUser.setMoney(balance.getBalance());
            findWxUser.setIsWhite(user.getIsWhite());
            findWxUser.setNickName(user.getNickName());

            list.add(findWxUser);
        }


        response.setData(list);

        response.setTotal(pageInfo.getTotal());

        return response;
    }

    /**
     * 修改用户状态接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/chageStatus", method = RequestMethod.POST)
    public ChageUserStatusResponse chageUserStatus(@RequestBody ChageUserStatusRequest request) {
        final ChageUserStatusResponse response = new ChageUserStatusResponse();
        final WxUser wxUser = new WxUser();
        wxUser.setUserStatus(1);//禁用
        wxUser.setOpenid(request.getOpenid());

        if (!wxUserService.updateUserByOpenid(wxUser)) {
            return (ChageUserStatusResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }


    /**
     * 修改用户白名单状态接口V3
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/v3/addWhiteUser", method = RequestMethod.POST)
    public BaseResponse addWhiteUser(@RequestBody ChangeUserWhiteRequest request) {
        final BaseResponse response = new BaseResponse();
        final WxUser wxUser = new WxUser();
        wxUser.setIsWhite(request.getIsWhite());
        wxUser.setOpenid(request.getOpenid());

        if (!wxUserService.updateUserByOpenid(wxUser)) {
            return response.build(RespStatus.SYSTEM_ERROR);
        } else {
            // 添加/移除白名单成功后生成/更新shop信息
            insertOrUpdateShopInfo(request.getOpenid(), request.getIsWhite());
        }

        return response;
    }

    /**
     * 加入白名单时先查询该用户之前是否是店主
     * 是:	更新店铺状态
     * 不是:		插入新增的店主信息
     *
     * @param openId
     * @param isWhite
     */
    private void insertOrUpdateShopInfo(String openId, Integer isWhite) {
        ShopInfoQuery shopInfoQuery = new ShopInfoQuery();
        shopInfoQuery.setShopkeeperOpenId(openId);
        List<Shop> shops = shopMapper.queryShopInfo(shopInfoQuery);
        WxUser user = wxUserService.getWxUser(openId);

        if (CollectionUtils.isEmpty(shops)) {
            Shop shopInfo = new Shop();
            shopInfo.setShopName(user.getNickName());
            shopInfo.setShopkeeperOpenId(user.getOpenid());
            shopInfo.setShopkeeperNickname(user.getNickName());
            shopInfo.setShopHeadPicture(user.getHeadImgUrl());
            shopInfo.setGmtCreate(new Date());
            shopInfo.setIsDeleted(false);
            shopInfo.setShopIntro(user.getNickName() + "的小店~");
            List<DragonAddr> dragonAddrs = dragonAddrDao.selectAllAndOrderByCreateTimeDesc(user.getOpenid());
            ShopInfoQuery query = new ShopInfoQuery();
            query.setShopkeeperOpenId(user.getOpenid());
            Shop shop = statisticsService.countShopSalesAmountInfo(query);
            if (shop != null) {
                shopInfo.setGmtCreate(new Date());
                shopInfo.setAlreadyWithdrawCash(shop.getAlreadyWithdrawCash());
                shopInfo.setRepurchaseOrderCount(shop.getRepurchaseOrderCount());
                shopInfo.setMonthlySales(shop.getMonthlySales());
                shopInfo.setMonthlyFinishedOrder(shop.getMonthlyFinishedOrder());
                shopInfo.setShopFinishedOrder(Optional.ofNullable(shop.getShopFinishedOrder()).orElse(0L));
                shopInfo.setTodayFinishedOrder(Optional.ofNullable(shop.getTodayFinishedOrder()).orElse(0L));
                shopInfo.setTurnoverToday(Optional.ofNullable(shop.getTurnoverToday()).orElse(0));
            }
            if (CollectionUtils.isNotEmpty(dragonAddrs)) {
                DragonAddr dragonAddr = dragonAddrs.get(0);
                shopInfo.setShowAddr(dragonAddr.getAddr());
                shopInfo.setLatitude(dragonAddr.getLatitude());
                shopInfo.setLongitude(dragonAddr.getLongitude());
            }
            int i = shopMapper.insertSelective(shopInfo);
            if (i < 1) {
                log.error("插入店铺状态失败!新增店主信息{}", JsonUtils.toJson(user));
            }
        } else {
            // 更新shop状态
            Shop shop = shops.get(0);
            shop.setStatus(isWhite);
            int i = shopMapper.updateByPrimaryKeySelective(shop);
            if (i < 1) {
                log.error("更新店铺状态失败!店主信息{}", JsonUtils.toJson(user));
            }
        }
    }

    /**
     * 订单详情列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/orderList", method = RequestMethod.POST)
    public OrderDetailsResponse orderDetails(@RequestBody OrderDetailsRequest request) {
        final OrderDetailsResponse response = new OrderDetailsResponse();

        PageInfo<OrderInfo> pageInfo = orderService.selectAdminOrder(request);


        if (pageInfo.getList() == null) {
            return (OrderDetailsResponse) response.build(RespStatus.ORDER_NOT_EXIST);
        }

        final List<OrderDetails> list = Lists.newArrayList();


        for (OrderInfo orderInfo : pageInfo.getList()) {

            final DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());
            if (null == dragonInfo) {
                continue;
            }

            final List<OrderGoodsInfo> orderGoodsInfoList = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
            if (null == orderGoodsInfoList) {
                continue;
            }
            final List<Goods> goodsList = Lists.newArrayList();
            for (OrderGoodsInfo ogInfo : orderGoodsInfoList) {
                final Goods goods = new Goods();
                goods.setGoodsImgs(ogInfo.getGoodsImg());
                goods.setTotalNumber(ogInfo.getBuyNumber());
                goods.setPrice(ogInfo.getPrice());
                goods.setGoodsName(ogInfo.getGoodsName());

                goodsList.add(goods);
            }

            WxUser sellerInfo = wxUserService.getWxUser(dragonInfo.getOpenid());

            WxUser buyerInfo = wxUserService.getWxUser(orderInfo.getOpenid());

            if (null == sellerInfo || null == buyerInfo) {
                return (OrderDetailsResponse) response.build(RespStatus.SYSTEM_ERROR);
            }

            final OrderDetails orderDetails = new OrderDetails();
            orderDetails.setSellerPhone(dragonInfo.getPhone());//卖家手机号
            orderDetails.setSellerOpenid(dragonInfo.getOpenid());//卖家openid
            orderDetails.setBuyerOpenid(orderInfo.getOpenid());//买家openid
            orderDetails.setBuyerPhone(orderInfo.getAddrPhone());//买家手机号
            orderDetails.setGoodsTypeNumber(orderGoodsInfoList.size());//商品种类数量
            orderDetails.setGoodsList(goodsList);//商品信息列表
            orderDetails.setOrderStatus(orderInfo.getOrderStatus());//订单状态
            orderDetails.setOrderMoney(orderInfo.getOrderMoney());//实付金额
            orderDetails.setCommitOrderTime(orderInfo.getCreateTime());//下单时间
            orderDetails.setPayTime(orderInfo.getUpdateTime());//付款时间
            orderDetails.setSendTime(orderInfo.getSendTime());//发货时间
            orderDetails.setComfirmTime(orderInfo.getComfirmTime());//确认收货时间
            orderDetails.setAddr(orderInfo.getAddress());//发货地址
            orderDetails.setOrderNum(orderInfo.getOrderNum());
            orderDetails.setIsCod(orderInfo.getIsCod());//是否货到付款
            orderDetails.setUserName(orderInfo.getAddrName());//快递姓名
            orderDetails.setDragonAddr(orderInfo.getDragonAddr());//自提地址
            orderDetails.setIsDelivery(orderInfo.getIsDelivery());
            orderDetails.setBuyerName(buyerInfo.getNickName());
            orderDetails.setSellerName(sellerInfo.getNickName());

            list.add(orderDetails);

        }
        response.setTotal(pageInfo.getTotal());

        response.setData(list);

        return response;
    }

    /**
     * 修改订单状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/updateOrder", method = RequestMethod.POST)
    public UpdateOrderResponse updateOrder(@RequestBody UpdateOrderRequest request) throws Exception {
        final UpdateOrderResponse response = new UpdateOrderResponse();
        final OrderInfo orderInfo = orderService.getOrderByOrderNum(request.getOrderNum());
        final DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());
        PartInfo partInfo = partyInfoService.getPartByOrderNum(request.getOrderNum());

        WxUser wxUser = wxUserService.getWxUser(orderInfo.getOpenid());

        if (wxUser == null || partInfo == null || dragonInfo == null || orderInfo == null) {
            return (UpdateOrderResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        if (request.getOrderStatus().equals(OrderStatusEnum.PENDING_RECEIVED.getCode())) {
            orderInfo.setSendTime(new Date());
        }

        if (request.getOrderStatus().equals(OrderStatusEnum.COMPELETED.getCode())) {
            orderInfo.setComfirmTime(new Date());
        }


        orderInfo.setOrderStatus(request.getOrderStatus());
        if (!orderService.updateOrder(orderInfo)) {
            return (UpdateOrderResponse) response.build(RespStatus.SYSTEM_ERROR);
        } else {
            response.setCode(200);
            response.setMsg("ok");
            if (request.getOrderStatus().equals(OrderStatusEnum.PENDING_RECEIVED.getCode()) && orderInfo.getParentOrderNum() == null) {//发货,补差订单不发通知

                if (request.getIsSendMsg() == 1) {
                    SendSmsResponse sendSmsResponse = null;
                    try {
                        if (StringUtils.isNotBlank(orderInfo.getDragonAddr())) {
                            sendSmsResponse = SendMessageUtil.sendSmsDeliver(orderInfo.getAddrPhone(), dragonInfo.getDragonTitle(), orderInfo.getDragonAddr());
                        } else {
                            sendSmsResponse = SendMessageUtil.sendSmsDeliver(orderInfo.getAddrPhone(), dragonInfo.getDragonTitle(), orderInfo.getAddress());
                        }

                        taskService.noticeOfDelivery(orderInfo, partInfo.getFormId().split(",")[0]);

                    } catch (Exception exce) {
                        exce.printStackTrace();
                        log.error("短信发送失败:" + orderInfo.getAddrPhone());
                        log.error("msg:" + exce);
                    }

                    if (!sendSmsResponse.getCode().equals("OK")) {
                        log.error("短信发送失败:" + orderInfo.getAddrPhone());
                        log.error("msg:" + sendSmsResponse.getMessage());
                    }
                }


                response.setMsg("发货成功");
                return response;
            } else if (request.getOrderStatus().equals(OrderStatusEnum.COMPELETED.getCode())) {//买家确认收货，钱款进入卖家账户

                List<SystemParam> params = systemParamDao.selectByParamGroup("endTime");

                if (orderInfo.getUpdateTime().getTime() < DateUtils.convertString2Date(params.get(0).getParamValue()).getTime()) {
                    //老数据
                    //不更改余额，不用更新交易记录表

                } else {

//                    MoneyRecord moneyRecord = moneyRecordService.selectRecordByOrderNum(request.getOrderNum());
//
//                    Integer income = moneyRecord.getMoney();
//
//                    //更新我的余额  买家收货后钱款进入卖家账户
//                    final Balance balance1 = balanceService.getBalance(dragonInfo.getOpenid());
//                    balance1.setBalance(balance1.getBalance() + income);
//                    balance1.setUpdateTime(new Date());
//                    if (!balanceService.updateBalanceByOpenid(balance1)) {
//                        throw new DataException("更新余额错误，支付回滚");
//                    }
//
//                    moneyRecord.setRecordType(MoneyRecordEnum.IMCOME.getCode());
//
//                    moneyRecord.setBalance( balance1.getBalance()+"");
//
//                    moneyRecord.setCreateTime(new Date());
//
//                    moneyRecord.setCause(MoneyRecordEnum.IMCOME.getMsg()+"-"+moneyRecord.getOrderNum());
//
//                    if (!moneyRecordService.updateByOrderNum(moneyRecord)) {
//                        throw new DataException("更新交易记录表错误，回滚");
//                    }
                }


                //扣除交易费
//				moneyRecord.setId(null);
//				moneyRecord.setRecordType(MoneyRecordEnum.TRANSACTION_COST.getCode());
//				moneyRecord.setCause(MoneyRecordEnum.TRANSACTION_COST.getMsg());
//				moneyRecord.setBalance((Integer.valueOf(moneyRecord.getBalance())- Constants.SERVICE_CHARGE)+"");
//				moneyRecord.setMoney(Constants.SERVICE_CHARGE);
//				moneyRecord.setCreateTime(new Date());
//
//				if (!moneyRecordService.add(moneyRecord)) {
//					throw new DataException("更新交易记录表错误，回滚");
//				}

            } else if (request.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())) {//买家申请退款

                if (request.getIsSendMsg() == 1) {
                    SendSmsResponse sendSmsResponse = null;
                    try {
                        sendSmsResponse = SendMessageUtil.sendSmsRefund(dragonInfo.getPhone(), wxUser.getNickName(), dragonInfo.getDragonTitle());
                    } catch (ClientException exce) {
                        log.error("短信发送失败:" + orderInfo.getAddrPhone());
                        log.error("msg:" + exce);
                    }

                    if (!sendSmsResponse.getCode().equals("OK")) {
                        log.error("短信发送失败:" + orderInfo.getAddrPhone());
                        log.error("msg:" + sendSmsResponse.getMessage());
                    }
                }

            }

            return response;
        }
    }


    /**
     * 余额明细表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/balanceDetailList", method = RequestMethod.POST)
    public GetMoneyRecordListResponse balanceDetailList(@RequestBody GetMoneyRecordListRequest request) {
        final GetMoneyRecordListResponse response = new GetMoneyRecordListResponse();

        PageHelper.startPage(request.getPage(), request.getSize());
        final List<MoneyRecord> list = moneyRecordService.getRecordList(request.getOpenid());

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
     * 运营统计
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/operationStatistics", method = RequestMethod.POST)
    public OperationStatisticsResponse operationStatistics(@RequestBody OperationStatisticsRequest request) {
        OperationStatisticsResponse response = new OperationStatisticsResponse();


        log.info("******" + new Date());

        List<OrderInfo> orderInfos = orderService.operationStatistics(request);

        log.info("******" + new Date());


        int allMoney = 0;

        for (OrderInfo e : orderInfos) {
            allMoney += e.getOrderMoney();
        }

        log.info("******" + new Date());


        response.setOrderMoney(allMoney);
        response.setOrderNumber(orderInfos.size());


        if (request.getSellerName() == null && request.getBuyerName() == null) {

            response.setRegisterNumber(wxUserService.countRegister(request));
            response.setSellerNumber(dragonService.countSellerNumByTime(request));

        }


        return response;

    }

    /**
     * 运营统计(V2.0)
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/admin/v20/operationStatistics", method = RequestMethod.POST)
    public OperationStatisticsResponse statistics(@RequestBody OperationStatisticsRequest request) {
        OperationStatisticsResponse response = new OperationStatisticsResponse();

        List<OrderInfo> orderInfos = orderService.operationStatistics(request);
        int allMoney = 0;
        for (OrderInfo e : orderInfos) {
            allMoney += e.getOrderMoney();
        }
        response.setOrderMoney(allMoney);
        response.setOrderNumber(orderInfos.size());
        if (request.getSellerName() == null && request.getBuyerName() == null) {
            response.setRegisterNumber(wxUserService.countRegister(request));
            response.setSellerNumber(dragonService.countSellerNumByTime(request));
        }
        /**
         * 时间必须满足年月日才能计算周的销售额
         */
        String year = request.getYear();
        String month = request.getMonth();
        // day为周一
        String day = request.getDay();

        if (StringUtils.isNotBlank(day) && StringUtils.isNotBlank(month) && StringUtils.isNotBlank(year)) {
            // 获取指定周的周一0点的时间 √
            Calendar calendar = this.getAssignWeekMondayDate(year, month, day);
            Date mondayMorning = calendar.getTime();
            // 获取指定周的上周一0点的时间
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            Date assignLastWeekMondayDate = calendar.getTime();
            // 指定周的下周一零点
            calendar.add(Calendar.DAY_OF_MONTH, 14);
            Date assignWeekEndDate = calendar.getTime();
            log.info("获取指定周的周一0点的时间:{},获取指定周的上周一0点的时间:{},指定周的下周一零点:{}", DateUtil.SDF.format(mondayMorning), DateUtil.SDF.format(assignLastWeekMondayDate), DateUtil.SDF.format(assignWeekEndDate));
            OperationStatisticsRequest query = new OperationStatisticsRequest();
            query.setBuyerName(request.getBuyerName());
            query.setSellerName(request.getSellerName());
            query.setBeginDate(assignLastWeekMondayDate);
            query.setEndDate(mondayMorning);
            // 上周订单
            List<OrderInfo> ordersOfLastWeek = orderInfoDao.getOrdersByCondition(query);
            // 更改查询条件查询指定周内的订单
            query.setBeginDate(mondayMorning);
            // 结束时间为指定周周日时间
            query.setEndDate(assignWeekEndDate);
            // 本周订单
            List<OrderInfo> ordersOfWeek = orderInfoDao.getOrdersByCondition(query);

            // 设置本周订单数
            response.setOrderCountOfWeek(ordersOfWeek.size());
            // 设置上周订单数
            response.setOrderCountOfLastWeek(ordersOfLastWeek.size());

            if (CollectionUtils.isNotEmpty(ordersOfWeek)) {
                // 计算本周销售额
                Integer integer = ordersOfWeek.stream().map(OrderInfo::getOrderMoney).reduce((a, b) -> a + b).orElse(0);
                response.setSalesAmountOfWeek(integer);
            } else {
                response.setSalesAmountOfWeek(0);
            }
            if (CollectionUtils.isNotEmpty(ordersOfLastWeek)) {
                // 计算上周销售额
                Integer integer = ordersOfLastWeek.stream().map(OrderInfo::getOrderMoney).reduce((a, b) -> a + b).orElse(0);
                response.setSalesAmountOfLastWeek(integer);
            } else {
                response.setSalesAmountOfLastWeek(0);
            }
            // 获取上周一至本周一注册的用户数
            request.setBeginDate(assignLastWeekMondayDate);
            request.setEndDate(mondayMorning);
            List<WxUser> usersOfLastWeek = wxUserService.getUserByDate(request);
            if (CollectionUtils.isNotEmpty(usersOfLastWeek)) {
                // 抽取上周商家用户
                List<WxUser> sellersOfLastWeek = usersOfLastWeek.stream().filter(user -> IsWhiteEnum.IS_WHITE.getCode().equals(Optional.ofNullable(user.getIsWhite()).orElse(0)))
                        .collect(Collectors.toList());
                response.setRegisterCountOfLastWeek(usersOfLastWeek.size() - sellersOfLastWeek.size());
                response.setSellerCountOfLastWeek(sellersOfLastWeek.size());
            } else {
                response.setRegisterCountOfLastWeek(0);
                response.setSellerCountOfLastWeek(0);
            }
            // 获取本周一到现在的注册用户数
            request.setBeginDate(mondayMorning);
            request.setEndDate(assignWeekEndDate);

            List<WxUser> usersOfWeek = wxUserService.getUserByDate(request);
            if (CollectionUtils.isNotEmpty(usersOfWeek)) {
                // 抽取本周注册的商家用户
                List<WxUser> sellersOfWeek = usersOfWeek.stream().filter(user -> IsWhiteEnum.IS_WHITE.getCode().equals(Optional.ofNullable(user.getIsWhite()).orElse(0)))
                        .collect(Collectors.toList());
                response.setRegisterCountOfWeek(usersOfWeek.size() - sellersOfWeek.size());
                response.setSellerCountOfWeek(sellersOfWeek.size());
            } else {
                response.setRegisterCountOfWeek(0);
                response.setSellerCountOfWeek(0);
            }
        }

        return response;

    }

    /**
     * 获取指定年月日的周一凌晨0点
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    private Calendar getAssignWeekMondayDate(String year, String month, String day) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, Integer.valueOf(year));
        instance.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        instance.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        return instance;
    }

    /**
     * 修改密码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/changePwd", method = RequestMethod.POST)
    public BaseResponse changePwd(@RequestBody AdminChangePwdRequest request) {
        BaseResponse response = new BaseResponse();

        if (request == null || StringUtils.isBlank(request.getUsername()) ||
                StringUtils.isBlank(request.getOldPwd()) ||
                StringUtils.isBlank(request.getNewPwd())) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        if (!adminService.changePwd(request)) {
            response.build(RespStatus.OLD_PWD_ERROR);
        }

        return response;

    }


    /**
     * 获取关键词列表
     *
     * @return
     */
    @RequestMapping(value = "/admin/getKeywordList", method = RequestMethod.POST)
    public KeywordListResponse getKeywordList(BaseRequest request) {

        KeywordListResponse response = new KeywordListResponse();

        try {
            PageHelper.startPage(request.getPage(), request.getSize());
            response.setKeywordList(wxService.getKeywordList());

        } catch (Exception e) {
            return (KeywordListResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;

    }


    /**
     * 添加关键词
     *
     * @return
     */
    @RequestMapping(value = "/admin/addKeyword", method = RequestMethod.POST)
    public BaseResponse addKeyword(WxKeywordRequest request) {
        BaseResponse response = new BaseResponse();

        try {
            WxAutoReply wxAutoReply = wxService.selectByKeyword(request.getKeyword());

            if (wxAutoReply != null) {
                response.build(RespStatus.KEYWORD_EXIST);
            }

            wxAutoReply = new WxAutoReply();

            wxAutoReply.setKeyword(request.getKeyword());
            wxAutoReply.setMessage(request.getMessage());

            wxService.insertWxAutoReply(wxAutoReply);

        } catch (Exception e) {
            log.error(e.getMessage());
            return response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 删除关键词
     *
     * @return
     */
    @RequestMapping(value = "/admin/deleteKeyword", method = RequestMethod.POST)
    public BaseResponse deleteKeyword(WxKeywordRequest request) {
        BaseResponse response = new BaseResponse();

        try {
            WxAutoReply wxAutoReply = wxService.selectById(request.getId());

            if (wxAutoReply != null) {
                response.build(RespStatus.KEYWORD_EXIST);
            } else {
                wxService.deleteWxAutoReply(request.getId());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /*获取当前首页显示状态（获取审核状态）*/
    @RequestMapping(value = "/admin/getShowStatusForRelease", method = RequestMethod.GET)
    public GetDragonShowStatusRespone getShowStatusForRelease() {
        GetDragonShowStatusRespone respone = new GetDragonShowStatusRespone();
        Integer showStatus = dragonShowService.selectDragonShow(VersionEnum.RELEASE.getCode());
        respone.setShowStatus(showStatus);
        return respone;
    }

    /*修改审核状态（首页显示状态）*/
    @RequestMapping(value = "/admin/updateShowDragonOfRelease", method = RequestMethod.POST)
    public BaseResponse updateShowDragonOfRelease(@RequestBody UpdateShowDragonRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        dragonShowService.updateDragonShow(request.getShowStatus(), VersionEnum.RELEASE.getCode());
        return baseResponse;
    }

    /*获取当前首页显示状态（获取审核状态）*/
    @RequestMapping(value = "/admin/getShowStatusForProduct", method = RequestMethod.GET)
    public GetDragonShowStatusRespone getShowStatusForProduct() {
        GetDragonShowStatusRespone respone = new GetDragonShowStatusRespone();
        Integer showStatus = dragonShowService.selectDragonShow(VersionEnum.PRODUCT.getCode());
        respone.setShowStatus(showStatus);
        return respone;
    }

    /*修改审核状态（首页显示状态）*/
    @RequestMapping(value = "/admin/updateShowDragonOfProduct", method = RequestMethod.POST)
    public BaseResponse updateShowDragonOfProduct(@RequestBody UpdateShowDragonRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        dragonShowService.updateDragonShow(request.getShowStatus(), VersionEnum.PRODUCT.getCode());
        return baseResponse;
    }


}
