package com.mhc.yunxian.service;

import com.alibaba.fastjson.JSON;
import com.mhc.yunxian.bean.MessageResponseBody;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.cache.JedisProducer;
import com.mhc.yunxian.cache.LockPool;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.dao.CouponUserDao;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.DeliveryCycleEnum;
import com.mhc.yunxian.enums.DragonDateTypeEnum;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.utils.DateUtils;
import com.mhc.yunxian.utils.JsonUtils;
import com.mhc.yunxian.utils.NoticeManager;
import com.mhc.yunxian.utils.NoticeTaskFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Component
public class TaskService {


    @Autowired
    WxUserService userService;


    @Autowired
    OkHttpClient okHttpClient;

    @Autowired
    DragonService dragonService;

    @Autowired
    PartyInfoService partyInfoService;

    @Autowired
    OrderInfoDao orderInfoDao;

    @Autowired
    OrderService orderService;

    @Autowired
    DragonGoodsService dragonGoodsService;

    @Autowired
    OrderGoodsService orderGoodsService;

    @Autowired
    GoodsInfoService goodsInfoService;

    @Autowired
    SystemParamService systemParamService;

    @Autowired
    private CouponUserDao couponUserDao;

    @Autowired
    private JedisProducer jedisProducer;

    @Autowired
    private SendStationMsgService sendStationMsgService;

    @Value("${wx.yunxian.appid}")
    String wxAppid;

    // @Value("${wx.yunxian.order.pay}")
    /**
     * 跳转路径暂无用
     */
    private String payUrl = "pages/newCustomerOrderDetail/main?orderNum=";

    // @Value("${wx.yunxian.order.seller}")
    /**
     * 通知页面跳转路径
     */
    private String sellerUrl = "pages/newCustomerOrderList/main?who=seller";

    // @Value("${wx.yunxian.dragon}")
    /**
     * 接龙跳转路径
     */
    private String dragonUrl = "pages/newDragonDetail/main?dragonNum=";

    /***
     * 订单跳转路径
     */
    private String orderUrl = "pages/newOrderDetail/main?orderNum=";

    private static final Long ONE_HOUR_TIMEMILLis = 60*60*1000L;
    private static final Long FOUR_HOUR_TIMEMILLis = 4*60*60*1000L;
    private static final Long EIGHT_HOUR_TIMEMILLis = 8*60*60*1000L;
    private static final Long ONE_MIN_OFFSET_TIME = 30*1000L;
    private static final Long FIVE_MIN_OFFSET_TIME = 5*30*1000L;
    private static final Integer ONE_HOUR = 1;
    private static final Integer FOUR_HOUR = 4;
    private static final Integer EIGHT_HOUR = 8;


    @Autowired
    private WxUserService wxUserService;

