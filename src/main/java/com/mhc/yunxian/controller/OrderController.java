package com.mhc.yunxian.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.pay.RefundMoneyRequest;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.CouponUserDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.*;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private DragonService dragonService;
    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private GoodsInfoService goodsInfoService;
    @Autowired
    private OrderGoodsService orderGoodsService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PartyInfoService partyInfoService;
    @Autowired
    MyUserService myUserService;
    @Autowired
    DrawMoneyService drawMoneyService;

    @Autowired
    MoneyRecordService moneyRecordService;

    @Autowired
    BalanceService balanceService;

    @Autowired
    SystemParamService systemParamService;

    @Autowired
    DragonGoodsService dragonGoodsService;

    @Autowired
    RefundRecordService refundRecordService;

    @Autowired
    private CouponUserDao couponUserDao;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private SendStationMsgService sendStationMsgService;

    @Autowired
    private StatisticsService statisticsService;

    private String appid = "wxc38768c84fd437b6";

    private String secret = "638f506b89aa993c2576cc8c2da4b1b3";

    private String mchId = "1504015191";

    private String secretKey = "3233jkdddDDF454Fddddf2seFDEDd3dD";

    private String tradeType = "JSAPI";


    @Value("${wx.yunxian.order.notifyUrl}")
    private String notifyUrl;

    private String signType = "MD5";

    private String unifiedOrderUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    private String wxOrderQueryUrl = "https://api.mch.weixin.qq.com/pay/orderquery";

    private String refundOrderUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    //private String notify_url = "https://www.yunxian.shop/refund/success";


    /**
     * 根据订单状态查询订单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/getOrdersByOrderStatus", method = RequestMethod.POST)
    public GetMyAllOrderResponse getOrdersByOrderStatus(@RequestBody GetMyAllOrderRequest request) {
        return orderService.listByorderStatus(request);
    }


    /**
     * 订单详情
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/getOrderDetail", method = RequestMethod.POST)
    public OrderDetailResponse getOrderDetail(@RequestBody OrderDetailRequest request) {
        return orderService.detailByOrderNum(request);


    }

    /**
     * 修改订单状态 v11
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "yunxian/v11/order/modifyOrderStatusByOrderNum", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, IOException.class, NullPointerException.class, DataException.class})
    public BaseResponse modifyOrderStatusByOrderNum_v11(@RequestBody GetMyBalanceRequest request) throws Exception {
        BaseResponse baseResponse = new BaseResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        OrderInfo order = orderService.getOrderByOrderNum(request.getOrderNum());
        if (order == null) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        // 请求参数传入的订单状态
        Integer status = request.getOrderStatus();
        if (order.getOrderStatus().equals(status) && request.getRefundStatus() == null) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT.getKey(), "该订单已为修改状态，请勿重复修改");
        }

        Integer orderId = order.getId();
        Integer orderStatus = order.getOrderStatus();

        //对订单号加锁，防止并发操作订单
        synchronized (request.getOrderNum().intern()) {
            if (OrderStatusEnum.PENDING_PAYMENT.getCode().equals(order.getOrderStatus())) {
                //原订单为待付款
                if (OrderStatusEnum.PENDING_DELIVERY.getCode().equals(status)) {
                    //修改为待发货
                    order.setOrderStatus(OrderStatusEnum.PENDING_DELIVERY.getCode());
                } else if (OrderStatusEnum.CANCELLED.getCode().equals(status)) {
                    //修改为已取消
                    order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
                    //退红包
                    if (order.getCouponUserId() != null) {
                        //使用红包
                        CouponUser couponUser = couponUserDao.selectByPrimaryKey(order.getCouponUserId());
                        if (couponUser == null) {
                            throw new DataException("红包不存在");
                        }
                        //更新红包为未使用
                        couponUser.setIsUsed(0);
                        couponUserDao.updateByPrimaryKeySelective(couponUser);
                    }
                } else {
                    return baseResponse.build(RespStatus.ILLEGAL_ARGUMENT);
                }
            } else if (OrderStatusEnum.PENDING_DELIVERY.getCode().equals(order.getOrderStatus())) {
                //原订单为待发货
                if (OrderStatusEnum.PENDING_RECEIVED.getCode().equals(status)) {
                    //修改为待收货
                    order.setOrderStatus(OrderStatusEnum.PENDING_RECEIVED.getCode());
                } else if (OrderStatusEnum.REFUNDING.getCode().equals(status)) {

                    log.debug("待发货退款申请：{}", JSONObject.toJSON(order));
                    //生成发起退款记录
                    this.insertRefundRecord(wxUser, orderId, orderStatus);
                    //修改为退款中
                    order.setOrderStatus(OrderStatusEnum.REFUNDING.getCode());
                } else {
                    return baseResponse.build(RespStatus.ILLEGAL_ARGUMENT);
                }
            } else if (OrderStatusEnum.PENDING_RECEIVED.getCode().equals(order.getOrderStatus())) {
                //原订单为待收货
                if (OrderStatusEnum.COMPELETED.getCode().equals(status)) {
                    //修改为已完成
                    order.setOrderStatus(OrderStatusEnum.COMPELETED.getCode());
                } else if (OrderStatusEnum.REFUNDING.getCode().equals(status)) {
                    log.debug("待收货退款申请：{}", JSONObject.toJSON(order));

                    //生成发起退款记录
                    this.insertRefundRecord(wxUser, orderId, orderStatus);
                    //修改为退款中
                    order.setOrderStatus(OrderStatusEnum.REFUNDING.getCode());
                } else {
                    return baseResponse.build(RespStatus.ILLEGAL_ARGUMENT);
                }
            } else if (OrderStatusEnum.COMPELETED.getCode().equals(order.getOrderStatus())) {
                // 订单为已完成
                if (OrderStatusEnum.REFUNDING.getCode().equals(status)) {
                    log.debug("订单完成退款申请：{}", JSONObject.toJSON(order));
                    //生成发起退款记录
                    this.insertRefundRecord(wxUser, orderId, orderStatus);
                    //修改为退款中
                    order.setOrderStatus(OrderStatusEnum.REFUNDING.getCode());
                } else {
                    return baseResponse.build(RespStatus.ILLEGAL_ARGUMENT);
                }
            } else if (OrderStatusEnum.REFUNDING.getCode().equals(order.getOrderStatus())) {
                //订单为退款中
                Integer refundStatus = request.getRefundStatus();
                RefundRecord latestRefundRecord = refundRecordService.findLatestRefundRecord(orderId);
                if (latestRefundRecord == null) {
                    throw new DataException("查询最新退款申请记录失败");
                }
                latestRefundRecord.setStatus(refundStatus);
                latestRefundRecord.setUpdateTime(new Date());
                refundRecordService.updateRefundRecord(latestRefundRecord);
                order.setOrderStatus(latestRefundRecord.getOrderStatus());
            } else {
                return baseResponse.build(RespStatus.ILLEGAL_ARGUMENT);
            }

            DragonInfo dragonInfo = dragonService.getDragon(order.getDragonNum());
            PartInfo partInfo = partyInfoService.getPartByOrderNum(request.getOrderNum());
            if (OrderStatusEnum.COMPELETED.getCode().equals(status)) {
                order.setComfirmTime(new Date());
            } else if (OrderStatusEnum.PENDING_RECEIVED.getCode().equals(status)) {
                order.setSendTime(new Date());
            } else if (OrderStatusEnum.CANCELLED.getCode().equals(status) && order.getParentOrderNum() != null) {
                return baseResponse.build(RespStatus.ORDER_CANCEL_FAILED);
            }
            // 更新订单状态
            boolean b = orderService.updateOrder(order);
            log.info("修改订单是否成功--->" + b);
            if (b) {
                baseResponse.setCode(200);
                baseResponse.setMsg("ok");
                if (OrderStatusEnum.PENDING_RECEIVED.getCode().equals(status) && order.getParentOrderNum() == null) {
                    String formIds = partInfo.getFormId();
                    String str = new String("");
                    if (StringUtils.isNotBlank(formIds)) {
                        str = formIds.split(",")[0];
                    }
                    final String formId = str;
                    //发消息给买家采用异步线程方式
                    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                            .setNameFormat("demo-pool-%d").build();
                    ExecutorService threadPool = new ThreadPoolExecutor(2, 5,
                            10L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(10), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
                    threadPool.execute(() -> sendSmsDeliver(order.getAddrPhone(), dragonInfo.getDragonTitle(), Optional.ofNullable(order.getDragonAddr()).orElse(order.getAddress()), order, formId));
                    threadPool.execute(() -> sendStationMsgService.sendShippingRemindNotice(order.getOpenid(), dragonInfo.getDragonTitle(), Optional.ofNullable(order.getDragonAddr()).orElse(order.getAddress()), order.getOrderNum()));
                    threadPool.shutdown();
                    baseResponse.setMsg("发货成功");
                    return baseResponse;

                } else if (OrderStatusEnum.COMPELETED.getCode().equals(status)) {
                    //买家确认收货，钱款进入卖家账户

                } else if (OrderStatusEnum.REFUNDING.getCode().equals(status) && request.getRefundStatus() == null) {
                    //买家申请退款
                    //发送消息给卖家
                    sendSmsRefund(dragonInfo.getPhone(), wxUser.getNickName(), dragonInfo.getDragonTitle());
                    //发送站内消息给卖家
                    sendStationMsgService.snedRefundToSeller(order.getOpenid(), dragonInfo.getDragonTitle(), order.getOrderNum());
                    WxUser seller = wxUserService.selectByOpenid(dragonInfo.getOpenid());
                    if (seller != null) {
                        if (StringUtils.isNotBlank(seller.getUnionid())) {
                            //查询用户是否订阅公众号
                            WxUnion wxUnion = wxUserService.findWxUnionByUnionId(seller.getUnionid());
                            if (wxUnion != null && wxUnion.getIsSub().intValue() == WxSubTypeEnum.SUBSCRIBE.getCode().intValue()) {
                                try {
                                    taskService.gzhOrderRefundNotice(wxUnion.getWxOpenId(), order.getOrderNum(), String.valueOf(order.getOrderMoney()),
                                            wxUser.getNickName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    log.error("退款公众号模板消息发送失败{}退款订单号{}", e.getMessage(), order.getOrderNum());
                                }
                            }
                        }
                    }
                } else if (OrderStatusEnum.REFUNDING.getCode().equals(status) && RefundStatusEnum.REFUSED.getCode().equals(request.getRefundStatus())) {
                    //卖家拒绝，发送站内消息通知买家
                    sendStationMsgService.sendRefundRefusedNotice(order.getOpenid(), dragonInfo.getDragonTitle(), order.getOrderMoney() * 1.00 / 100);
                } else if (OrderStatusEnum.CANCELLED.getCode().equals(status)) {
                    //卖家取消，增加库存
                } else if (OrderStatusEnum.CANCELLED.getCode().equals(status) || (OrderStatusEnum.REFUNDING.getCode().equals(status) && RefundStatusEnum.REFUNDED.getCode().equals(request.getRefundStatus()))) {
                    //买家取消|| 退款成功，增加库存
                    List<OrderGoodsInfo> orderGoods = orderGoodsService.getOrderGoods(request.getOrderNum());
                    for (OrderGoodsInfo ogInfo : orderGoods) {
                        List<DragonGoods> dragonGoodsList = dragonGoodsService.getByGoodsNum(ogInfo.getGoodsNum());
                        dragonGoodsList.forEach(x -> {
                            x.setUpdateTime(new Date());
                            //增加dragon_goods库存
                            x.setCurrentNumber(ogInfo.getBuyNumber() + x.getCurrentNumber());
                            dragonGoodsService.updateDragonGoods(x);
                        });
                        GoodsInfo goods = goodsInfoService.getGoods(ogInfo.getGoodsNum());
                        //增加good_info库存
                        goods.setTotalNumber(goods.getTotalNumber() + ogInfo.getBuyNumber());
                        goods.setUpdateTime(new Date());
                        if (goodsInfoService.updateGoods(goods) == false) {
                            log.error("商品数量更新失败");
                        }
                    }
                }
                return baseResponse;
            }
        }

        baseResponse.setCode(200);
        baseResponse.setMsg("订单修改失败");
        return baseResponse;
    }

    /**
     * 生成发起退款记录
     *
     * @param wxUser
     * @param orderId
     * @param orderStatus
     */
    private void insertRefundRecord(WxUser wxUser, Integer orderId, Integer orderStatus) {
        // 生成发起退款记录
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setCreateTime(new Date());
        refundRecord.setOrderId(orderId);
        refundRecord.setRemark("退款发起人昵称-" + wxUser.getNickName() + "-退款发起人openId-" + wxUser.getOpenid());
        refundRecord.setCause("待发货订单退款");
        refundRecord.setOrderStatus(orderStatus);
        refundRecord.setStatus(RefundStatusEnum.REFUNDING.getCode());
        refundRecordService.createRefundRecord(refundRecord);
    }


    /**
     * 发货发消息
     *
     * @param phone
     * @param title
     * @param addr
     * @param order
     * @param formId
     * @return
     */
    private void sendSmsDeliver(String phone, String title, String addr, OrderInfo order, String formId) {
        try {
            SendSmsResponse sendSmsResponse = SendMessageUtil.sendSmsDeliver(phone, title, addr);
            taskService.noticeOfDelivery(order, formId);
            if (!sendSmsResponse.getCode().equals("OK")) {
                log.error("发货短信发送失败:" + phone);
                log.error("msg:" + sendSmsResponse.getMessage());
            }
        } catch (Exception exce) {
            exce.printStackTrace();
            log.error("发货短信发送失败:" + phone);
            log.error("msg:" + exce);
        }
    }

    /**
     * 退款申请发送消息
     *
     * @param phone
     * @param nickName
     * @param title
     * @return
     */
    private void sendSmsRefund(String phone, String nickName, String title) {
        try {
            SendSmsResponse sendSmsResponse = SendMessageUtil.sendSmsRefund(phone, nickName, title);
            if (!sendSmsResponse.getCode().equals("OK")) {
                log.error("退款申请短信发送失败:" + phone);
                log.error("msg:" + sendSmsResponse.getMessage());
            }
        } catch (ClientException e) {
            e.printStackTrace();
            log.error("退款申请短信发送失败:" + phone);
            log.error("msg:" + e);
        }
    }

    /**
     * 微信小程序支付
     *
     * @param requestParameter
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/unifiedOrder", method = RequestMethod.POST)
    @Transactional(rollbackFor = {IOException.class, Exception.class, NullPointerException.class})
    public BaseResponse unifiedOrder(@RequestBody UnifiedOrderRequest requestParameter, HttpServletRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        Assert.notNull(requestParameter, "parameter can not be null");
        if (StringUtils.isBlank(requestParameter.getOrderNum()) || requestParameter.getPaymentPrice() == null) {
            throw new DataException("入参不能为空");
        }

        OrderInfo orderInfo = orderService.getOrderByOrderNum(requestParameter.getOrderNum());

        if (null == orderInfo) {
            return baseResponse.build(RespStatus.ORDER_NOT_EXIST_RE_CRY);
        }

        WxUser user = wxUserService.getUserBySessionId(requestParameter.getSessionId());

        if (user == null) {
            return baseResponse.build(RespStatus.USER_NOT_EXIST);
        }


        //保存用户手机号
        if (user.getPhone().equals("0")) {
            log.info("修改手机号：", orderInfo.getAddrPhone());
            user.setPhone(orderInfo.getAddrPhone());
            wxUserService.updateUserByOpenid(user);
        }

        log.info("用户openId" + user);
        try {
            //生成的随机字符串
            String nonce_str = RandomCodeUtil.getRandomCode(32);
            //商品名称
            String body = EmojiParser.removeAllEmojis(requestParameter.getGoodsName());
            //获取客户端的ip地址
            String client_ip = com.mhc.yunxian.utils.HttpUtil.getClientIp(request);


            //支付金额，单位：分，这边需要转成字符串类型，否则后面的签名会失败
            String money = requestParameter.getPaymentPrice() + "";

            Map<String, String> packageParams = new HashMap<String, String>();
            packageParams.put("appid", appid);
            packageParams.put("mch_id", mchId);
            packageParams.put("nonce_str", nonce_str);
            packageParams.put("body", body);
            //商户订单号
            packageParams.put("out_trade_no", requestParameter.getOrderNum());
            //支付金额，这边需要转成字符串类型，否则后面的签名会失败
            packageParams.put("total_fee", money);
            packageParams.put("spbill_create_ip", client_ip);
            //支付成功回调url
            packageParams.put("notify_url", notifyUrl);
            //签名类型(MD5)
            packageParams.put("trade_type", tradeType);
            packageParams.put("openid", user.getOpenid());

            // 除去数组中的空值和签名参数
            packageParams = PaymentUtil.paraFilter(packageParams);
            // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
            String prestr = PaymentUtil.createLinkString(packageParams);
            //MD5运算生成签名，这里是第一次签名，用于调用统一下单接口
            String mysign = PaymentUtil.sign(prestr, secretKey, "utf-8").toUpperCase();
            log.info("=======================第一次签名完成：" + mysign + "=====================");

            //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去
            String xml = "<xml>" + "<appid>" + appid + "</appid>"
                    + "<body><![CDATA[" + body + "]]></body>"
                    + "<mch_id>" + mchId + "</mch_id>"
                    + "<nonce_str>" + nonce_str + "</nonce_str>"
                    + "<notify_url>" + notifyUrl + "</notify_url>"
                    + "<openid>" + user.getOpenid() + "</openid>"
                    + "<out_trade_no>" + requestParameter.getOrderNum() + "</out_trade_no>"
                    + "<spbill_create_ip>" + client_ip + "</spbill_create_ip>"
                    + "<total_fee>" + money + "</total_fee>"
                    + "<trade_type>" + tradeType + "</trade_type>"
                    + "<sign>" + mysign + "</sign>"
                    + "</xml>";

            System.out.println("调试模式_统一下单接口 请求XML数据：" + xml);
            //调用统一下单接口，并接受返回的结果
            String result = PaymentUtil.httpRequest(unifiedOrderUrl, "POST", xml);

            System.out.println("调试模式_统一下单接口 返回XML数据：" + result);

            // 将解析结果存储在HashMap中
            Map map = PaymentUtil.doXMLParse(result);
            //返回状态码
            String return_code = (String) map.get("return_code");

            //返回给移动端需要的参数
            Map<String, Object> response = new HashMap<String, Object>();
            if (return_code.equals("SUCCESS")) {
                // 业务结果
                //返回的预付单信息
                String prepay_id = (String) map.get("prepay_id");
                response.put("nonceStr", nonce_str);

                //response.put("package", "prepay_id=" + prepay_id);
                response.put("prepayId", prepay_id);
                Long timeStamp = System.currentTimeMillis() / 1000;
                //这边要将返回的时间戳转化成字符串，不然小程序端调用wx.requestPayment方法会报签名错误
                response.put("timeStamp", timeStamp + "");

                String stringSignTemp = "appId=" + appid + "&nonceStr=" + nonce_str + "&package=prepay_id=" + prepay_id + "&signType=" + signType + "&timeStamp=" + timeStamp;
                //再次签名，这个签名用于小程序端调用wx.requesetPayment方法
                String paySign = PaymentUtil.sign(stringSignTemp, secretKey, "utf-8").toUpperCase();
                log.info("=======================第二次签名完成：" + paySign + "=====================");
                response.put("paySign", paySign);

                response.put("orderNum", requestParameter.getOrderNum());
                //TODO 业务逻辑代码

                //保存prepay_id
                PartInfo partInfo = new PartInfo();
                partInfo.setPrepayId(prepay_id);
                partInfo.setOrderNum(requestParameter.getOrderNum());
                partyInfoService.updatePartInfo(partInfo);


            }

            response.put("appid", appid);

            baseResponse.build(RespStatus.UNIFIED_ORDER_SUCCESS);
            baseResponse.setMap(response);
        } catch (Exception e) {
            e.printStackTrace();
            baseResponse.build(RespStatus.UNIFIED_ORDER_FAILURE);
        }
        return baseResponse;
    }

    /**
     * 微信支付回调函数
     * 微信支付成功微信服务端循环回调通知方法
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/payment/success", method = RequestMethod.POST)
    public String paymentSuccessCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        //sb为微信返回的xml
        String notityXml = sb.toString();
        String resXml = "";
        log.info("接收到的报文：" + notityXml);
        Map map = PaymentUtil.doXMLParse(notityXml);
        Map noSginMap = new HashMap();
        noSginMap.putAll(map);
        noSginMap.remove("sign");
        String returnCode = (String) map.get("return_code");
        if ("SUCCESS".equals(returnCode)) {
            //验证签名是否正确
            if (PaymentUtil.verify(PaymentUtil.createLinkString(noSginMap), (String) map.get("sign"), "&key=" + secretKey, "utf-8")) {
                String orderNum = (String) map.get("out_trade_no");
                log.info("订单号：{}", orderNum);
                try {
                    Boolean flag = this.paySuccessUpdateOrder(orderNum);
                    if (flag) {
                        // 订单更新成功时
                        OrderInfo orderInfo = orderService.getOrderByOrderNum(orderNum);
                        //更新我的用户列表
                        final PartInfo partInfo1;
                        if (null != orderInfo.getParentOrderNum()) {
                            partInfo1 = partyInfoService.getPartByOrderNum(orderInfo.getParentOrderNum());
                        } else {
                            partInfo1 = partyInfoService.getPartByOrderNum(orderNum);
                        }
                        final DragonInfo dragonInfo1 = dragonService.getDragon(partInfo1.getDragonNum());
                        //卖家
                        final WxUser sellUser = wxUserService.getWxUser(dragonInfo1.getOpenid());
                        //买家
                        final WxUser buyUser = wxUserService.getWxUser(orderInfo.getOpenid());
                        // 插入商家的客户信息
                        this.insertMyUserInfo(orderInfo.getOrderMoney(), sellUser.getOpenid(), buyUser.getOpenid());
                        // 支付成功更新商品销量
                        this.paySuccessUpdateGoodsSalesVolume(orderNum);
                        // 发送通知给用户和商家
                        this.notify(orderInfo, partInfo1.getPrepayId(), dragonInfo1, sellUser.getUnionid(), buyUser);
                        // 发送站内通知给买家和卖家
                        sendStationMsgService.sendOrderSuccessNoticeToBuyer(orderInfo.getOpenid(),dragonInfo1.getDragonTitle(),orderInfo.getOrderNum());
                        sendStationMsgService.sendOrderSuccessNoticeToSeller(orderInfo.getOpenid(),dragonInfo1.getDragonTitle(),dragonInfo1.getDragonNum());
                        //更新/管理用户的复购缓存/商品复购缓存/店铺复购
                        ExecutorService service = Executors.newSingleThreadExecutor();
                        service.execute(()->{
                            //用户
                            statisticsService.updateUserRepurchase(orderInfo.getOpenid(),dragonInfo1.getOpenid());
                            //商品
                            List<OrderGoodsInfo> orderGoodsInfos = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
                            orderGoodsInfos.forEach(x->{
                                statisticsService.updateGoodsRepurchase(x.getGoodsNum());
                            });
                            //店铺
                            statisticsService.updateShopRepurchase(dragonInfo1.getOpenid());
                        });
                        service.shutdown();
                        // 更新商家余额
                        final Balance balance = updateSellerBalance(orderInfo, sellUser);
                        // 扣除手续费
                        this.deductionOfHandlingFees(sellUser, balance);

                        // 通知微信服务器已经支付成功
                        resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                                + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                        return resXml;
                    } else {
                        log.error("支付成功更新订单状态失败,订单编号{}", orderNum);
                        // 更新订单失败,通知微信服务端失败
                        resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                                + "<return_msg><![CDATA[FAIL]]></return_msg>" + "</xml> ";
                        return resXml;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("支付成功更新订单状态失败,订单编号{}", orderNum);
                    // 更新订单失败,通知微信服务端失败
                    resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                            + "<return_msg><![CDATA[FAIL]]></return_msg>" + "</xml> ";
                    log.error("【微信支付回调函数数据更新错误】错误信息:" + e);
                    return resXml;
                }
            }
        } else {
            resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                    + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
        }
        System.out.println(resXml);
        System.out.println("微信支付回调数据结束");
        return resXml;
    }

    /**
     * 支付成功时扣除商家的手续费
     *
     * @param sellUser
     * @param balance
     */
    private void deductionOfHandlingFees(WxUser sellUser, Balance balance) {

        SystemParam systemParam = systemParamService.selectOneByParamGroup("SERVICE_CHARGE");
        if (null == systemParam) {
            throw new DataException("交易费读取失败");
        }
        if (Integer.valueOf(systemParam.getParamValue()) > 0) {
            MoneyRecord serviceCharge = new MoneyRecord();
            //扣除交易费
            serviceCharge.setRecordType(MoneyRecordEnum.TRANSACTION_COST.getCode());
            serviceCharge.setCause(MoneyRecordEnum.TRANSACTION_COST.getMsg());
            serviceCharge.setOpenid(sellUser.getOpenid());
            serviceCharge.setBalance((balance.getBalance() - Integer.valueOf(systemParam.getParamValue())) + "");
            serviceCharge.setMoney(Integer.valueOf(systemParam.getParamValue()));
            serviceCharge.setCreateTime(new Date());
            if (!moneyRecordService.add(serviceCharge)) {
                throw new DataException("更新交易记录表错误，回滚");
            }
            balance.setBalance(balance.getBalance() - Integer.valueOf(systemParam.getParamValue()));
            balance.setUpdateTime(new Date());
            if (!balanceService.updateBalanceByOpenid(balance)) {
                throw new DataException("更新余额错误，支付回滚");
            }
        }
    }

    /**
     * 支付成功时更新商家的余额
     *
     * @param orderInfo
     * @param sellUser
     * @return
     */
    private Balance updateSellerBalance(OrderInfo orderInfo, WxUser sellUser) {
        //商家余额增加订单金额
        final Balance balance = balanceService.getBalance(sellUser.getOpenid());
        if (balance == null) {
            throw new DataException("余额不存在，支付回滚");
        }
        sellUser.setOrderNumber(sellUser.getOrderNumber() + 1);

        if (!wxUserService.updateUserByOpenid(sellUser)) {
            throw new DataException("更新用户表错误，回滚");
        }

        List<MoneyRecord> list = moneyRecordService.listRecordByOrderNum(orderInfo.getOrderNum());
        if (CollectionUtils.isNotEmpty(list)) {
            log.warn("订单流水已存在,订单编号{}", orderInfo.getOrderNum());
        } else {
            //更新我的余额
            log.info("我的余额：{}，订单余额：{}", balance.getBalance(), orderInfo.getOrderMoney());
            balance.setBalance(balance.getBalance() + orderInfo.getOrderMoney());
            balance.setUpdateTime(new Date());
            if (!balanceService.updateBalanceByOpenid(balance)) {
                throw new DataException("更新余额错误，支付回滚");
            }
            //插入余额交易纪录明细表
            log.info("订单金额：{}", orderInfo.getOrderMoney() / 100);
            final MoneyRecord moneyRecord = new MoneyRecord();
            moneyRecord.setCreateTime(new Date());
            moneyRecord.setMoney(orderInfo.getOrderMoney());
            moneyRecord.setOpenid(sellUser.getOpenid());
            //收入
            moneyRecord.setRecordType(MoneyRecordEnum.IMCOME.getCode());
            moneyRecord.setCause(MoneyRecordEnum.IMCOME.getMsg());
            moneyRecord.setOrderNum(orderInfo.getOrderNum());
            moneyRecord.setBalance(String.valueOf(balance.getBalance()));
            if (!moneyRecordService.add(moneyRecord)) {
                throw new DataException("插入余额交易纪录错误，支付回滚");
            }
        }
        return balance;
    }

    /**
     * 支付成功时更新商品销量
     *
     * @param orderNum
     */
    private void paySuccessUpdateGoodsSalesVolume(String orderNum) {
        //支付成功，商品销量增加
        final List<OrderGoodsInfo> orderGoods = orderGoodsService.getOrderGoods(orderNum);
        for (OrderGoodsInfo ogInfo : orderGoods) {
            final GoodsInfo info = goodsInfoService.getGoods(ogInfo.getGoodsNum());
            //商品总数量-购买数量
            info.setUpdateTime(new Date());
            info.setSalesVolume(info.getSalesVolume() + ogInfo.getBuyNumber());
            if (!goodsInfoService.updateGoods(info)) {
                throw new DataException("商品数量更新失败，支付回滚");
            }
        }
    }

    /**
     * 支付成功时插入或更新商家客户记录
     *
     * @param orderMoney     订单金额
     * @param sellUserOpenId 商家openId
     * @param buyUserOpenId  消费者openId
     */
    private void insertMyUserInfo(Integer orderMoney, String sellUserOpenId, String buyUserOpenId) {
        final MyUser user1 = new MyUser();
        user1.setUserOpenid(buyUserOpenId);
        user1.setMyOpenid(sellUserOpenId);
        final MyUser myUsersell = myUserService.getUser(buyUserOpenId, sellUserOpenId);
        if (myUsersell == null) {
            //第一次购买
            user1.setBuyTime(1);
            user1.setTotalMoney(orderMoney);
            user1.setCreateTime(new Date());
            if (!myUserService.insertMyUser(user1)) {
                throw new DataException("插入我的用户纪录错误，支付回滚");
            }
        } else {
            user1.setBuyTime(myUsersell.getBuyTime() + 1);
            user1.setTotalMoney(myUsersell.getTotalMoney() + orderMoney);
            user1.setUpdateTime(new Date());
            if (!myUserService.updateMyUser(user1)) {
                throw new DataException("更新我的用户纪录错误，支付回滚");
            }
        }
    }

    /**
     * 支付成功时更新订单状态
     *
     * @param orderNum 订单编号
     * @return
     */
    private Boolean paySuccessUpdateOrder(String orderNum) {
        // 更新订单信息
        OrderInfo orderInfo = orderService.getOrderByOrderNum(orderNum);
        log.info("订单信息orderInfo：{}", orderInfo);
        //商户订单号
        //已付款
        orderInfo.setOrderStatus(OrderStatusEnum.PENDING_DELIVERY.getCode());
        if (orderInfo.getParentOrderNum() != null) {
            //补差订单直接完成
            orderInfo.setOrderStatus(OrderStatusEnum.COMPELETED.getCode());
        }
        //付款时间
        orderInfo.setUpdateTime(new Date());
        boolean falg = orderService.updateOrder(orderInfo);
        return falg;
    }

    /**
     * 支付成功且订单状态更新成功时
     * 发送支付成功通知给消费者
     * 发送消费者支付成功通知给商家
     * 发送短信给商家
     * 发送公众号通知给商家
     *
     * @param orderInfo       订单信息
     * @param prepayId        预付id
     * @param dragonInfo1     接龙信息
     * @param sellUserUnionId 商家unionId
     * @param buyUser         消费者用户信息
     */
    private void notify(OrderInfo orderInfo, String prepayId, DragonInfo dragonInfo1, String sellUserUnionId, WxUser buyUser) {
        //消费者支付成功通知消费者
        try {
            taskService.noticeOfPayment(orderInfo, buyUser.getOpenid(), prepayId, 0);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送模板消息失败:" + e);
        }
        //消费者支付成功通知卖家
//        try {
//            taskService.noticeOfPayment(orderInfo, sellerUser.getOpenid(), prepayId, 1);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("发送模板消息失败:" + e);
//        }

        try {
            SendSmsResponse sendSmsResponse = SendMessageUtil.sendSms(dragonInfo1.getPhone(), dragonInfo1.getDragonTitle(), orderInfo.getAddrName());
            if (!sendSmsResponse.getCode().equals("OK")) {
                log.error("短信发送失败:" + dragonInfo1.getPhone());
                log.error("msg:" + sendSmsResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("短信发送失败:" + orderInfo.getOrderNum());
            log.error("msg:" + e.getMessage());
        }
        if (StringUtils.isNotBlank(sellUserUnionId)) {
            //查询用户是否订阅公众号
            WxUnion wxUnion = wxUserService.findWxUnionByUnionId(sellUserUnionId);
            if (wxUnion != null && wxUnion.getIsSub().intValue() == WxSubTypeEnum.SUBSCRIBE.getCode().intValue()) {
                try {
                    taskService.gzhOrderNotice(wxUnion.getWxOpenId(), dragonInfo1.getDragonTitle(),
                            buyUser.getNickName(), String.valueOf(orderInfo.getOrderMoney()), String.valueOf(orderInfo.getOrderMoney()), orderInfo.getOrderNum());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("公众号模版消息发送失败,订单编号{},错误信息msg{}" + orderInfo.getOrderNum(), e.getMessage());
                }
            }
        }
    }

    /**
     * 微信退款接口 todo 第二版测试时，同意退款接口换成该接口
     *
     * @param refundMoneyRequest
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/order/v2/refundOrder", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, IOException.class, NullPointerException.class, DataException.class}, isolation = Isolation.SERIALIZABLE)
    public BaseResponse refundOrder(@RequestBody RefundMoneyRequest refundMoneyRequest, HttpServletRequest httpServletRequest) {
        final BaseResponse response = orderService.refundOrder(refundMoneyRequest, 0);

        return response;
    }


    /**
     * 修改订单金额接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/v2/reviseMoney", method = RequestMethod.POST)
    public BaseResponse changeMoney(@RequestBody ReviseMoneyRequest request) {
        final BaseResponse response = new BaseResponse();
        final OrderInfo orderInfo = orderService.getOrderByOrderNum(request.getOrderNum());
        if (null == orderInfo) {
            return response.build(RespStatus.ORDER_NOT_EXIST);
        }

        if (!orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getCode())) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        orderInfo.setOrderMoney(request.getMoney());
        if (!orderService.updateOrder(orderInfo)) {
            return response.build(RespStatus.REVISE_ORDER_MONEY_ERROR);
        }
        return response;
    }

    /**
     * 获取我发起的所有接龙
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/getAllMyDragon", method = RequestMethod.POST)
    public GetAllMyDragonResponse getAllMyDragon(@RequestBody GetAllMyDragonRequest request) {
        final GetAllMyDragonResponse response = new GetAllMyDragonResponse();
        //查询openid
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return (GetAllMyDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }

        //查询我创建的所有接龙
        List<DragonInfo> dragonInfoList = dragonService.getDragonByOpenid(wxUser.getOpenid());
        if (null == dragonInfoList) {
            return (GetAllMyDragonResponse) response.build(RespStatus.NOT_DRAGON);
        }

        final List<GetAllMyDragon> list = Lists.newArrayList();


        for (DragonInfo dragonInfo : dragonInfoList) {

            int totalMoney = 0;

            int refundingNum = 0;

            //判断接龙状态是否是进行中
            if (null != request.getDragonStatus() && !dragonInfo.getDragonStatus().equals(request.getDragonStatus())) {
                continue;
            }

            //获取该接龙的所有订单
            List<OrderInfo> orderInfoList = orderService.getOpenidByDragonNum(dragonInfo.getDragonNum());
            if (null == orderInfoList) {
                return (GetAllMyDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
            }
            for (OrderInfo orderInfo : orderInfoList) {
                if (orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())
                        || orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_RECEIVED.getCode())
                        || orderInfo.getOrderStatus().equals(OrderStatusEnum.COMPELETED.getCode())
                        || orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())) {
                    totalMoney += orderInfo.getOrderMoney();
                }

                if (orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())) {
                    refundingNum++;
                }

            }

            final GetAllMyDragon getAllMyDragon = new GetAllMyDragon();
            getAllMyDragon.setCreateTime(dragonInfo.getCreateTime().getTime() + "");
            //截取第一张图片
            getAllMyDragon.setDragonImg(dragonInfo.getDragonImg().substring(0, dragonInfo.getDragonImg().indexOf(",")));
            getAllMyDragon.setPartyNumber(orderInfoList.size());
            getAllMyDragon.setTotalMoney(totalMoney);
            getAllMyDragon.setTitle(dragonInfo.getDragonTitle());
            getAllMyDragon.setDragonNum(dragonInfo.getDragonNum());
            getAllMyDragon.setRefundingNum(refundingNum);
            list.add(getAllMyDragon);
        }

        response.setData(list);

        return response;
    }

    /**
     * 获取我发起的所有接龙V4
     * 我的接龙-进行中/下架的接龙(店家专用)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/v4/getAllMyDragon", method = RequestMethod.POST)
    public GetAllMyDragonResponse getAllMyDragonV4(@RequestBody GetAllMyDragonRequest request) {
        final GetAllMyDragonResponse response = new GetAllMyDragonResponse();
        //查询openid
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return (GetAllMyDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        /**
         * 若当前用户不是卖家,直接返回空列表
         */
        if (IsWhiteEnum.IS_NOT_WHITE.getCode().equals(wxUser.getIsWhite())) {
            return (GetAllMyDragonResponse) response.build(RespStatus.SUCCESS);
        }
        Shop shopByKeeperOpenid = shopMapper.getShopByKeeperOpenid(wxUser.getOpenid());
        response.setShopId(shopByKeeperOpenid.getShopId());
        if (request.getDragonStatus() == null) {
            return (GetAllMyDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        DragonInfo dragonInfoRequest = new DragonInfo();
        dragonInfoRequest.setOpenid(wxUser.getOpenid());
        dragonInfoRequest.setDragonStatus(request.getDragonStatus());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("isWhite", wxUser.getIsWhite());
        hashMap.put("dragonButIsOpen", wxUser.getDragonButIsOpen());
        response.setMap(hashMap);
        //查询我创建的所有接龙
        PageHelper.startPage(request.getPage(), request.getSize());
        List<DragonInfo> dragonInfoList = dragonService.selectDragonByStatusAndOpenid(dragonInfoRequest);
        final List<GetAllMyDragon> list = Lists.newArrayList();
        for (DragonInfo dragonInfo : dragonInfoList) {
            int totalMoney = 0;
            int refundingNum = 0;
            int pendingNum = 0;
            final GetAllMyDragon getAllMyDragon = new GetAllMyDragon();
            //获取该接龙的所有订单
            List<OrderInfo> orderInfoList = orderService.getOpenidByDragonNum(dragonInfo.getDragonNum());
            List<OrderInfo> filterOrders = Lists.newArrayList();
            for (OrderInfo orderInfo : orderInfoList) {
                if(StringUtils.isNotBlank(orderInfo.getParentOrderNum())){
                    if(DifTypeEnum.REPAIR_ORDER.getType().equals(orderInfo.getOrderType())){
                        //如果是补差子订单 且本身不处于代付款、退款中 ，则增加总价
                        if(OrderStatusEnum.PENDING_DELIVERY.getCode().equals(orderInfo.getOrderStatus()) ||
                                OrderStatusEnum.PENDING_RECEIVED.getCode().equals(orderInfo.getOrderStatus()) ||
                                OrderStatusEnum.COMPELETED.getCode().equals(orderInfo.getOrderStatus()) ||
                                OrderStatusEnum.REFUNDING.getCode().equals(orderInfo.getOrderStatus())) {
                            totalMoney += orderInfo.getOrderMoney();
                            filterOrders.add(orderInfo);
                        }
                    }else {
                        //如果是退差子订单且父订单没有退款，则减少 总价
                        OrderInfo parentOrder = orderService.getOrderByOrderNum(orderInfo.getParentOrderNum());
                        if(OrderStatusEnum.PENDING_DELIVERY.getCode().equals(parentOrder.getOrderStatus()) ||
                           OrderStatusEnum.PENDING_RECEIVED.getCode().equals(parentOrder.getOrderStatus()) ||
                           OrderStatusEnum.COMPELETED.getCode().equals(parentOrder.getOrderStatus()) ||
                           OrderStatusEnum.REFUNDING.getCode().equals(parentOrder.getOrderStatus())){
                            totalMoney -=orderInfo.getOrderMoney();
                            filterOrders.add(orderInfo);
                        }
                    }

                }else {
                    if(OrderStatusEnum.PENDING_PAYMENT.getCode().equals(orderInfo.getOrderStatus())){
                        pendingNum++;
                        //去除待付款的
                        continue;
                    }
                    if(OrderStatusEnum.CANCELLED.getCode().equals(orderInfo.getOrderStatus())) {
                        //去除取消的
                        continue;
                    }
                    if(OrderStatusEnum.REFUNDED.getCode().equals(orderInfo.getOrderStatus())){
                        //去除退款完成的
                        continue;
                    }
                    if (orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())) {
                        refundingNum++;
                    }
                    totalMoney += orderInfo.getOrderMoney();
                    filterOrders.add(orderInfo);
                }

            }
            /**
             *  统计未下架的最近一期截单时间内的接龙人数和订单收入
             */
            String deliveryCycle = dragonInfo.getDeliveryCycle();
            // 兼容老数据
            getAllMyDragon.setCutOffTime(dragonInfo.getEndTime());
            List<FindDragon> partDragonUser = dragonService.getPartDragonUser(dragonInfo.getDragonNum(), null);
            getAllMyDragon.setPartyNumber(partDragonUser.size());
            getAllMyDragon.setDragonNum(dragonInfo.getDragonNum());
            try {
                if (StringUtils.isNotBlank(deliveryCycle)) {
                    DeliveryCycle cycle = JSONObject.parseObject(deliveryCycle, DeliveryCycle.class);
                    Date cutOffDate = null;
                    if (!DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(cycle.getCycleType())) {
                        // 下一期截单时间
                        cutOffDate = dragonService.getDragonDeliveryCycleDate(deliveryCycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME);
                        getAllMyDragon.setCutOffTime(DateUtil.SDF.format(cutOffDate));
                        // 下一期发货时间
                        Date deliveryDate = dragonService.getDragonDeliveryCycleDate(deliveryCycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME);
                        getAllMyDragon.setSendTime(DateUtil.SDF.format(deliveryDate));

                    }

                    switch (cycle.getCycleType()) {
                        case 0:
                            // 不是周期性截单发货
                            getAllMyDragon.setCutOffTime(dragonInfo.getEndTime());
                            List<FindDragon> partDragonUser1 = dragonService.getPartDragonUser(dragonInfo.getDragonNum(), null);
                            getAllMyDragon.setCutOffPartyNumber(partDragonUser1.size());
                            getAllMyDragon.setPartyNumber(partDragonUser1.size());
                            getAllMyDragon.setCutOffTotalMoney(totalMoney);
                            break;
                        case 1:
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(cutOffDate);
                            // 计算截单时间段内的订单金额和接龙人数需要上一期截单时间
                            // 前一天
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            log.info("上期截单时间{}", DateUtil.SDF.format(calendar.getTime()));
                            this.calculateMoneyAndPeople(getAllMyDragon, filterOrders, calendar.getTime());
                            break;
                        case 2:
                            // 计算截单时间段内的订单金额和接龙人数需要上一期截单时间
                            List<Date> dateList = dragonService.getDateList(cycle.getCutHour(), cycle.getDayOfWeek(), DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME.getCode());
                            // 筛选出在当前时间之前的截单时间点
                            List<Date> collect = dateList.stream().filter(date -> date.before(new Date())).collect(Collectors.toList());
                            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(collect)) {
                                // 上期截单(或者发货)时间,取最后一个
                                Date date = collect.get(collect.size() - 1);
                                log.info("上期截单时间{}", DateUtil.SDF.format(cutOffDate));
                                this.calculateMoneyAndPeople(getAllMyDragon, filterOrders, date);
                            } else {
                                //本周中获取不到上周时间,说明不存在已经截单的时间,计算总金额时取接龙创建时间为节点
                                this.calculateMoneyAndPeople(getAllMyDragon, filterOrders, dragonInfo.getCreateTime());
                            }
                            break;
                        case 3:
                            // 每月中一天或几天 (需求砍掉)
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("周期发货信息解析异常{}", e.getMessage());
            }
            getAllMyDragon.setCreateTime(dragonInfo.getCreateTime().getTime() + "");
            //截取第一张图片
            getAllMyDragon.setDragonImg(dragonInfo.getDragonImg());
            // 按用户分组
            // Map<String, List<OrderInfo>> collect = filterOrders.stream().collect(Collectors.groupingBy(OrderInfo::getOpenid));

            //截取到秒
            if (org.apache.commons.lang.StringUtils.isNotBlank(getAllMyDragon.getCutOffTime())) {
                getAllMyDragon.setCutOffTime(getAllMyDragon.getCutOffTime().substring(0, getAllMyDragon.getCutOffTime().lastIndexOf(":")));
            }
            getAllMyDragon.setTotalMoney(totalMoney);
            getAllMyDragon.setRefundingNum(refundingNum);
            getAllMyDragon.setPendingNum(pendingNum);
            getAllMyDragon.setTitle(dragonInfo.getDragonTitle());
            getAllMyDragon.setDragonNum(dragonInfo.getDragonNum());
            list.add(getAllMyDragon);
        }
        response.setData(list);
        return response;
    }

    /**
     * 计算周期内的接龙人数和收入
     *
     * @param getAllMyDragon 响应参数对象
     * @param filterOrders   订单列表
     * @param cutOffDate     上期截单时间
     */
    private void calculateMoneyAndPeople(GetAllMyDragon getAllMyDragon, List<OrderInfo> filterOrders, Date cutOffDate) {
        Integer cutTotalMoney = 0;
        for (OrderInfo orderInfo : filterOrders){
            if(orderInfo.getCreateTime().before(cutOffDate)){
                continue;
            }
            if(StringUtils.isNotBlank(orderInfo.getParentOrderNum())){
                if(DifTypeEnum.RETREAT_ORDER.getType().equals(orderInfo.getOrderType())){
                    //去掉退差子订单的价格
                    cutTotalMoney -= orderInfo.getOrderMoney();
                }else {
                    cutTotalMoney += orderInfo.getOrderMoney();
                }
                continue;
            }
            cutTotalMoney += orderInfo.getOrderMoney();
        }
        getAllMyDragon.setCutOffTotalMoney(cutTotalMoney);

        List<FindDragon> partDragonUser = dragonService.getPartDragonUser(getAllMyDragon.getDragonNum(), cutOffDate);

        // 设置截单时间段内的总参与人数
        getAllMyDragon.setCutOffPartyNumber(partDragonUser.size());
    }

    /**
     * 修改订单商品重量
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/v2/reviseWeight", method = RequestMethod.POST)
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public BaseResponse changePrice(@RequestBody GetGoodsInfoRequest request) {
        final BaseResponse response = new BaseResponse();

        try {
            for (GoodsInfo goodsInfo : request.getData()) {
                final OrderGoodsInfo orderGoodsInfo = new OrderGoodsInfo();
                orderGoodsInfo.setGoodsNum(goodsInfo.getGoodsNum());
                orderGoodsInfo.setOrderNum(request.getOrderNum());
                orderGoodsInfo.setBuyWeight(goodsInfo.getBuyWeight());
                if (!orderGoodsService.updateOrderGoodsByOrderNum(orderGoodsInfo)) {
                    throw new DataException("商品重量修改错误");
                }
            }
            final OrderInfo orderInfo = orderService.getOrderByOrderNum(request.getOrderNum());
            orderInfo.setOrderMoney(request.getMoney());
            if (!orderService.updateOrder(orderInfo)) {
                throw new DataException("订单金额修改错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.build(RespStatus.GOODS_NUM_ERROR);
        }
        return response;
    }

    /**
     * 卖家确认后付款订单
     * @param request
     * @return
     */
//	@RequestMapping(value = "/order/v2/commitCodOrder",method = RequestMethod.POST)
//	@Transactional(isolation = Isolation.REPEATABLE_READ)
//	public BaseResponse commitCodOrder(@RequestBody ComfirmOrderRequest request){
//		final BaseResponse response = new BaseResponse();
//		final OrderInfo orderInfo = orderService.getOrderByOrderNum(request.getOrderNum());
//		orderInfo.setIsCod(3);
//		if (!orderService.updateOrder(orderInfo)){
//			return response.build(RespStatus.COMMIT_ORDER_ERROR);
//		}
//		return response;
//	}

    /**
     * 生成补差价订单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/v2/createDifOrder", method = RequestMethod.POST)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BaseResponse createDifOrder(@RequestBody AddDifOrderRequest request) {
        final BaseResponse response = new BaseResponse();

        Integer msgType = null;

        OrderInfo parentOrderInfo = orderService.getOrderByOrderNum(request.getParentOrderNum());

        if (request.getPrice() < DifPriceEnum.DIF_PRICE_MIN.getCode() || request.getPrice() > DifPriceEnum.DIF_PRICE_MAX.getCode()) {
            return response.build(RespStatus.DIF_PRICE_LIMIT);
        }

        if (parentOrderInfo.getOrderMoney() <= request.getPrice() && request.getType() == 4) {
            return response.build(RespStatus.DIF_PRICE_TOO_MUCH);
        }
        if (request.getType().equals(4)) {
            msgType = MsgTypeEnum.BUYER_RETREAT_DIF.getCode();
            request.setOrderType(OrderTypeEnum.REFUND_ORDER.getCode());
        }
        if (request.getType().equals(0)) {
            msgType = MsgTypeEnum.BUYER_PATCH_DIF.getCode();
            request.setOrderType(OrderTypeEnum.DIF_ORDER.getCode());
        }

        if (parentOrderInfo.getParentOrderNum() != null && parentOrderInfo.getParentOrderNum().length() > 0) {
            return response.build(RespStatus.DIF_ORDER_CREATE_FAILED);//补差订单不允许补差
        }

        if (orderService.getDifOrderByParentOrderNum(request.getParentOrderNum()) != null) {
            return response.build(RespStatus.DIF_ORDER_CREATE_ONLY_ONE);//订单只允许补差一次
        }

        if (!parentOrderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        if (!orderService.createDifOrder(request)) {
            return response.build(RespStatus.COMMIT_ORDER_ERROR);
        }

        //发送退补差价通知
        DragonInfo dragon = dragonService.getDragon(parentOrderInfo.getDragonNum());
        sendStationMsgService.sendDifNotice(parentOrderInfo.getOpenid(),msgType,dragon.getDragonTitle(),(request.getPrice()*1.00)/100,parentOrderInfo.getOrderNum());

        return response;
    }

    /**
     * 发送发货通知
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/v2/sendDeliveryNotice", method = RequestMethod.POST)
    public BaseResponse sendDeliveryNotice(@RequestBody DeliveryNoticeRequest request) {
        final BaseResponse response = new BaseResponse();
        OrderInfo orderInfo = orderService.getOrderByOrderNum(request.getOrderNum());
        if (orderInfo.getSendNoticeNum() < 1) {
            response.build(RespStatus.SEND_NOTICE_NUM_EMPTY);
        }
        DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());

        PartInfo partInfo = partyInfoService.getPartByOrderNum(request.getOrderNum());
        try {
            orderInfo.setSendNoticeNum(orderInfo.getSendNoticeNum() - 1);
            if (orderService.updateOrder(orderInfo)) {

                //发货给买家发消息
                sendSmsDeliver(orderInfo.getAddrPhone(), dragonInfo.getDragonTitle(), orderInfo.getDragonAddr(), orderInfo, partInfo.getFormId());
            } else {
                response.build(RespStatus.SYSTEM_ERROR);
            }
            response.build(RespStatus.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            response.build(RespStatus.SYSTEM_ERROR);
        }
        return response;
    }

    /**
     * 订单批量发货
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/v3/batchDeliver", method = RequestMethod.POST)
    public BaseResponse createDifOrder(@RequestBody BatchDeliverRequest request) {
        final BaseResponse response = new BaseResponse();

        try {
            List<OrderInfo> orderInfos = orderService.getOpenidByDragonNum(request.getDragonNum());

            DragonInfo dragonInfo = dragonService.getDragon(request.getDragonNum());

            for (OrderInfo e : orderInfos) {
                if (e.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())) {//待发货
                    e.setOrderStatus(OrderStatusEnum.PENDING_RECEIVED.getCode());//待收货
                    e.setSendTime(new Date());


                    if (!orderService.updateOrder(e)) {
                        return response.build(RespStatus.COMMIT_ORDER_ERROR);
                    }

                    //发货给买家发消息
                    if (StringUtils.isBlank(e.getParentOrderNum())) {
                        PartInfo partInfo = partyInfoService.getPartByOrderNum(e.getOrderNum());
                        if (StringUtils.isNotBlank(e.getDragonAddr())) {
                            sendSmsDeliver(e.getAddrPhone(), dragonInfo.getDragonTitle(), e.getDragonAddr(), e, partInfo.getFormId().split(",")[0]);
                        } else {
                            sendSmsDeliver(e.getAddrPhone(), dragonInfo.getDragonTitle(), e.getAddress(), e, partInfo.getFormId().split(",")[0]);
                        }
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            return response.build(RespStatus.SYSTEM_ERROR);
        }
        response.setMsg("发货成功");

        return response;
    }


    /**
     * 统计卖家累计成交次数(不含退款)。
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/v4/countOrderNum", method = RequestMethod.POST)

    public OrderNumResponse countOrderNum(@RequestBody OrderNumRequest request) {
        final OrderNumResponse response = new OrderNumResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        List<OrderInfo> orderInfoList = orderService.getOrderBySellerId(wxUser.getOpenid());

        Integer count = 0;

        for (OrderInfo e : orderInfoList) {
            if (e.getOrderStatus() == OrderStatusEnum.COMPELETED.getCode()) {
                count++;
            }
        }

        response.setOrderNumber(count);

        return response;
    }


    /**
     * 查询退款订单数目
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/refundingOrderNumber", method = RequestMethod.POST)
    public OrderNumResponse refundingOrderNumber(@RequestBody OrderNumRequest request) {
        final OrderNumResponse response = new OrderNumResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return (OrderNumResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        List<OrderInfo> orderInfoList = orderService.getOrderBySellerId(wxUser.getOpenid());

        Integer count = 0;

        for (OrderInfo e : orderInfoList) {
            if (e.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())) {
                count++;

                if (StringUtils.isBlank(response.getDragonNum())) {
                    response.setDragonNum(e.getDragonNum());
                }
            }
        }

        response.setOrderNumber(count);

        return response;
    }

    /**
     * 查询待发货订单数目
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/pendingOrderNumber", method = RequestMethod.POST)
    public OrderNumResponse pendingOrderNumber(@RequestBody OrderNumRequest request) {
        final OrderNumResponse response = new OrderNumResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return (OrderNumResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        List<OrderInfo> orderInfoList = orderService.getOrderBySellerId(wxUser.getOpenid());

        Integer count = 0;

        for (OrderInfo e : orderInfoList) {
            if (e.getOrderStatus().equals(OrderStatusEnum.PENDING_DELIVERY.getCode())) {
                count++;
            }

            if (StringUtils.isBlank(response.getDragonNum())) {
                response.setDragonNum(e.getDragonNum());
            }
        }

        response.setOrderNumber(count);

        return response;
    }

    /**
     * 调价
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/changeOrderMoney", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, IOException.class, NullPointerException.class, DataException.class}, isolation = Isolation.REPEATABLE_READ)
    public BaseResponse changeOrderMoney(@RequestBody ChangeOrderMoneyRequest request) {
        final BaseResponse response = new BaseResponse();

        if (request.getMoney() == null || request.getState() == null) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        OrderInfo orderInfo = orderService.getOrderByOrderNum(request.getOrderNum());

        if (orderInfo == null) {
            return response.build(RespStatus.ORDER_NOT_EXIST);
        }

        if (orderInfo.getOrderStatus().intValue() != OrderStatusEnum.PENDING_PAYMENT.getCode()) {
            return response.build(RespStatus.ORDER_MONEY_CHANGE_FAILED);
        }

        if (request.getState() == 1 && request.getMoney() > orderInfo.getOrderMoney()) {
            return response.build(RespStatus.CHANGE_MONEY_TO_MUCH);
        }

        List<OrderGoodsInfo> orderGoodsInfos = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());

        if (orderGoodsInfos.size() < 1) {
            return response.build(RespStatus.GOODS_NOT_NULL);
        }

        int oldOrderMoney = 0;

        for (OrderGoodsInfo orderGoodsInfo : orderGoodsInfos) {//订单原始总价

            oldOrderMoney += orderGoodsInfo.getBuyNumber() * orderGoodsInfo.getPrice();
        }

        if (request.getState() == 1) {
            orderInfo.setOrderMoney(orderInfo.getOrderMoney() - request.getMoney());
        } else {
            orderInfo.setOrderMoney(orderInfo.getOrderMoney() + request.getMoney());
        }

        int newOrderMoney = orderInfo.getOrderMoney();//订单新价

        Double discount = ((double) newOrderMoney) / ((double) oldOrderMoney);

        String newOrderNum = KeyTool.createOrderNo();

        for (OrderGoodsInfo orderGoodsInfo : orderGoodsInfos) {
            int realMoney = (int) (Math.floor(orderGoodsInfo.getPrice() * discount + 0.5));

            orderGoodsInfo.setRealPrice(realMoney);

            orderGoodsInfo.setOrderNum(newOrderNum);

            orderGoodsInfo.setUpdateTime(new Date());


            if (!orderGoodsService.updateById(orderGoodsInfo)) {//修改订单商品订单号
                throw new DataException("修改订单商品订单号错误");
            }

        }


        PartInfo partInfo = partyInfoService.getPartByOrderNum(orderInfo.getOrderNum());

        if (partInfo == null) {
            return response.build(RespStatus.SYSTEM_ERROR);
        }

        partInfo.setOrderNum(newOrderNum);

        if (!partyInfoService.updatePartInfoById(partInfo)) {
            throw new DataException("修改关系表订单号错误");
        }


        orderInfo.setOrderNum(newOrderNum);

        if (orderInfo.getCouponUserId() != null) {
            int newMoney = orderInfo.getOrderMoney() - orderInfo.getCouponAmount();

            if (newMoney <= 0) {
                orderInfo.setOrderMoney(1);
            } else {
                orderInfo.setOrderMoney(newMoney);
            }
        }


        if (!orderService.updateOrderById(orderInfo)) {//修改订单号
            throw new DataException("修改订单号错误");
        }

        Map map = new HashMap();

        map.put("orderNum", orderInfo.getOrderNum());

        response.setMap(map);

        return response;
    }


    /**
     * 我的用户一级页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/myUserIndex", method = RequestMethod.POST)
    public MyUserIndexResponse myUserIndex(@RequestBody MyUserIndexRequest request) {
        final MyUserIndexResponse response = new MyUserIndexResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return (MyUserIndexResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        if (request.getYear() == null || request.getMonth() == null
                || request.getDay() == null) {
            return (MyUserIndexResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        String todayDateString = request.getYear() + "-" + request.getMonth() + "-" + request.getDay();

        //所有
        Map<String, String> total = new HashMap();
        total.put("openid", wxUser.getOpenid());

        //当年
        Map<String, String> year = new HashMap();
        year.put("openid", wxUser.getOpenid());
        year.put("year", request.getYear());

        //当月
        Map<String, String> month = new HashMap();
        month.put("openid", wxUser.getOpenid());
        month.put("year", request.getYear());
        month.put("month", request.getMonth());

        //当日
        Map<String, String> day = new HashMap();
        day.put("openid", wxUser.getOpenid());
        day.put("year", request.getYear());
        day.put("month", request.getMonth());
        day.put("day", request.getDay());

        //本周
        Map<String, String> week = new HashMap();
        week.put("openid", wxUser.getOpenid());
        week.put("week", todayDateString);

        Date todayDate = DateUtils.convertString2Date(todayDateString, "yyyy-MM-dd");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        //去年
        Calendar lastYearC = Calendar.getInstance();
        lastYearC.setTime(todayDate);
        lastYearC.add(Calendar.YEAR, -1);
        Date lastYearDate = lastYearC.getTime();
        String lastYearString = format.format(lastYearDate);

        Map<String, String> lastYear = new HashMap();
        lastYear.put("openid", wxUser.getOpenid());
        lastYear.put("lastYear", lastYearString);

        //上月
        Calendar lastMonthC = Calendar.getInstance();
        lastMonthC.setTime(todayDate);
        lastMonthC.add(Calendar.MONTH, -1);
        Date lastMonthDate = lastMonthC.getTime();
        String lastMonthString = format.format(lastMonthDate);

        Map<String, String> lastMonth = new HashMap();
        lastMonth.put("openid", wxUser.getOpenid());
        lastMonth.put("lastMonth", lastMonthString);

        //上周
        Calendar lastWeekC = Calendar.getInstance();
        lastWeekC.setTime(todayDate);
        lastWeekC.add(Calendar.DATE, -7);
        Date lastWeekDate = lastWeekC.getTime();
        String lastWeekString = format.format(lastWeekDate);

        Map<String, String> lastWeek = new HashMap();
        lastWeek.put("openid", wxUser.getOpenid());
        lastWeek.put("lastWeek", lastWeekString);

        //昨日
        Calendar lastDayC = Calendar.getInstance();
        lastDayC.setTime(todayDate);
        lastDayC.add(Calendar.DATE, -1);
        Date lastDayDate = lastDayC.getTime();
        String lastDayString = format.format(lastDayDate);

        Map<String, String> lastDay = new HashMap();
        lastDay.put("openid", wxUser.getOpenid());
        lastDay.put("lastDay", lastDayString);


        response.setTotalNumber(myUserService.countMyUserNumber(total));

        response.setYearNumber(myUserService.countMyUserNumber(year));

        response.setMonthNumber(myUserService.countMyUserNumber(month));

        response.setTodayNumber(myUserService.countMyUserNumber(day));

        response.setWeekNumber(myUserService.countMyUserNumber(week));


        response.setLastYearNumber(myUserService.countMyUserNumber(lastYear));

        response.setLastMonthNumber(myUserService.countMyUserNumber(lastMonth));

        response.setLastWeekNumber(myUserService.countMyUserNumber(lastWeek));

        response.setLastDayNumber(myUserService.countMyUserNumber(lastDay));


        return response;
    }


    /**
     * 订单统计
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/orderStatistics", method = RequestMethod.POST)
    public OrderStatisticsResponse orderStatistics(@RequestBody OrderStatisticsRequest request) {
        OrderStatisticsResponse response = new OrderStatisticsResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return (OrderStatisticsResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("openid", wxUser.getOpenid());
        dayMap.put("year", request.getYear());
        dayMap.put("month", request.getMonth());
        dayMap.put("day", request.getDay());

        //当月
        Map<String, String> month = new HashMap<>();
        month.put("openid", wxUser.getOpenid());
        month.put("year", request.getYear());
        month.put("month", request.getMonth());

        Map<String, String> weekMap = new HashMap<>();
        weekMap.put("openid", wxUser.getOpenid());
        weekMap.put("week", request.getWeek());

        Map<String, String> totalMap = new HashMap<>();
        totalMap.put("openid", wxUser.getOpenid());

        response.setTodayMoney(orderService.countOrderMoney(dayMap));
        response.setTodayNumber(orderService.countOrderNumber(dayMap));

        response.setWeekMoney(orderService.countOrderMoney(weekMap));
        response.setWeekNumber(orderService.countOrderNumber(weekMap));

        response.setMonthMoney(orderService.countOrderMoney(month));
        response.setMonthNumber(orderService.countOrderNumber(month));

        response.setTotalMoney(orderService.countOrderMoney(totalMap));
        response.setTotalNumber(orderService.countOrderNumber(totalMap));

        return response;

    }


    /**
     * 订单统计详情列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/orderStatistics/list", method = RequestMethod.POST)
    public OrderStatisticsListResponse orderStatisticsList(@RequestBody OrderStatisticsListRequest request) {
        OrderStatisticsListResponse response = new OrderStatisticsListResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return (OrderStatisticsListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        request.setOpenid(wxUser.getOpenid());

        PageHelper.startPage(request.getPage(), request.getSize());
        List<OrderStatisticsListDetail> orderStatisticsListDetailList = orderService.selectOrderStatisticsListDetail(request);
        PageInfo<OrderStatisticsListDetail> pageInfo = new PageInfo<>(orderStatisticsListDetailList);


        for (OrderStatisticsListDetail orderInfo : orderStatisticsListDetailList) {
            List<OrderStatisticsListGoodsDetail> list = orderGoodsService.selectOrderStatisticsListGoodsDetail(orderInfo.getOrderNum());

            if (list.size() < 1) {
                return (OrderStatisticsListResponse) response.build(RespStatus.GOODS_NOT_NULL);
            }

            orderInfo.setGoodsList(list);

        }

        response.setOrderList(orderStatisticsListDetailList);
        response.setTotal(pageInfo.getTotal());

        return response;

    }


    /**
     * 获取我的用户订单列表
     * 合并子订单到订单中展示
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/order/myUser/orderList", method = RequestMethod.POST)
    public GetMyUserOrderListResponse myUserOrderList(@RequestBody GetMyUserOrderListRequest request) {

        GetMyUserOrderListResponse response = new GetMyUserOrderListResponse();

        try {
            response = orderService.getMyUserOrderList(request);
        } catch (Exception e) {
            e.printStackTrace();
            return (GetMyUserOrderListResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 获取订单金额
     *
     * @param checkOrderAmount
     * @return
     */
    @RequestMapping(value = "/order/v20/checkOrderAmount", method = RequestMethod.POST)
    public BaseResponse checkOrderAmount(@RequestBody CheckOrderAmountRequest checkOrderAmount) {
        if (null == checkOrderAmount.getCouponId()) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        return orderService.checkOrderAmount(checkOrderAmount.getCouponId(), checkOrderAmount.getSessionId(), checkOrderAmount.getGoodsList());
    }

}
