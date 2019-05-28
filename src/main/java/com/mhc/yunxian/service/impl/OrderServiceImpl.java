package com.mhc.yunxian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.admin.OrderDetailsRequest;
import com.mhc.yunxian.bean.pay.RefundMoneyRequest;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.*;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.*;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    OrderInfoDao orderInfoDao;

    @Autowired
    WxUserDao wxUserDao;

    @Autowired
    PartyInfoDao partyInfoDao;

    @Autowired
    OrderGoodsDao orderGoodsDao;

    @Autowired
    DragonInfoDao dragonInfoDao;

    @Autowired
    GoodsInfoDao goodsInfoDao;

    @Autowired
    PartyInfoService partyInfoService;

    @Autowired
    DragonService dragonService;

    @Autowired
    OrderGoodsService orderGoodsService;

    @Autowired
    GoodsInfoService goodsInfoService;

    @Autowired
    TaskService taskService;

    @Autowired
    BalanceService balanceService;

    @Autowired
    MoneyRecordService moneyRecordService;

    @Autowired
    RefundRecordService refundRecordService;

    @Autowired
    SystemParamDao systemParamDao;

    @Autowired
    DragonGoodsDao dragonGoodsDao;

    @Autowired
    CouponUserDao couponUserDao;

    @Autowired
    CouponDao couponDao;

    @Autowired
    ShopMapper shopMapper;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private SendStationMsgService sendStationMsgService;

    @Autowired
    private MsgRecordService msgRecordService;

    @Autowired
    private DragonGoodsService dragonGoodsService;


    @Autowired
    private LogisticsService logisticsService;

    private String appid = "wxc38768c84fd437b6";

    private String secret = "638f506b89aa993c2576cc8c2da4b1b3";

    private String mchId = "1504015191";

    private String secretKey = "3233jkdddDDF454Fddddf2seFDEDd3dD";

    private String tradeType = "JSAPI";

    private String signType = "MD5";

    private String refundOrderUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";


    @Override
    public List<OrderInfo> getOpenidByDragonNum(String dragonNum) {
        return orderInfoDao.selectOrder(dragonNum);
    }

    @Override
    public int getAllOrder() {
        return orderInfoDao.selectAllOrder();
    }

    @Override
    public boolean addOrder(OrderInfo orderInfo) {

        return orderInfoDao.insertSelective(orderInfo) > 0;
    }

    @Override
    public List<OrderInfo> getOrdersByOrderStatus(String openid, Integer orderStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOpenid(openid);
        orderInfo.setOrderStatus(orderStatus);

        if (null == orderStatus) {
            return orderInfoDao.selectOrderByOpenid(openid);
        }
        return orderInfoDao.getOrdersByOrderStatus(orderInfo);


    }

    @Override
    public boolean modifyOrderStatusByOrderNumAndOpenid(OrderInfo orderInfo) {
        if (orderInfoDao.modifyOrderStatusByOrderNum(orderInfo) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean updateOrder(OrderInfo orderInfo) {
        return orderInfoDao.updateOrder(orderInfo) > 0;
    }

    @Override
    public boolean updateOrderById(OrderInfo orderInfo) {
        return orderInfoDao.updateOrderById(orderInfo) > 0;
    }

    @Override
    public List<OrderInfo> getOrderByOpenid(String openid) {
        return orderInfoDao.selectOrderByOpenid(openid);
    }

    @Override
    public List<OrderInfo> selectOrderByOpenidAndStatus(String openid, Integer orderStatus) {
        return orderInfoDao.selectOrderByOpenidAndStatus(openid, String.valueOf(orderStatus));
    }

    @Override
    public List<OrderInfo> selectOrderByDragonNumAndStatus(String dragonNum, Integer orderStatus) {
        return orderInfoDao.selectOrderByDragonNumAndStatus(dragonNum, String.valueOf(orderStatus));
    }

    @Override
    public List<OrderInfo> selectCompeletedOrderByDragonNum(String dragonNum) {
        return orderInfoDao.selectCompeletedOrderByDragonNum(dragonNum);
    }

    @Override
    public List<OrderInfo> getOrder() {
        return orderInfoDao.selectOrderAll();
    }

    @Override
    public List<OrderInfo> getOrderBySellerId(String openid) {
        return orderInfoDao.selectOrderBySellerId(openid);
    }

    @Override
    public OrderInfo getOrderByOrderNum(String orderNum) {
        return orderInfoDao.selectOrderByOrderNum(orderNum);
    }

    @Override
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public void add(CommitOrderRequest request) {
        //查询买家openid
        WxUser wxUser = wxUserDao.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            log.error("用户不存在");
            throw new DataException("用户不存在");
        }
        request.setOpenid(wxUser.getOpenid());
        DragonInfo dragonInfo = dragonInfoDao.selectDragon(request.getDragonNum());
        if (dragonInfo == null) {
            log.error("接龙不存在");
            throw new DataException("接龙不存在");
        }
        request.setSellOpenid(dragonInfo.getOpenid());
        if (request.getData() == null) {
            log.error("获取请求数据失败");
            throw new DataException("获取请求数据失败");
        }
        List<GoodsInfo> goodsInfoList = request.getData();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (goodsInfo.getTotalNumber() == 0) {
                continue;
            }
            final OrderGoodsInfo info = new OrderGoodsInfo();
            info.setGoodsNum(goodsInfo.getGoodsNum());
            info.setBuyNumber(goodsInfo.getTotalNumber());
            info.setCreateTime(new Date());
            info.setPrice(goodsInfo.getPrice());
            info.setOpenid(request.getOpenid());
            info.setOrderNum(request.getOrderNum());
            info.setGoodsImg(goodsInfo.getGoodsImg());
            info.setGoodsName(goodsInfo.getGoodsName());
            info.setLimitBuyNum(goodsInfo.getLimitBuyNum());
            info.setRealPrice(goodsInfo.getPrice());
            if (orderGoodsDao.insertSelective(info) < 1) {
                log.error("添加商品订单关联信息错误");
                throw new DataException("添加商品订单关联信息错误");
            }
            List<DragonGoods> dragonGoodsList = dragonGoodsService.getByGoodsNum(goodsInfo.getGoodsNum());
            dragonGoodsList.forEach(x->{
                x.setCurrentNumber(x.getCurrentNumber() - goodsInfo.getBuyNumber());
                x.setUpdateTime(new Date());
                dragonGoodsService.updateDragonGoods(x);
            });

            GoodsInfo goods = goodsInfoService.getGoods(goodsInfo.getGoodsNum());
            goods.setTotalNumber(goods.getTotalNumber() - goodsInfo.getBuyNumber());
            goods.setUpdateTime(new Date());
            if (goodsInfoService.updateGoods(goods) == false) {
                log.error("商品数量更新失败");
            }
        }
        final OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateTime(new Date());//订单创建时间
        orderInfo.setDragonNum(request.getDragonNum());
        orderInfo.setOpenid(request.getOpenid());
        orderInfo.setOrderNum(request.getOrderNum());
        // 获取传入的订单付款金额
        Integer orderMoney = request.getOrderMoney();
        if (request.getCouponUserId() != null) {
            CouponUser couponUser = couponUserDao.selectByPrimaryKey(request.getCouponUserId());
            if (couponUser == null || couponUser.getIsUsed() == 1) {
                log.error("未查询到券用户信息券id{}", request.getCouponUserId());
                throw new DataException("红包信息错误，下单回滚");
            }
            Coupon coupon = couponDao.selectByPrimaryKey(couponUser.getCouponId());
            if (coupon == null) {
                log.error("红包信息错误");
                throw new DataException("红包信息错误，下单回滚");
            }
            // 计算券的优惠量
            Integer discountAmount = this.calculateDiscountAmount(coupon, goodsInfoList);
            //计算商品总金额
            Integer totalMoney = 0;
            for (GoodsInfo goodsInfo : goodsInfoList) {
                totalMoney += goodsInfo.getBuyNumber() * goodsInfo.getPrice();
            }
            // 如果前端计算的实付款与后端计算不相等,则返回错误
            if (totalMoney - discountAmount != orderMoney) {
                throw new DataException("前端计算的实付款与后端计算不相等");
            }
            orderInfo.setCouponUserId(request.getCouponUserId());
            orderInfo.setCouponAmount(coupon.getCouponAmount());
            couponUser.setIsUsed(1);
            if (couponUserDao.updateByPrimaryKeySelective(couponUser) < 1) {
                log.error("红包关联表更新错误{}", JsonUtils.toJson(couponUser));
                throw new DataException("红包关联表更新错误，下单回滚");
            }
        }
        orderInfo.setOrderMoney(orderMoney);
        orderInfo.setOrderRemark(request.getOrderRemark());
        orderInfo.setIsCod(dragonInfo.getCashOnDelivery());
        orderInfo.setIsPayLater(dragonInfo.getIsPayLater());
        //待付款
        orderInfo.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        orderInfo.setAddress(request.getAddress() == null ? "无" : request.getAddress());
        orderInfo.setAddrName(request.getAddrName());
        orderInfo.setAddrPhone(request.getAddrPhone());
        orderInfo.setDragonAddr(request.getDragonAddr());
        // 保存配送方式
        orderInfo.setIsDelivery(request.getDeliveryType());
        if (StringUtils.isBlank(request.getOrderType())) {
            orderInfo.setOrderType(OrderTypeEnum.NORMAL_ORDER.getCode());
        }
        if (orderInfoDao.insertSelective(orderInfo) < 1) {
            log.error("添加订单信息错误");
            throw new DataException("添加订单信息错误");
        }

        //插入到参与表
        final PartInfo partInfo = new PartInfo();
        partInfo.setOpenid(request.getOpenid());
        partInfo.setOrderNum(request.getOrderNum());
        partInfo.setFormId(request.getFormId());
        partInfo.setCreateTime(new Date());
        partInfo.setDragonNum(request.getDragonNum());
        partInfo.setUserType(1);//参与者
        if (partyInfoDao.insertParty(partInfo) < 1) {
            log.error("添加参与信息错误");
            throw new DataException("添加参与信息错误");
        }
    }


    @Override
    public List<OrderInfo> getOrderByOpenidAndDragonNum(String openid, String dragonNum) {
        Map<String, String> map = new HashMap<>();
        map.put("openid", openid);
        map.put("dragonNum", dragonNum);
        return orderInfoDao.selectOrderByOpenidAndDragonNum(map);
    }

    @Override
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public boolean createDifOrder(AddDifOrderRequest request) {
        OrderInfo orderInfo = orderInfoDao.selectOrderByOrderNum(request.getParentOrderNum());

        DragonInfo dragonInfo = dragonInfoDao.selectDragon(orderInfo.getDragonNum());


        //生成商品
        GoodsInfo goodsInfo = new GoodsInfo();
        goodsInfo.setGoodsNum(KeyTool.createOrderNo());
        goodsInfo.setGoodsName(dragonInfo.getDragonTitle() + "-按重调价");
        goodsInfo.setPrice(request.getPrice());
        goodsInfo.setTotalNumber(1);
        goodsInfo.setGoodsImg("https://image.yunxian.shop/images/wxfile://tmp_9a4ff2d1b6f48042110e93eb557182e8.jpg");

        //生成补差订单
        orderInfo.setId(null);
        orderInfo.setOrderStatus(request.getType());
        orderInfo.setUpdateTime(null);
        orderInfo.setOrderMoney(request.getPrice());
        orderInfo.setCreateTime(new Date());
        orderInfo.setOrderNum(KeyTool.createOrderNo());
        orderInfo.setParentOrderNum(request.getParentOrderNum());
        orderInfo.setOrderRemark(request.getOrderRemark());
        orderInfo.setSellerDesc(request.getSellerDesc());
        orderInfo.setOrderType(request.getOrderType());

        //生成订单商品
        OrderGoodsInfo info = new OrderGoodsInfo();
        info.setGoodsNum(goodsInfo.getGoodsNum());
        info.setBuyNumber(1);
        info.setCreateTime(new Date());
        info.setPrice(request.getPrice());
        info.setOpenid(orderInfo.getOpenid());
        info.setOrderNum(orderInfo.getOrderNum());
        info.setGoodsName(goodsInfo.getGoodsName());
        info.setGoodsImg(goodsInfo.getGoodsImg());
        info.setRealPrice(request.getPrice());

        if (goodsInfoDao.insertGoods(goodsInfo) < 1) {
            throw new DataException("添加商品信息错误");
        }

        if (orderInfoDao.insertSelective(orderInfo) < 1) {
            throw new DataException("添加商品订单信息错误");
        }

        if (orderGoodsDao.insertSelective(info) < 1) {
            throw new DataException("添加商品订单关联信息错误");
        }

        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = SendMessageUtil.sendSmsDif(request.getPrice(),orderInfo.getAddrPhone(), dragonInfo.getDragonTitle(),request.getOrderType());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("短信发送失败:" + orderInfo.getAddrPhone());
            log.error("msg:" + e);
        }

        if (!sendSmsResponse.getCode().equals("OK")) {
            log.error("短信发送失败:" + orderInfo.getAddrPhone());
            log.error("msg:" + sendSmsResponse.getMessage());
        }

        //若为退差价订单，执行退款操作
        if (request.getType().equals(OrderStatusEnum.REFUNDING.getCode())) {
            RefundMoneyRequest refundMoneyRequest = new RefundMoneyRequest();
            refundMoneyRequest.setOrderNum(orderInfo.getOrderNum());
            this.refundOrder(refundMoneyRequest, 1);//来自补差价退款
        }
        return true;
    }

    @Override
    public OrderInfo getDifOrderByParentOrderNum(String parentOrderNum) {
        return orderInfoDao.selectOrderByParentOrderNum(parentOrderNum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = {IOException.class, InterruptedException.class})
    public BaseResponse refundOrder(RefundMoneyRequest refundMoneyRequest, int state) {
        final BaseResponse response = new BaseResponse();

        //防止重复退款
        synchronized (refundMoneyRequest.getOrderNum().intern()) {
            //获取订单信息
            final OrderInfo orderInfo = this.getOrderByOrderNum(refundMoneyRequest.getOrderNum());
            log.debug("------微信退款------,订单数据：{}", JSONObject.toJSON(orderInfo));
            if (orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDED.getCode())) {
                return response.build(RespStatus.ORDER_ALREADY_REFUNDED);
            }
            if (!orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDING.getCode())) {
                return response.build(RespStatus.ILLEGAL_ARGUMENT);
            }
            OrderInfo parentOrderInfo = new OrderInfo();
            if (null != orderInfo.getParentOrderNum() && state == 1) {
                //若当前订单为补差订单，获取父订单
                parentOrderInfo = this.getOrderByOrderNum(orderInfo.getParentOrderNum());
            }
            try {
                //生成的随机字符串
                String nonce_str = RandomCodeUtil.getRandomCode(32);
                //支付金额，单位：分，这边需要转成字符串类型，否则后面的签名会失败
                String total_fee;
                String refund_fee;
                if (null != orderInfo.getParentOrderNum() && state == 1) {
                    //总金额为父订单总金额
                    total_fee = parentOrderInfo.getOrderMoney() + "";
                } else {
                    total_fee = orderInfo.getOrderMoney() + "";
                }
                OrderInfo difOrderInfo = this.getDifOrderByParentOrderNum(orderInfo.getOrderNum());
                if (null != difOrderInfo && difOrderInfo.getOrderStatus() == OrderStatusEnum.REFUNDED.getCode() && state == 0
                        && difOrderInfo.getUpdateTime() == null) {
                    refund_fee = (orderInfo.getOrderMoney() - difOrderInfo.getOrderMoney()) + "";
                    //若当前订单存在补差退款订单(补差退款订单updateTime为null)(非补差支付再退款)，退款金额需要减去补差订单的退款金额
                } else {
                    refund_fee = orderInfo.getOrderMoney() + "";
                }
                String out_trade_no;
                if (null != orderInfo.getParentOrderNum() && state == 1) {
                    //若当前订单为补差订单，退款订单号为父订单号
                    out_trade_no = parentOrderInfo.getOrderNum();
                } else {
                    out_trade_no = orderInfo.getOrderNum();
                }
                String out_refund_no = "";
                if (orderInfo.getTransactionId() == null) {
                    out_refund_no = KeyTool.createOrderNo();
                } else {
                    out_refund_no = orderInfo.getTransactionId();
                }
                Map<String, String> packageParams = new HashMap<String, String>();
                packageParams.put("appid", appid);
                packageParams.put("mch_id", mchId);
                packageParams.put("nonce_str", nonce_str);
                //商户订单号
                packageParams.put("out_trade_no", out_trade_no);
                //支付金额，这边需要转成字符串类型，否则后面的签名会失败
                packageParams.put("total_fee", total_fee);
                packageParams.put("refund_fee", refund_fee);
                //支付成功回调url
                //packageParams.put("notify_url", notify_url);
                packageParams.put("out_refund_no", out_refund_no);
                // 除去数组中的空值和签名参数
                packageParams = PaymentUtil.paraFilter(packageParams);
                // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
                String prestr = PaymentUtil.createLinkString(packageParams);
                //MD5运算生成签名
                String mysign = PaymentUtil.sign(prestr, secretKey, "utf-8").toUpperCase();
                //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去
                String xml = "<xml>" + "<appid>" + appid + "</appid>"
                        + "<mch_id>" + mchId + "</mch_id>"
                        + "<nonce_str>" + nonce_str + "</nonce_str>"
                        + "<out_trade_no>" + out_trade_no + "</out_trade_no>"
                        + "<out_refund_no>" + out_refund_no + "</out_refund_no>"
                        + "<total_fee>" + total_fee + "</total_fee>"
                        + "<refund_fee>" + refund_fee + "</refund_fee>"
                        + "<sign>" + mysign + "</sign>"
                        + "</xml>";

                System.out.println("调试模式_退款接口 请求XML数据：" + xml);
                //执行退款
                String result = doRefund(refundOrderUrl, xml);
                System.out.println("调试模式_退款接口 返回XML数据：" + result);
                // 将解析结果存储在HashMap中
                Map map = PaymentUtil.doXMLParse(result);
                //返回状态码
                String return_code = (String) map.get("return_code");
                String result_code = (String) map.get("result_code");
                String err_code_des = (String) map.get("err_code_des");
                String alreadyRefunded = "订单已全额退款";
                String success = "SUCCESS";
                //返回给移动端需要的参数
                if (success.equals(return_code) && success.equals(result_code)) {
                    this.updateRefundInfo(orderInfo, out_refund_no, refund_fee);
                } else {
                    if (alreadyRefunded.equals(err_code_des)) {
                        response.build(RespStatus.ORDER_ALREADY_REFUNDED);
                        this.updateRefundInfo(orderInfo, out_refund_no, refund_fee);
                    } else {
                        response.build(RespStatus.BACK_MONEY_FILE);
                    }
                }
                //异步线程池更新/用户管理复购缓存/商品复购缓存/店铺首页
                ExecutorService service = Executors.newSingleThreadExecutor();
                DragonInfo dragonInfo = dragonInfoDao.selectDragon(orderInfo.getDragonNum());
                service.execute(()->{
                    //用户
                    statisticsService.updateUserRepurchase(orderInfo.getOpenid(),dragonInfo.getOpenid());
                    //商品
                    List<OrderGoodsInfo> orderGoodsInfos = orderGoodsDao.select(orderInfo.getOrderNum());
                    orderGoodsInfos.forEach(x->{
                        statisticsService.updateGoodsRepurchase(x.getGoodsNum());
                    });
                    //店铺
                    statisticsService.updateShopRepurchase(dragonInfo.getOpenid());
                });
                service.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
                // throw new DataException("微信退款失败");
            }
        }

        return response;
    }

    /**
     * 退款更新相关信息
     */
    private void updateRefundInfo(OrderInfo orderInfo, String out_refund_no, String refund_fee) {
        //修改订单状态
        orderInfo.setOrderStatus(OrderStatusEnum.REFUNDED.getCode());
        //退款单号
        orderInfo.setTransactionId(out_refund_no);
        this.updateOrder(orderInfo);
        //更新退款申请记录
        RefundRecord latestRefundRecord = refundRecordService.findLatestRefundRecord(orderInfo.getId());
        if (latestRefundRecord != null) {
            latestRefundRecord.setStatus(RefundStatusEnum.REFUNDED.getCode());
            latestRefundRecord.setUpdateTime(new Date());
            refundRecordService.updateRefundRecord(latestRefundRecord);
        }
        final DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());
        final PartInfo partInfo;
        final List<OrderGoodsInfo> orderGoods;
        if (null != orderInfo.getParentOrderNum()) {
            //补差价退款
            partInfo = partyInfoService.getPartByOrderNum(orderInfo.getParentOrderNum());
        } else {  //正常退款
            partInfo = partyInfoService.getPartByOrderNum(orderInfo.getOrderNum());

            //退款成功，商品数量增加,销量减少
            orderGoods = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
            for (OrderGoodsInfo ogInfo : orderGoods) {
                final GoodsInfo info = goodsInfoService.getGoods(ogInfo.getGoodsNum());
                //商品总数量+购买数量
                info.setTotalNumber(info.getTotalNumber() + ogInfo.getBuyNumber());
                info.setUpdateTime(new Date());
                info.setSalesVolume(info.getSalesVolume() - ogInfo.getBuyNumber());
                if (!goodsInfoService.updateGoods(info)) {
                    log.error("商品销量更新失败");
                    throw new DataException("商品销量更新失败");
                }
                DragonGoods dragonGoods = dragonGoodsDao.selectByGoodsNumAndDragonNum(orderInfo.getDragonNum(), ogInfo.getGoodsNum());
                dragonGoods.setCurrentNumber(dragonGoods.getCurrentNumber() + ogInfo.getBuyNumber());
                dragonGoods.setUpdateTime(new Date());

                GoodsInfo updateTotalNum = new GoodsInfo();
                updateTotalNum.setGoodsNum(dragonGoods.getGoodsNum());
                updateTotalNum.setTotalNumber(dragonGoods.getCurrentNumber());
                updateTotalNum.setUpdateTime(new Date());

                if (!goodsInfoService.updateGoods(updateTotalNum)) {
                    log.error("商品数量更新失败");
                }

                if (dragonGoodsDao.updateByPrimaryKeySelective(dragonGoods) < 1) {
                    log.error("商品库存更新失败");
                    throw new DataException("商品库存更新失败");
                }
            }
            //退红包
            //查询该笔交易是否使用红包
            if (orderInfo.getCouponUserId() != null) {
                //使用红包
                CouponUser couponUser = couponUserDao.selectByPrimaryKey(orderInfo.getCouponUserId());
                if (couponUser == null) {
                    throw new DataException("红包不存在");
                }
                //更新红包为未使用
                couponUser.setIsUsed(0);
                couponUserDao.updateByPrimaryKeySelective(couponUser);
            }
        }
        final Balance sellMoney = balanceService.getBalance(dragonInfo.getOpenid());
        //老数据
        //商家余额减少，更新商家余额
        final Balance balance = new Balance();
        balance.setOpenid(dragonInfo.getOpenid());
        balance.setUpdateTime(new Date());
        balance.setBalance(sellMoney.getBalance() - Integer.valueOf(refund_fee));
        if (!balanceService.updateBalanceByOpenid(balance)) {
            log.error("商家余额更新错误");
            throw new DataException("商家余额更新错误");
        }
        //插入商家交易明细表
        final MoneyRecord moneyRecord = new MoneyRecord();
        moneyRecord.setCreateTime(new Date());
        moneyRecord.setMoney(Integer.valueOf(refund_fee));
        //退款
        moneyRecord.setRecordType(MoneyRecordEnum.REFUNED.getCode());
        moneyRecord.setCause(MoneyRecordEnum.REFUNED.getMsg());
        moneyRecord.setOpenid(dragonInfo.getOpenid());
        moneyRecord.setBalance(String.valueOf((sellMoney.getBalance() - Integer.valueOf(refund_fee))));
        moneyRecord.setOrderNum(orderInfo.getOrderNum());
        if (!moneyRecordService.add(moneyRecord)) {
            log.error("插入交易表错误");
            throw new DataException("插入交易表错误");
        }
        WxUser seller = wxUserDao.selectByOpenid(dragonInfo.getOpenid());
        if (null == seller) {
            throw new DataException("用户表获取错误，回滚");
        }
        seller.setOrderNumber(seller.getOrderNumber() - 1);
        if (wxUserDao.updateUserByOpenid(seller) < 1) {
            throw new DataException("更新用户表错误，回滚");
        }
        //退款成功 发送站内消息
        sendStationMsgService.sendRefundSuccessNotice(orderInfo.getOpenid(),dragonInfo.getDragonTitle(),Double.parseDouble(refund_fee)/100);
        //退款成功，通知用户
        try {
            taskService.noticeOfRefunds(orderInfo, dragonInfo, partInfo.getPrepayId(), refund_fee);
        } catch (Exception e) {
            log.error("退款通知失败");
        }
    }

    /**
     * 微信退款请求
     *
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    private String doRefund(String url, String data) throws Exception {
        System.out.println("进入微信退款请求函数");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        //todo 该证书目前使用的是本地证书，线上测试时请把证书上传到线上服务器，并换成线上服务器的路径
        //本地
        //FileInputStream is = new FileInputStream(new File("/Users/mhc/Desktop/apiclient_cert.p12"));
        //测试服
        FileInputStream is = new FileInputStream(new File("/usr/local/yunxian/apiclient_cert.p12"));
        try {
            keyStore.load(is, "1504015191".toCharArray());
        } finally {
            is.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(
                keyStore,
                "1504015191".toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
        );
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {
            HttpPost httpost = new HttpPost(url); // 设置响应头信息
            httpost.addHeader("Connection", "keep-alive");
            httpost.addHeader("Accept", "*/*");
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpost.addHeader("Host", "api.mch.weixin.qq.com");
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");
            httpost.addHeader("Cache-Control", "max-age=0");
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
            httpost.setEntity(new StringEntity(data, "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httpost);
            try {
                HttpEntity entity = response.getEntity();

                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                EntityUtils.consume(entity);
                return jsonStr;
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Override
    public Integer countOrderNumByDragon(String dragonNum) {

        Integer count = 0;

        List<OrderInfo> orderInfos = orderInfoDao.selectOrder(dragonNum);

        for (OrderInfo e : orderInfos) {
            if (e.getOrderStatus().equals(OrderStatusEnum.COMPELETED.getCode())
                    && e.getParentOrderNum() == null) {
                count++;
            }
        }
        DragonInfo subDragonInfo = dragonInfoDao.selectDragon(dragonNum);

        DragonInfo parentDragonInfo = dragonInfoDao.selectDragonBySubDragonNum(subDragonInfo.getDragonNum());

        if (null == parentDragonInfo) {
            return count;
        } else {
            return countOrderNumByDragon(parentDragonInfo.getDragonNum()) + count;
        }


    }

    @Override
    public PageInfo<OrderInfo> selectAdminOrder(OrderDetailsRequest request) {

        if (StringUtils.isNotBlank(request.getEndDate())) {

            Date tomorrow = DateUtils.convertString2Date(request.getEndDate(), "yyyy-MM-dd");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            Calendar tomorrowC = Calendar.getInstance();
            tomorrowC.setTime(tomorrow);
            tomorrowC.add(Calendar.DATE, 1);
            Date tomorrowDate = tomorrowC.getTime();
            request.setEndDate(format.format(tomorrowDate));
        }


        PageHelper.startPage(request.getPage(), request.getSize());
        List<OrderInfo> list = orderInfoDao.selectAdminOrder(request);
        PageInfo<OrderInfo> pageInfo = new PageInfo<>(list);
        return pageInfo;

    }

    @Override
    public List<OrderInfo> operationStatistics(OperationStatisticsRequest request) {
        return orderInfoDao.selectOrderStatistics(request);
    }

    @Override
    public int countOrderNumber(Map param) {
        return orderInfoDao.countOrderNumber(param);
    }

    @Override
    public int countOrderMoney(Map param) {
        return orderInfoDao.countOrderMoney(param);
    }

    @Override
    public int countCompleteNumByOpenid(String openid) {
        return orderInfoDao.countCompleteNumByOpenid(openid);
    }

    @Override
    public List<OrderStatisticsListDetail> selectOrderStatisticsListDetail(OrderStatisticsListRequest request) {
        return orderInfoDao.selectOrderStatisticsListDetail(request);
    }

    /**
     * 我的用户订单列表
     *
     * @param request
     * @return
     */
    @Override
    public GetMyUserOrderListResponse getMyUserOrderList(GetMyUserOrderListRequest request) {

        GetMyUserOrderListResponse response = new GetMyUserOrderListResponse();

        WxUser wxUser = wxUserDao.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return (GetMyUserOrderListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }


        if(StringUtils.isBlank(request.getOpenid())){
            return (GetMyUserOrderListResponse) response.build(500,"买家不存在");
        }
        Map<String, String> param = new HashMap<>();
        param.put("sellerOpenid",wxUser.getOpenid());
        param.put("buyerOpenid", request.getOpenid());

        PageHelper.startPage(request.getPage(), request.getSize());
        List<GetMyUserOrder> getMyUserOrders = orderInfoDao.selectMyUserOrder(param);
        // 处理有子订单
        List<GetMyUserOrder> subOrders = getMyUserOrders.stream().filter(getMyUserOrder -> StringUtils.isNotBlank(getMyUserOrder.getParentOrderNum()))
                .collect(Collectors.toList());
        // 筛选出没有子订单的订单
        List<GetMyUserOrder> collect = getMyUserOrders.stream().filter(getMyUserOrder -> StringUtils.isBlank(getMyUserOrder.getParentOrderNum()))
                .collect(Collectors.toList());
        for (GetMyUserOrder getMyUserOrder : collect) {
            List<GetMyUserOrderGoods> getMyUserOrderGoods = orderGoodsDao.selectMyUserOrderGoods(getMyUserOrder.getOrderNum());

            Optional<GetMyUserOrder> any = subOrders.stream().filter(myUserOrder -> myUserOrder.getParentOrderNum().equals(getMyUserOrder.getOrderNum())).findAny();
            if (any.isPresent()) {
                GetMyUserOrder getMyUserOrder1 = any.get();
                SubBizOrder subBizOrder = new SubBizOrder();
                BeanUtils.copyProperties(getMyUserOrder1, subBizOrder);
                // 设置匹配上的子订单
                getMyUserOrder.setSubBizOrderList(Arrays.asList(subBizOrder));
            }
            getMyUserOrder.setGoodsList(getMyUserOrderGoods);
        }
        response.setData(getMyUserOrders);

        return response;
    }

    /**
     * 接龙详情页
     *
     * @param request
     * @return
     */
    @Override
    public DragonHistoryResponse DragonHistory(DragonHistoryRequest request) {
        DragonHistoryResponse response = new DragonHistoryResponse();

        if (StringUtils.isBlank(request.getDragonNum())) {
            return (DragonHistoryResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        PageHelper.startPage(request.getPage(), request.getSize());
        List<DragonHistory> dragonHistories = orderInfoDao.selectOrderDetailByDragonNum(request.getDragonNum());

        for (DragonHistory dragonHistory : dragonHistories) {

            List<GetMyUserOrderGoods> getMyUserOrderGoods = orderGoodsDao.selectMyUserOrderGoods(dragonHistory.getOrderNum());

            dragonHistory.setGoodsList(getMyUserOrderGoods);
            // 统计每个接龙用户的复购次数
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("openid", dragonHistory.getOpenid());
            hashMap.put("dragonNum", dragonHistory.getDragonNum());
            List<OrderInfo> orderInfos = orderInfoDao.selectOrderByOpenidAndDragonNum(hashMap);
            // 用户接龙复购次数=用户订单总数-子订单数-1
            Set<OrderInfo> collect = new HashSet();
            if (CollectionUtils.isNotEmpty(orderInfos)) {
                collect = orderInfos.stream().filter(orderInfo -> StringUtils.isBlank(orderInfo.getParentOrderNum()))
                        .collect(Collectors.toSet());
            }
            dragonHistory.setRepurchaseCount(collect.size() - 1);
        }

        response.setData(dragonHistories);

        return response;

    }

    @Override
    public DragonInfo getDragonInfoByCouponUserId(Integer couponUserId) {
        return orderInfoDao.getDragonInfoByCouponUserId(couponUserId);
    }

    @Override
    public List<OrderInfo> selectOrderBySellerIdIndex(String dragonNum) {
        return orderInfoDao.selectOrderBySellerIdIndex(dragonNum);
    }

    @Override
    public BaseResponse checkOrderAmount(Integer couponId, String sessionId, List<GoodsInfo> list) {
        if (StringUtils.isBlank(sessionId) || CollectionUtils.isEmpty(list)) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        WxUser wxUser = wxUserDao.getUserBySessionId(sessionId);
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        Coupon coupon = couponDao.selectByPrimaryKey(couponId);
        if (coupon == null) {
            return new BaseResponse().build("红包不存在!");
        }

        Integer discountAmount = this.calculateDiscountAmount(coupon, list);
        log.debug("折扣金额分{}", discountAmount);
        Map<String, Object> map = new HashMap<>();
        map.put("discountAmount", discountAmount);
        BaseResponse response = new BaseResponse();
        response.setMap(map);
        return response;
    }

    /**
     * 计算优惠金额
     *
     * @param
     * @return
     */
    @Override
    public Integer calculateDiscountAmount(Coupon coupon, List<GoodsInfo> list) {
        String goodsNum = coupon.getGoodsNum();
        List<String> goodsNumList = null;
        if (StringUtils.isNotBlank(goodsNum)) {
            goodsNumList = Arrays.asList(coupon.getGoodsNum().split(","));
        }
        // 计算订单总金额
        Integer total = 0;
        for (GoodsInfo goodsInfo : list) {
            total += goodsInfo.getBuyNumber() * goodsInfo.getPrice();
        }
        // 初始化要返回的优惠金额
        Integer discountAmount = Integer.valueOf(0);
        switch (CouponTypeEnum.matchType(coupon.getCouponType())) {
            case MONEY_OVER_ALL:
                // 通用红包
                discountAmount = coupon.getCouponAmount();
                // 如果商品总金额小于优惠额度,至少付1分钱
                if (total <= discountAmount) {
                    discountAmount = total - 1;
                }
                break;
            case MONEY_ASSIGN_GOODS:
                // 金额类型,指定商品
                Integer totalMoney = 0;
                for (GoodsInfo goods : list) {
                    if (CollectionUtils.isNotEmpty(goodsNumList) && goodsNumList.contains(goods.getGoodsNum())) {
                        totalMoney += goods.getPrice() * goods.getBuyNumber();
                    }
                }
                // 若订单的总金额小于等于折扣金额则至少付1分钱
                if (totalMoney <= coupon.getCouponAmount()) {
                    discountAmount = totalMoney - 1;
                    if (total - totalMoney > 0) {
                        // 订单中有其他未指定商品
                        discountAmount = totalMoney;
                    }
                } else {
                    discountAmount = coupon.getCouponAmount();
                }
                break;
            case DISCOUNT_OVER_ALL:
                // 折扣(全场通用)
                BigDecimal one = BigDecimal.valueOf(1);
                // 把折扣除以100
                BigDecimal divide = BigDecimal.valueOf(coupon.getCouponAmount()).divide(BigDecimal.valueOf(100));
                //折扣力度=1-折扣券
                log.debug("折扣力度{}", one.subtract(divide));
                // 折扣金额=(1-折扣券)*订单总金额(分)
                discountAmount = one.subtract(divide).multiply(BigDecimal.valueOf(total)).intValue();
                break;
            case DISCOUNT_ASSIGN_GOODS:
                //折扣（指定商品） 满足折扣条件的优惠金额
                Integer discountMoney = 0;
                for (GoodsInfo goodsInfo : list) {
                    if (CollectionUtils.isNotEmpty(goodsNumList) && goodsNumList.contains(goodsInfo.getGoodsNum())) {
                        //只要商品列表中包含可以打折的商品都要打折
                        //将要打折的金额累加（单位分）
                        discountMoney += goodsInfo.getPrice() * goodsInfo.getBuyNumber();
                    }
                }
                BigDecimal zK = BigDecimal.valueOf(1);
                // 换算 把折扣除以100
                BigDecimal decimal = BigDecimal.valueOf(coupon.getCouponAmount()).divide(BigDecimal.valueOf(100));
                log.debug("折扣力度{}", zK.subtract(decimal));
                // 折扣金额=(1-折扣券)*订单总金额(分)
                discountAmount = zK.subtract(decimal).multiply(BigDecimal.valueOf(discountMoney)).intValue();
                if (discountMoney <= discountAmount) {
                    //当优惠金额大于商品总金额时(理论上不该有)至少付1分钱
                    discountAmount = discountMoney - 1;
                }
                break;
            default:
                break;
        }
        return discountAmount;
    }

    @Override
    public List<OrderInfo> getOrderByDragonNum(String dragonNum) {
        return orderInfoDao.selectOrder(dragonNum);
    }

    @Override
    public Long countBizOrderByDragonNum(String dragonNum) {
        return orderInfoDao.countBizOrderByDragonNum(dragonNum);
    }

    @Override
    public List<OrderInfo> getOrdersByStatus(Integer status) {
        return orderInfoDao.selectOrderByOrderStatus(status);
    }

    @Override
    public GetMyAllOrderResponse listByorderStatus(GetMyAllOrderRequest request) {
        long start = System.currentTimeMillis();

        final GetMyAllOrderResponse response = new GetMyAllOrderResponse();

        final List<GetMyAllOrder> lists = Lists.newArrayList();

        final WxUser user = wxUserDao.getUserBySessionId(request.getSessionId());
        if (null == user) {
            return (GetMyAllOrderResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        //获取我参与的所有订单
        List<OrderInfo> orderInfoList = new ArrayList<OrderInfo>();
        PageHelper.startPage(request.getPage(), request.getSize());
        if (request.getOrderStatus() != null) {
            orderInfoList = orderInfoDao.selectOrderByOpenidAndStatus(user.getOpenid(), String.valueOf(request.getOrderStatus()));
        } else {
            orderInfoList = orderInfoDao.selectOrderByOpenid(user.getOpenid());
        }
        // 如果是待付款订单需要过滤子订单
        if (null == request.getOrderStatus()
                || OrderStatusEnum.PENDING_PAYMENT.getCode().equals(request.getOrderStatus())
                || OrderStatusEnum.COMPELETED.getCode().equals(request.getOrderStatus())
                || OrderStatusEnum.REFUNDING.getCode().equals(request.getOrderStatus())) {
            orderInfoList = orderInfoList.stream().filter(orderInfo -> StringUtils.isBlank(orderInfo.getParentOrderNum())).collect(Collectors.toList());
        }
        for (OrderInfo orderInfo : orderInfoList) {
            int goodsNumber = 0;
            //获取订单接龙
            DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());
            if (dragonInfo == null) {
                continue;
            }
            //获取卖家信息
            WxUser sellerInfo = wxUserDao.selectByOpenid(dragonInfo.getOpenid());
            //获取订单中商品信息
            final List<OrderGoodsInfo> orderGoodsInfoList = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
            if (null == orderGoodsInfoList) {
                return (GetMyAllOrderResponse) response.build(RespStatus.SYSTEM_ERROR);
            }
            final List<Goods> goodsList = Lists.newArrayList();
            int totalMoney = 0;
            for (OrderGoodsInfo ogInfo : orderGoodsInfoList) {
                if (ogInfo.getBuyNumber() == 0) {
                    continue;
                }
                final Goods goods = new Goods();
                //购买数量
                goods.setTotalNumber(ogInfo.getBuyNumber());
                //商品图片
                goods.setGoodsImgs(ogInfo.getGoodsImg().split(",")[0]);
                //商品名称
                goods.setGoodsName(ogInfo.getGoodsName());
                //商品价格
                goods.setPrice(ogInfo.getPrice());
                goods.setRealPrice(ogInfo.getRealPrice());
                //商品编号
                goods.setGoodsNum(ogInfo.getGoodsNum());
                goodsList.add(goods);
                //商品数量
                goodsNumber += ogInfo.getBuyNumber();
                totalMoney += ogInfo.getBuyNumber() * ogInfo.getPrice();
            }
            final GetMyAllOrder getMyAllOrder = new GetMyAllOrder();
            getMyAllOrder.setOrderNum(orderInfo.getOrderNum());
            getMyAllOrder.setCreateTime(orderInfo.getCreateTime());
            getMyAllOrder.setOrderStatus(orderInfo.getOrderStatus());
            getMyAllOrder.setGoodsList(goodsList);
            getMyAllOrder.setGoodsType(goodsNumber);
            getMyAllOrder.setTotalMoney(totalMoney);
            getMyAllOrder.setRealMoney(orderInfo.getOrderMoney());
            getMyAllOrder.setHeadImg(sellerInfo.getHeadImgUrl());
            getMyAllOrder.setNickName(sellerInfo.getNickName());
            getMyAllOrder.setSellerPhone(dragonInfo.getPhone());
            getMyAllOrder.setDragonNum(dragonInfo.getDragonNum());
            getMyAllOrder.setRemark(orderInfo.getOrderRemark());
            getMyAllOrder.setDragonTitle(dragonInfo.getDragonTitle());
            getMyAllOrder.setAddr(orderInfo.getAddress());
            getMyAllOrder.setPhone(orderInfo.getAddrPhone());
            getMyAllOrder.setUserName(orderInfo.getAddrName());
            getMyAllOrder.setRemark(orderInfo.getOrderRemark());
            getMyAllOrder.setSellerDesc(orderInfo.getSellerDesc());
            //判断是否是补差自订单
            if (StringUtils.isNotBlank(orderInfo.getParentOrderNum())) {
                getMyAllOrder.setIsDifOrder(1);
            }
            getMyAllOrder.setParentOrderNum(orderInfo.getParentOrderNum());
            getMyAllOrder.setDragonAddr(orderInfo.getDragonAddr());
            if (orderInfo.getCouponUserId() != null) {
                getMyAllOrder.setIsUsed(1);
                getMyAllOrder.setCouponMoney(orderInfo.getCouponAmount());
            } else {
                getMyAllOrder.setIsUsed(0);
            }
            getMyAllOrder.setDeliveryType(orderInfo.getIsDelivery());
            // 把子订单放到父订单下
            OrderInfo subOrder = orderInfoDao.selectOrderByParentOrderNum(orderInfo.getOrderNum());
            if (subOrder != null) {
                SubBizOrder subBizOrder = new SubBizOrder();
                subBizOrder.setCreateTime(subOrder.getCreateTime());
                subBizOrder.setOrderMoney(subOrder.getOrderMoney());
                subBizOrder.setOrderNum(subOrder.getOrderNum());
                subBizOrder.setOrderStatus(subOrder.getOrderStatus());
                subBizOrder.setRemark(subOrder.getOrderRemark());
                subBizOrder.setOrderGoodsInfoList(orderGoodsDao.select(subOrder.getOrderNum()));
                if (subOrder.getOrderType() != null) {
                    subBizOrder.setOrderType(subOrder.getOrderType());
                } else {
                    Integer orderType = this.getOrderType(subOrder.getOrderNum());
                    subBizOrder.setOrderType(orderType);
                }
                getMyAllOrder.setSubBizOrder(subBizOrder);
            }
            lists.add(getMyAllOrder);
        }
        log.info("/order/getOrdersByOrderStatus接口耗时：{}",System.currentTimeMillis()-start);
        response.setData(lists);
        return response;
    }


    @Override
    public OrderDetailResponse detailByOrderNum(OrderDetailRequest request) {
        final OrderDetailResponse response = new OrderDetailResponse();
        if (request.getState() == 1) {
            WxUser user = wxUserDao.getUserBySessionId(request.getSessionId());
            if (null == user) {
                return (OrderDetailResponse) response.build(RespStatus.USER_NOT_EXIST);
            }
        }
        if(Integer.valueOf(1).equals(request.getUrlType()) && null != request.getMsgId()){
            //如果是从消息中心进入 则把消息改为已读
            msgRecordService.updateReadStatus(request.getMsgId());
        }
        if (request.getOrderNum() == null) {
            return (OrderDetailResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        //获取我参与的所有订单
        OrderInfo orderInfo = orderInfoDao.selectOrderByOrderNum(request.getOrderNum());
        //获取订单接龙
        DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());
        if (dragonInfo == null) {
            return (OrderDetailResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        response.setDragonTitle(dragonInfo.getDragonTitle());
        response.setSellerRemark(orderInfo.getSellerDesc());
        // 设置物流信息
        Logistics logisticsInfo = logisticsService.getLogisticsInfoByOrderNum(orderInfo.getOrderNum());
        response.setLogistics(logisticsInfo);
        //获取卖家信息
        WxUser sellerInfo = wxUserDao.selectByOpenid(dragonInfo.getOpenid());
        Shop shopByKeeperOpenid = shopMapper.getShopByKeeperOpenid(sellerInfo.getOpenid());
        response.setSellerOpenId(sellerInfo.getOpenid());
        if (shopByKeeperOpenid != null) {
            response.setShopId(shopByKeeperOpenid.getShopId());
        }

        //获取订单中商品信息
        final List<OrderGoodsInfo> orderGoodsInfoList = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
        if (null == orderGoodsInfoList) {
            log.debug("未找到该订单中的商品信息!订单编号{}", orderInfo.getOrderNum());
            response.setMsg("未找到该订单中的商品信息!订单编号{}" + orderInfo.getOrderNum());
        }
        final List<Goods> goodsList = Lists.newArrayList();
        int totalMoney = 0;
        for (OrderGoodsInfo ogInfo : orderGoodsInfoList) {
            if (ogInfo.getBuyNumber() == 0) {
                continue;
            }
            final Goods goods = new Goods();
            GoodsInfo goods1 = goodsInfoService.getGoods(ogInfo.getGoodsNum());
            // 库存
            goods.setTotalNumber(goods1.getTotalNumber());
            //购买数量
            goods.setBuyNum(ogInfo.getBuyNumber());
            //商品图片
            goods.setGoodsImgs(ogInfo.getGoodsImg().split(",")[0]);
            //商品名称
            goods.setGoodsName(ogInfo.getGoodsName());
            //商品价格
            goods.setPrice(ogInfo.getPrice());
            goods.setRealPrice(ogInfo.getRealPrice());
            //商品编号
            goods.setGoodsNum(ogInfo.getGoodsNum());
            // 若当前接龙没有结束
            goods.setDragonNum(dragonInfo.getDragonNum());
            if (Integer.valueOf(DragonStatusEnum.ALREADY_OVER.getStatus()).equals(dragonInfo.getDragonStatus())) {
                // 当前接龙已结束时,查找卖家所有包含该商品的接龙,并按最新开始时间降序排序取第一个
                DragonGoods dragonGoods = dragonGoodsDao.findByGoodsNum(ogInfo.getGoodsNum());
                if (null != dragonGoods) {
                    goods.setDragonNum(dragonGoods.getDragonNum());
                }
            }
            goodsList.add(goods);
            totalMoney += ogInfo.getBuyNumber() * ogInfo.getPrice();
        }
        // TODO 缓存改造
        Long repurchase = statisticsService.getShopRepurchaseNum(sellerInfo.getOpenid());

        response.setOrderNum(orderInfo.getOrderNum());
        response.setCreateTime(orderInfo.getCreateTime());
        response.setOrderStatus(orderInfo.getOrderStatus());
        response.setGoodsList(goodsList);
        response.setTotalMoney(totalMoney);

        // 订单实付价格计算
        Integer realMoney = orderInfo.getOrderMoney();
        OrderInfo subOrderInfo = orderInfoDao.selectOrderByParentOrderNum(request.getOrderNum());
        if (subOrderInfo != null) {
            if (subOrderInfo.getOrderType() == null) {
                // 退差类型
                if (OrderStatusEnum.REFUNDED.getCode().equals(subOrderInfo.getOrderStatus()) && subOrderInfo.getUpdateTime() == null) {
                    realMoney = orderInfo.getOrderMoney() - subOrderInfo.getOrderMoney();
                } else if (OrderStatusEnum.COMPELETED.getCode().equals(subOrderInfo.getOrderStatus())) {
                    // 补差类型
                    realMoney = orderInfo.getOrderMoney() + subOrderInfo.getOrderMoney();
                } else {
                    realMoney = orderInfo.getOrderMoney();
                }
            } else {
                if (OrderTypeEnum.REFUND_ORDER.getCode().equals(subOrderInfo.getOrderType())) {
                    // 退差类型
                    realMoney = orderInfo.getOrderMoney() - subOrderInfo.getOrderMoney();
                } else if (OrderStatusEnum.COMPELETED.getCode().equals(subOrderInfo.getOrderStatus())) {
                    // 补差类型
                    realMoney = orderInfo.getOrderMoney() + subOrderInfo.getOrderMoney();
                } else {
                    realMoney = orderInfo.getOrderMoney();
                }
            }
            //卖家备注
            response.setSellerRemark(subOrderInfo.getOrderRemark());
        }

        response.setRealMoney(realMoney);
        response.setHeadImg(sellerInfo.getHeadImgUrl());
        response.setNickName(sellerInfo.getNickName());
        response.setRepurchase(repurchase.intValue());
        response.setSellerPhone(dragonInfo.getPhone());
        response.setDragonNum(dragonInfo.getDragonNum());
        response.setAddr(orderInfo.getAddress());
        response.setPhone(orderInfo.getAddrPhone());
        response.setUserName(orderInfo.getAddrName());
        response.setRemark(orderInfo.getOrderRemark());
        response.setParentOrderNum(orderInfo.getParentOrderNum());
        response.setDragonAddr(orderInfo.getDragonAddr());
        response.setSendTime(DateUtils.convertString2Date2(dragonInfo.getSendTime()));

        if (orderInfo.getParentOrderNum() != null) {
            response.setIsDifOrder(1);
        }
        response.setIsPayLater(orderInfo.getIsPayLater());
        //判断该笔交易是否使用优惠卷
        if (orderInfo.getCouponUserId() != null) {
            CouponUser couponUser = couponUserDao.selectByPrimaryKey(orderInfo.getCouponUserId());
            Coupon coupon = couponDao.selectByPrimaryKey(couponUser.getCouponId());
            if(CouponTypeEnum.MONEY_OVER_ALL.getType().equals(coupon.getCouponType()) || CouponTypeEnum.MONEY_ASSIGN_GOODS.getType().equals(coupon.getCouponType())){
                response.setCouponMoney(orderInfo.getCouponAmount());
            }else {
                response.setCouponMoney(totalMoney-realMoney);
            }
            response.setIsUsed(1);
        } else {
            response.setIsUsed(0);
        }
        OrderInfo subOrder = orderInfoDao.selectOrderByParentOrderNum(orderInfo.getOrderNum());
        if (null != subOrder) {
            response.setDifOrderNum(subOrder.getOrderNum());
            response.setRefundMoney(subOrder.getOrderMoney());
            SubBizOrder subBizOrder = new SubBizOrder();
            subBizOrder.setCreateTime(subOrder.getCreateTime());
            List<OrderGoodsInfo> orderGoodsInfos = orderGoodsDao.select(subOrder.getOrderNum());
            subBizOrder.setOrderGoodsInfoList(orderGoodsInfos);
            subBizOrder.setOrderNum(subOrder.getOrderNum());
            subBizOrder.setOrderStatus(subOrder.getOrderStatus());
            if (subOrder.getOrderType() != null) {
                subBizOrder.setOrderType(subOrder.getOrderType());
            } else {
                Integer orderType = this.getOrderType(subOrder.getOrderNum());
                subBizOrder.setOrderType(orderType);
            }
            response.setSubBizOrder(subBizOrder);
        }
        response.setDeliveryType(orderInfo.getIsDelivery());
        return response;
    }

    @Override
    public Integer getOrderType(String orderNum) {
        OrderInfo orderInfo = orderInfoDao.selectOrderByOrderNum(orderNum);
        Integer orderType = 0;
        if (StringUtils.isNotBlank(orderInfo.getParentOrderNum())) {
            if (OrderStatusEnum.REFUNDED.getCode().equals(orderInfo.getOrderStatus()) && orderInfo.getUpdateTime() == null) {
                orderType = OrderTypeEnum.REFUND_ORDER.getCode();
            } else {
                orderType = OrderTypeEnum.DIF_ORDER.getCode();
            }
        }
        return orderType;
    }

}