    //定时扫描数据库,订单未付款15分钟则取消订单
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行一次
    public void scheduledOrder() {
        log.info("扫描未付款订单定时器运行中");

        SystemParam systemParam = systemParamService.selectOneByParamGroup("LOCK_KEY_DRAGON");

        if (null == systemParam) {
            throw new DataException("接龙锁读取失败");
        }

        if ("1".equals(systemParam.getParamValue())) {
            log.info("已加锁。");
            return;
        }

        systemParam.setParamValue("1");//加锁

        if (systemParamService.updateSystemParam(systemParam) < 1) {
            log.error("接龙锁加锁失败");
            return;
        }


        try {

            List<OrderInfo> orderInfos = orderInfoDao.selectOrderByOrderStatus(0);

            for (OrderInfo orderInfo : orderInfos) {
                //获取当前时间毫秒数
                long time = System.currentTimeMillis();
                //时间左偏移
                long leftTime = time - 15 * 60 * 1000;
                if (Optional.ofNullable(orderInfo.getCreateTime()).orElse(new Date(0L)).getTime() < leftTime
                        //补差订单不取消
                        && orderInfo.getParentOrderNum() == null) {
                    orderInfo.setOrderStatus(6);
                    orderService.updateOrder(orderInfo);
                    log.info("更新超时未付款订单，订单号：" + orderInfo.getOrderNum());

                    List<OrderGoodsInfo> orderGoods = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
                    for (OrderGoodsInfo ogInfo : orderGoods) {
                        DragonGoods dragonGoods = dragonGoodsService.getDragonGoodsByGoodsNumAndDragonNum(ogInfo.getGoodsNum(), orderInfo.getDragonNum());

                        if (dragonGoods == null) {
                            log.error("接龙商品不存在：" + ogInfo.getGoodsNum() + "**:" + orderInfo.getDragonNum());
                        }

                        //商品总数量+购买数量
                        dragonGoods.setCurrentNumber(dragonGoods.getCurrentNumber() + ogInfo.getBuyNumber());
                        dragonGoods.setUpdateTime(new Date());

                        GoodsInfo updateTotalNum = new GoodsInfo();
                        updateTotalNum.setGoodsNum(ogInfo.getGoodsNum());
                        updateTotalNum.setTotalNumber(dragonGoods.getCurrentNumber());
                        updateTotalNum.setUpdateTime(new Date());

                        if (!dragonGoodsService.updateDragonGoods(dragonGoods)) {
                            log.error("商品数量更新失败");
                        }

                        if (goodsInfoService.updateGoods(updateTotalNum) == false) {
                            log.error("商品数量更新失败");
                        }

                    }
                    //退红包
                    if (orderInfo.getCouponUserId() != null) {
                        CouponUser couponUser = new CouponUser();
                        couponUser.setIsUsed(0);
                        couponUser.setId(orderInfo.getCouponUserId());
                        couponUserDao.updateByPrimaryKeySelective(couponUser);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        } finally {
            systemParam.setParamValue("0");//解锁

            if (systemParamService.updateSystemParam(systemParam) < 1) {
                log.error("接龙锁解锁失败");
            }

            log.info("更新超时未付款订单结束");
        }
    }

    /**
     * <p> 每1分钟执行一次，已发货订单分别在1，4，8小时提醒买家收货 </p>
     * @param 
     * @return void
     * @author 昊天 
     * @date 2019/1/28 3:47 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Scheduled(cron = "10 */1 * * * ?")
    public void sendReceiptNotice(){
        Jedis jedis = jedisProducer.getJedis();
        if(null == jedis){
            log.warn("无法获取jedis连接");
            return;
        }
        String uuid = UUID.randomUUID().toString();
        boolean lock = LockPool.tryGetDistributeLock(jedis, uuid, uuid, 5 * 1000);
        log.info("开始扫描已发货订单，当前时间{}",new Date());
        try {
            if(lock) {
                //待收货订单
                List<OrderInfo> orders = orderService.getOrdersByStatus(OrderStatusEnum.PENDING_RECEIVED.getCode());
                for (OrderInfo orderInfo : orders){
                    Date orderSendTime = orderInfo.getSendTime();
                    Long sendTime = orderSendTime.getTime();
                    Long nowTime = System.currentTimeMillis();
                    //定时任务每1分钟执行一次，在这之间发货的订单会存在偏差，所以需要加上偏移时间1分钟
                    Long mixTime = nowTime - sendTime;
                    Integer msgSendTime;
                    if(mixTime < (ONE_HOUR_TIMEMILLis + ONE_MIN_OFFSET_TIME) && mixTime > (ONE_HOUR_TIMEMILLis - ONE_MIN_OFFSET_TIME)){
                          msgSendTime = ONE_HOUR;
                    }else if (mixTime < (FOUR_HOUR_TIMEMILLis + ONE_MIN_OFFSET_TIME) && mixTime > (FOUR_HOUR_TIMEMILLis - ONE_MIN_OFFSET_TIME)){
                          msgSendTime = FOUR_HOUR;
                    }else if (mixTime < (EIGHT_HOUR_TIMEMILLis + ONE_MIN_OFFSET_TIME) && mixTime > (EIGHT_HOUR_TIMEMILLis - ONE_MIN_OFFSET_TIME)){
                          msgSendTime = EIGHT_HOUR;
                    }else {
                        continue;
                    }
                    DragonInfo dragon = dragonService.getDragon(orderInfo.getDragonNum());
                    if(Objects.isNull(dragon)){
                        continue;
                    }
                    //发送消息
                    sendStationMsgService.sendReceiptNotice(orderInfo.getOpenid(),dragon.getDragonTitle(),msgSendTime,orderInfo.getOrderNum());
                    log.info("已发送消息通知买家,id:{}",orderInfo.getOpenid());
                }
            }else {
                log.warn("加锁失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放锁
            LockPool.releaseDistributeLock(jedis,uuid,uuid);
            if(null !=jedis){
                jedis.close();
            }
        }
    }

    /**
     * <p> 每5分钟执行一次， 接龙截单时间到了提醒卖家 </p>
     * @param
     * @return void
     * @author 昊天
     * @date 2019/1/28 7:04 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Scheduled(cron = "20 */5 * * * ?")
    public void sendDragonCufNotice(){
        Jedis jedis = jedisProducer.getJedis();
        if(null == jedis){
            log.warn("无法获取jedis连接");
            return;
        }
        String uuid = UUID.randomUUID().toString();
        boolean lock = LockPool.tryGetDistributeLock(jedis, uuid, uuid, 3 * 1000);
        log.info("开始扫描接龙信息，当前时间{}",new Date());
        try {
            if(lock){
                List<DragonInfo> dragonInfos = dragonService.selectDragonByStatusAndCycle();
                for (DragonInfo dragonInfo: dragonInfos) {
                    DeliveryCycle deliveryCycle = JsonUtils.toObj(dragonInfo.getDeliveryCycle(), DeliveryCycle.class);
                    Date time = null;
                    Long cutOffTime;
                    if(DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(deliveryCycle.getCycleType())){
                        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //一次性发货
                        if(StringUtils.isNotBlank(dragonInfo.getEndTime())) {
                            time = sdf.parse(dragonInfo.getEndTime());
                        }
                    }else {
                        //周期性发货
                        time= dragonService.getDragonDeliveryCycleDate(dragonInfo.getDeliveryCycle(), DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME);
                    }
                    if(null != time){
                        cutOffTime = time.getTime();
                        Long nowTime = System.currentTimeMillis();
                        //定时任务每5分钟执行一次，在这之间发货的订单会存在偏差，所以需要加上偏移时间5分钟
                        if (cutOffTime < (nowTime + FIVE_MIN_OFFSET_TIME) && cutOffTime > (nowTime - FIVE_MIN_OFFSET_TIME)) {
                            log.info("已扫描到符合发送消息条件的接龙:{},截单时间为:{}", dragonInfo.getId(), time);
                            sendStationMsgService.sendDragonEndNotice(dragonInfo.getOpenid(), dragonInfo.getDragonTitle(), dragonInfo.getDragonNum());
                        }
                    }
                }
            }else {
                log.warn("加锁失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LockPool.releaseDistributeLock(jedis,uuid,uuid);
            if(null !=jedis){
                jedis.close();
            }
        }
    }

    //----------------------------------发送给消费者-----------------------------------------------------

    /**
     * 付款成功通知（AT0005）
     * 只通知卖家
     * 订单详情页
     *
     * @param orderRequest
     * @param openId
     * @param dragonInfo   活动名称
     *                     {{keyword1.DATA}}
     *                     <p>
     *                     付款时间
     *                     {{keyword2.DATA}}
     *                     <p>
     *                     订单编号
     *                     {{keyword3.DATA}}
     *                     <p>
     *                     付款金额
     *                     {{keyword4.DATA}}
     * @throws Exception
     */
    public void noticeOfDragonSuccess(CommitOrderRequest orderRequest, DragonInfo dragonInfo, String openId) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();

        if (StringUtils.isBlank(accessToken)) {
            return;
        }

        MessageRequest request = new MessageRequest();

        request.setTouser(openId);
        request.setForm_id(orderRequest.getFormId().split(",")[1]);
        request.setTemplate_id("YJdVOuMr7-0T6Qk2ICE0R5uQgQ54TgTX4iHducjyaaI");
        request.setPage("pages/index/main");

        MessageKeyWordRequest data = new MessageKeyWordRequest();


        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(dragonInfo.getDragonTitle());
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal(orderRequest.getOrderNum());
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal((Double.parseDouble(orderRequest.getOrderMoney().toString())) / 100 + "元");
        data.setKeyword4(k4);
        request.setData(data);

        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    /**
     * 发货通知（AT0007）
     * 只通知买家
     * 订单发货提醒
     * 关键词
     * 发货时间
     * {{keyword1.DATA}}
     * <p>
     * 收货地址
     * {{keyword2.DATA}}
     *
     * @param orderInfo
     * @param formId
     * @throws Exception
     */
    public void noticeOfDelivery(OrderInfo orderInfo, String formId) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();
        MessageRequest request = new MessageRequest();

        request.setTouser(orderInfo.getOpenid());
        request.setForm_id(formId);
        request.setTemplate_id("Kdpju-tZH8X9I47vOruejrkmR_pEifQCzlXXh_c9wa8");
        request.setPage(orderUrl + orderInfo.getOrderNum() + "&orderStatus=3&isSeller=0");

        MessageKeyWordRequest data = new MessageKeyWordRequest();

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        if (StringUtils.isNotBlank(orderInfo.getDragonAddr())) {
            k2.setVal(orderInfo.getDragonAddr());
        } else {
            k2.setVal(orderInfo.getAddress());
        }
        data.setKeyword2(k2);


        request.setData(data);

        log.info("发货通知");
        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }

    /**
     * 退款成功通知（AT0036）
     * 只通知买家
     * 关键词
     * 退款金额
     * {{keyword1.DATA}}
     * <p>
     * 退款原因
     * {{keyword2.DATA}}
     * <p>
     * 退款时间
     * {{keyword3.DATA}}
     * <p>
     * 订单编号
     * {{keyword4.DATA}}
     * <p>
     * 活动名称
     * {{keyword5.DATA}}
     *
     * @param orderInfo
     * @param formId
     * @param dragonInfo
     * @throws Exception
     */
    public void noticeOfRefunds(OrderInfo orderInfo, DragonInfo dragonInfo, String formId, String refundMoney) {


        String accessToken = userService.getWxAccessToKenInfo();
        MessageRequest request = new MessageRequest();

        request.setTouser(orderInfo.getOpenid());
        request.setForm_id(formId);
        request.setTemplate_id("RJOzBUAxOZbcYWaUfok_dSgeX0lHdG6S52aFd6Z5kVU");
        request.setPage(orderUrl + orderInfo.getOrderNum() + "&orderStatus=3&isSeller=0");

        MessageKeyWordRequest data = new MessageKeyWordRequest();

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(Double.parseDouble(refundMoney) / 100 + "元");
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal("买家申请退款");
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal(orderInfo.getOrderNum());
        data.setKeyword4(k4);

        KeyWordRequest k5 = new KeyWordRequest();
        k5.setVal(dragonInfo.getDragonTitle());
        data.setKeyword5(k5);

        request.setData(data);

        log.info("退款成功通知");
        // sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    /**
     * 支付成功通知（AT0005） 也用来通知商家
     * <p>
     * 两者都有，如果关注公众号则走公众号通知，服务通知不需要
     * <p>
     * 活动名称
     * {{keyword1.DATA}}
     * <p>
     * 付款时间
     * {{keyword2.DATA}}
     * <p>
     * 订单编号
     * {{keyword3.DATA}}
     * <p>
     * 付款金额
     * {{keyword4.DATA}}
     *
     * @param orderInfo
     * @param openId
     * @param prepayId
     * @throws Exception
     */
    public void noticeOfPayment(OrderInfo orderInfo, String openId, String prepayId, Integer isSeller) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();

        if (StringUtils.isBlank(accessToken)) {
            return;
        }

        MessageRequest request = new MessageRequest();

        request.setTouser(openId);
        request.setForm_id(prepayId);
        request.setTemplate_id("YJdVOuMr7-0T6Qk2ICE0R5uQgQ54TgTX4iHducjyaaI");
        // request.setPage(payUrl + orderInfo.getOrderNum() + "&orderStatus=" + orderInfo.getOrderStatus() + "&isSeller=" + isSeller);
        request.setPage(orderUrl + orderInfo.getOrderNum() + "&orderStatus=3&isSeller=" + isSeller);
        MessageKeyWordRequest data = new MessageKeyWordRequest();


        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal("接龙订单支付成功");
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal(orderInfo.getOrderNum());
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal((Double.parseDouble(orderInfo.getOrderMoney().toString())) / 100 + "元");
        data.setKeyword4(k4);

        request.setData(data);

        log.info("支付成功通知");

        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    //----------------------------------发送给商家-----------------------------------------------------


    /**
     * 接龙发布成功通知（AT0276）
     * 只发送给卖家
     * 标题
     * 信息提交成功通知
     * 关键词
     * 活动名称
     * {{keyword1.DATA}}
     * <p>
     * 提交时间
     * {{keyword2.DATA}}
     * <p>
     * 提交人
     * {{keyword3.DATA}}
     *
     * @param dragonRequesto
     * @param wxUser
     * @throws Exception
     */
    public void noticeOfDragonToSeller(CreateDragonRequest dragonRequesto, WxUser wxUser) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();

        if (StringUtils.isBlank(accessToken)) {
            return;
        }

        MessageRequest request = new MessageRequest();

        request.setTouser(wxUser.getOpenid());
        request.setForm_id(dragonRequesto.getFormId());
        request.setTemplate_id("kefyFDSSYus_C-BUM7-0h_n0x-OZOEzYe4xk_M5gCHU");
        request.setPage(dragonUrl + dragonRequesto.getDragonNum() + "&isSeller=1");

        MessageKeyWordRequest data = new MessageKeyWordRequest();


        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(dragonRequesto.getTitle());//接龙主题
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal(wxUser.getNickName());
        data.setKeyword3(k3);

        request.setData(data);
        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    /**
     * 参与接龙通知（AT0002）
     * 商家成功发布一个接龙后，当有消费者成功提交接龙时，商家收到本消息通知
     *
     * @param
     * @param dragonInfo
     * @throws Exception
     */
    public void noticeOfJoinDragonToSeller(OrderInfo orderInfo, String openid, String formId, DragonInfo dragonInfo) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();
        MessageRequest request = new MessageRequest();

        request.setTouser(openid);
        request.setForm_id(formId);
        request.setTemplate_id("JECG8HmI-zFgnCI2uIdAbxchR0ImmZlbfxehZXfhwko");
        request.setPage("pages/index/main");

        MessageKeyWordRequest data = new MessageKeyWordRequest();
        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(dragonInfo.getDragonTitle());
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(orderInfo.getOrderNum());
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal((Double.parseDouble(orderInfo.getOrderMoney().toString())) / 100 + "元");
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal((Double.parseDouble(orderInfo.getOrderMoney().toString())) / 100 + "元");
        data.setKeyword4(k4);

        KeyWordRequest k5 = new KeyWordRequest();
        k5.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));
        data.setKeyword5(k5);

        request.setData(data);

        log.info("参与接龙通知");
        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    /**
     * 提现成功通知（AT1330）
     * 卖家
     * 当商家发起一个体现请求，提现请求成功执行后，会收到本消息通知。
     * 关键词
     * 关键词
     * 提现金额
     * {{keyword1.DATA}}
     * <p>
     * 时间
     * {{keyword2.DATA}}
     *
     * @param withdrawMoneyRequest
     * @param openId
     * @throws Exception
     */
    public void noticeOfWithdrawToSeller(DrawMoneySuccessRequest withdrawMoneyRequest, String openId) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();

        MessageRequest request = new MessageRequest();

        request.setTouser(openId);
        request.setForm_id(withdrawMoneyRequest.getFormId());
        request.setTemplate_id("0NuzdYiP9ouQgrHbZpijlVou5tq2RN8WsxUMZ-UcRpo");
        request.setPage("pages/index/main");

        MessageKeyWordRequest data = new MessageKeyWordRequest();
        request.setData(data);

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal((Double.parseDouble(withdrawMoneyRequest.getWithdrawMoney().toString())) / 100 + "元");
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));//时间
        data.setKeyword2(k2);


        log.info("提现成功通知");
        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    /**
     * 提现成功通知（AT1330）
     * 只发送给卖家
     * 当商家发起一个体现请求，提现请求成功执行后，会收到本消息通知。
     * 关键词
     * 关键词
     * 提现金额
     * {{keyword1.DATA}}
     * <p>
     * 时间
     * {{keyword2.DATA}}
     *
     * @param
     * @param openId
     * @throws Exception
     */
    public void noticeOfWithdrawToSeller(String formId, Integer drawMoney, String openId) throws Exception {


        String accessToken = userService.getWxAccessToKenInfo();

        MessageRequest request = new MessageRequest();

        request.setTouser(openId);
        request.setForm_id(formId);
        request.setTemplate_id("0NuzdYiP9ouQgrHbZpijlVou5tq2RN8WsxUMZ-UcRpo");
        request.setPage("pages/index/main");

        MessageKeyWordRequest data = new MessageKeyWordRequest();
        request.setData(data);

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal((Double.parseDouble(drawMoney.toString())) / 100 + "元");
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));//时间
        data.setKeyword2(k2);


        log.info("提现成功通知");
        // this.sendMsg(accessToken, request);
        NoticeManager.me().executorNotice(NoticeTaskFactory.sendMsg(accessToken, request));
    }


    //---------------------------------公众号通知-----------------------------------------------------

    /**
     * 微信公众号通知
     * 下单通知(发送卖家)
     * 关键词
     *
     * @param
     * @param openid
     * @throws Exception
     */
    public void gzhOrderNotice(String openid, String goodsName, String nickName, String price, String realPrice, String orderNum) throws Exception {

        String accessToken = userService.getWxGZHAccessToken();
        GZHMessageRequest request = new GZHMessageRequest();

        request.setTouser(openid);
        request.setTemplate_id("ytxfDHDxAKNTdsp0UaG5vi9mfEldVStDvqn44kUCOF0");

        MiniProgram miniProgram = new MiniProgram();
//        miniProgram.setPagepath("pages/customerOrderList/main");
        //miniProgram.setPagepath("pages/orderManage/main");
        miniProgram.setPagepath(sellerUrl);

        miniProgram.setAppid(wxAppid);

        request.setMiniprogram(miniProgram);

        MessageKeyWordRequest data = new MessageKeyWordRequest();
        request.setData(data);

        KeyWordRequest first = new KeyWordRequest();
        first.setVal("有人下单啦！");
        data.setFirst(first);

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(goodsName);
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(nickName);//下单者昵称
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal((Double.parseDouble(price)) / 100 + "元");//消费金额
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal((Double.parseDouble(realPrice)) / 100 + "元");//实际支付
        data.setKeyword4(k4);

        KeyWordRequest k5 = new KeyWordRequest();
        k5.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));//下单时间
        data.setKeyword5(k5);


        sendGZHTemplateMsg(accessToken, request);
        log.info("公众号下单通知(发送卖家)");

        return;
    }


    /**
     * 微信公众号通知
     * 退款申请通知(发送卖家)
     * 关键词
     *
     * @param
     * @param openid
     * @throws Exception
     */
    public void gzhOrderRefundNotice(String openid, String orderNum, String price, String nickName) throws Exception {


        String accessToken = userService.getWxGZHAccessToken();

        if (StringUtils.isBlank(accessToken)) {
            log.error("getWxGZHAccessToken failure");
            return;
        }

//        MessageRequest request = new MessageRequest();
        GZHMessageRequest request = new GZHMessageRequest();

        request.setTouser(openid);
        request.setTemplate_id("n4f0ZWE6aY16J0hm3luWZqiuVW4EMZUajUzNU3LlHt8");
//        request.setPagepath("pages/customerOrderList/main");
//        request.setAppid("wxc38768c84fd437b6");

        MiniProgram miniProgram = new MiniProgram();
        miniProgram.setPagepath(sellerUrl);
        miniProgram.setAppid(wxAppid);

        request.setMiniprogram(miniProgram);

        MessageKeyWordRequest data = new MessageKeyWordRequest();
        request.setData(data);

        KeyWordRequest first = new KeyWordRequest();
        first.setVal("您有一笔退款申请 请及时处理！");
        data.setFirst(first);

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(orderNum);//订单号
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal((Double.parseDouble(price)) / 100 + "元");//订单金额
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal(nickName);//买家昵称
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal("申请退款");//订单状态
        data.setKeyword4(k4);

        KeyWordRequest k5 = new KeyWordRequest();
        k5.setVal("无");//退款原因
        data.setKeyword5(k5);

        sendGZHTemplateMsg(accessToken, request);
        log.info("公众号退款申请通知(发送卖家)");

        return;
    }


    /**
     * 发起提现通知(发送运营人员)
     *
     * @param
     * @param openId
     * @throws Exception
     */
    public void addDrawMoneyMessageToAdmin(String nickName, Integer drawMoney, String openId, Integer drawMoneyId) throws Exception {


        String accessToken = userService.getWxGZHAccessToken();

        if (StringUtils.isBlank(accessToken)) {
            log.error("getWxGZHAccessToken failure");
            return;
        }

//        MessageRequest request = new MessageRequest();
        GZHMessageRequest request = new GZHMessageRequest();

        request.setTouser(openId);
        request.setTemplate_id("m5CmWPLZCcx4CYBWugBfdwvuw8vogdpx1M1A3Yf6z_w");
        request.setUrl("https://h5.yunxian.shop?drawmoneyid=" + drawMoneyId);

        MessageKeyWordRequest data = new MessageKeyWordRequest();
        request.setData(data);

        KeyWordRequest first = new KeyWordRequest();
        first.setVal("有新的提现申请，请及时处理。");
        data.setFirst(first);

        KeyWordRequest k1 = new KeyWordRequest();
        k1.setVal(nickName);
        data.setKeyword1(k1);

        KeyWordRequest k2 = new KeyWordRequest();
        k2.setVal(DateUtils.getCurrentStringDate(DateUtils.LONG_DATE_FORMAT));//时间
        data.setKeyword2(k2);

        KeyWordRequest k3 = new KeyWordRequest();
        k3.setVal((Double.parseDouble(drawMoney.toString())) / 100 + "元");//金额
        data.setKeyword3(k3);

        KeyWordRequest k4 = new KeyWordRequest();
        k4.setVal("支付到微信零钱");//方式
        data.setKeyword4(k4);


        sendGZHTemplateMsg(accessToken, request);
        log.info("发起提现通知(发送运营人员)");

        return;
    }

  /*  public void sendMsg(String accessToken, MessageRequest request) throws Exception {
        okhttp3.Response httpResponse = okHttpClient.newCall(new Request.Builder()
                .url("https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=" + accessToken)
                .post(okhttp3.RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(request)))
                .build()).execute();
        ResponseBody body = httpResponse.body();
        Boolean flag = this.parseMessageResponse(body);
        if (!flag) {
            Thread.sleep(600000);
            this.sendMsg(wxUserService.getWxAccessToKenInfo(), request);
        }

    }*/

 /*   private Boolean parseMessageResponse(ResponseBody body) throws IOException {
        if (body != null) {
            MessageResponseBody messageResponseBody = JsonUtils.toObj(body.string(), MessageResponseBody.class);
            if ("ok".equalsIgnoreCase(messageResponseBody.getErrmsg()) && Integer.valueOf(0).equals(messageResponseBody.getErrcode())) {
                log.info("---------------------------------服务通知发送成功!----------------------------------------");
                return Boolean.TRUE;
            }
            log.error("-----------服务通知发送失败----------------错误信息{}", messageResponseBody.getErrmsg());
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }*/

    //公众号消息
    public void sendGZHTemplateMsg(String accessToken, GZHMessageRequest request) throws Exception {
        int i = 0;
        do {
            okhttp3.Response httpResponse = okHttpClient.newCall(new Request.Builder()
                    .url("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken)
                    .post(okhttp3.RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(request)))
                    .build()).execute();

            if (httpResponse != null) {
                String resultMsg = httpResponse.body().string();
                if (resultMsg.indexOf("ok") > -1) {
                    log.warn("消息通知成功");
                } else {
                    log.warn("消息通知失败:" + resultMsg);
                }
                break;
            }
            i++;
            Thread.sleep(1000);
        } while (i <= 1);
    }

    //定时扫描数据库,接龙下架时间到了则下架接龙
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 */5 * * * ?")//每5分钟执行一次
    public void scheduledEndDragon() {
        Jedis jedis = jedisProducer.getJedis();
        if(null == jedis){
            log.warn("无法获取jedis连接");
            return;
        }
        String uuid = UUID.randomUUID().toString();
        boolean lock = LockPool.tryGetDistributeLock(jedis, uuid, uuid, 2000);
        try {
            if (lock) {
                List<DragonInfo> dragonInfoList = dragonService.getAllDragonInfo();
                for (DragonInfo dragonInfo : dragonInfoList) {

                    DragonInfo state = new DragonInfo();
                    Date currentTime = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    Date endTime = null;
                    if (StringUtils.isNotBlank(dragonInfo.getEndTime())) {
                        endTime = formatter.parse(dragonInfo.getEndTime());
                    }
                    if (StringUtils.isNotBlank(dragonInfo.getDeliveryCycle())) {
                        DeliveryCycle cycle = JsonUtils.toObj(dragonInfo.getDeliveryCycle(), DeliveryCycle.class);
                        if (DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(cycle.getCycleType()) && endTime != null) {
                            boolean res = endTime.before(currentTime);
                            if (res && dragonInfo.getDragonStatus() == 0) {
                                state.setDragonNum(dragonInfo.getDragonNum());
                                state.setDragonStatus(1);
                                if (dragonService.updateDragonStatus(state)) {
                                    log.info("下架接龙成功:{}", dragonInfo.getDragonNum());
                                }
                            }
                        }

                    } else {
                        if (endTime != null) {
                            boolean res = endTime.before(currentTime);

                            if (res && dragonInfo.getDragonStatus() == 0) {
                                state.setDragonNum(dragonInfo.getDragonNum());
                                state.setDragonStatus(1);
                                dragonService.updateDragonStatus(state);
                                log.info("下架接龙成功:{}", dragonInfo.getDragonNum());
                            }
                        }
                    }


                }
            } else {
                log.info("锁获取失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("下架接龙失败：{}", e.getMessage());
        } finally {
            LockPool.releaseDistributeLock(jedis, uuid, uuid);
            if(null !=jedis){
               jedis.close();
            }
        }
    }
}
