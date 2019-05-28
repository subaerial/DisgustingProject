package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.ConvertArgs;
import com.mhc.yunxian.dao.model.DragonInfo;
import com.mhc.yunxian.dao.model.OrderInfo;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.enums.MsgTypeEnum;
import com.mhc.yunxian.enums.RoleEnum;
import com.mhc.yunxian.utils.RabbitMqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service
 * @author: 昊天
 * @date: 2019/1/28 10:37 AM
 * @since V1.1.0-SNAPSHOT
 */
@Slf4j
@Component
public class SendStationMsgService {

    @Autowired
    private MsgRecordService msgRecordService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private DragonService dragonService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    private static final String BUYER_ORDER_SUCCESS = "buyer.order";
    private static final String BUYER_SHIPPING_REMINDER = "buyer.shipping";
    private static final String BUYER_REFUND_TO_ACCUNT = "buyer.refunded";
    private static final String BUYER_REFUND_REJECTED = "buyer.refundRefused";
    private static final String BUYER_DIF = "buyer.dif";
    private static final String BUYER_RECEIPT_REMINDER = "buyer.receipt";
    private static final String SELLER_ORDER_NOTICE = "seller.order";
    private static final String SELLER_REFUND_APPLY = "seller.refund";
    private static final String SELLER_WITHDRAWAL_TO_ACCUNT = "seller.withdrawal";
    private static final String SELLER_DRAGON_NOTICE = "seller.dragon";

    /**
     * <p> 买家下单成功 站内通知买家 </p>
     * @param openId
     * @param dragonName
     * @param orderNum
     * @return void
     * @author 昊天
     * @date 2019/1/28 11:13 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendOrderSuccessNoticeToBuyer(String openId,String dragonName,String orderNum){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map = msgRecordService.addMsgRecord(openId, MsgTypeEnum.BUYER_ORDER_SUCCESS.getCode(), RoleEnum.BUYER.getCode(), "/pages/newOrderDetail/main?orderNum=" + orderNum, convertArgs);
            if(map.isEmpty()){
                log.error("买家---->下单成功消息 没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.BUYER,BUYER_ORDER_SUCCESS,map);
        } catch (Exception e) {
            log.error("买家---->下单成功消息 发送失败");
            e.printStackTrace();
        }
    }

    /**
     * <p> 发货发送站内消息 </p>
     * @param openId
     * @param title
     * @param orderNum
     * @param addr
     * @return void
     * @author 昊天
     * @date 2019/1/26 10:36 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendShippingRemindNotice(String openId,String title,String addr,String orderNum){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setDragonName(title);
            convertArgs.setAddr(addr);
            Map<String, Object> map = msgRecordService.addMsgRecord(openId, MsgTypeEnum.BUYER_SHIPPING_REMINDER.getCode(), RoleEnum.BUYER.getCode(), "/pages/newOrderDetail/main?orderNum=" + orderNum, convertArgs);
            if(map.isEmpty()){
                log.error("买家---->发货通知消息 没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.BUYER,BUYER_SHIPPING_REMINDER,map);
        } catch (Exception e) {
            log.error("买家---->发货通知消息 发送失败");
            e.printStackTrace();
        }
    }

    /**
     * <p> 退款成功 发送站内消息 </p>
     * @param openId
     * @param dragonName
     * @param price
     * @return void
     * @author 昊天
     * @date 2019/1/26 11:31 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendRefundSuccessNotice(String openId,String dragonName,Double price){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setPrice(price);
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map = msgRecordService.addMsgRecord(openId, MsgTypeEnum.BUYER_REFUND_TO_ACCUNT.getCode(), RoleEnum.BUYER.getCode(), "", convertArgs);
            if(map.isEmpty()){
                log.error("买家---->退款到账消息 没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.BUYER,BUYER_REFUND_TO_ACCUNT,map);
        } catch (Exception e) {
            log.error("买家---->退款到账消息 发送失败");
            e.printStackTrace();
        }
    }

    /**
     * <p> 退款被拒 发送站内消息 </p>
     * @param openId
     * @param dragonName
     * @param price
     * @return void
     * @author 昊天
     * @date 2019/1/28 10:47 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendRefundRefusedNotice(String openId,String dragonName,Double price){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setPrice(price);
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map = msgRecordService.addMsgRecord(openId, MsgTypeEnum.BUYER_REFUND_REJECTED.getCode(), RoleEnum.BUYER.getCode(), "", convertArgs);
            if(map.isEmpty()){
                log.error("买家---->退款被拒消息  没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.BUYER,BUYER_REFUND_REJECTED,map);
        } catch (Exception e) {
            log.error("买家---->退款被拒消息 发送失败");
            e.printStackTrace();
        }
    }


    /**
     * <p> 退补差价通知 站内消息 </p>
     * @param openId
     * @param msgType
     * @param dragonName
     * @param price
     * @param orderNum
     * @return void
     * @author 昊天
     * @date 2019/1/28 10:56 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendDifNotice(String openId,Integer msgType,String dragonName,Double price,String orderNum){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setPrice(price);
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map =msgRecordService.addMsgRecord(openId, msgType, RoleEnum.BUYER.getCode(), "/pages/newOrderDetail/main?orderNum=" + orderNum, convertArgs);
            if(map.isEmpty()){
                log.error("买家---->退补差价通知 没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.BUYER,BUYER_DIF,map);
        } catch (Exception e) {
            log.error("买家---->退补差价通知 发送失败");
            e.printStackTrace();
        }
    }

    /**
     * <p> 买家收货提醒 站内通知 </p>
     * @param openId
     * @param dragonName
     * @param sendTime
     * @param orderNum
     * @return void
     * @author 昊天
     * @date 2019/1/28 11:36 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendReceiptNotice(String openId,String dragonName,Integer sendTime,String orderNum){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setSendTime(sendTime);
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map =msgRecordService.addMsgRecord(openId, MsgTypeEnum.BUYER_RECEIPT_REMINDER.getCode(), RoleEnum.BUYER.getCode(), "/pages/newOrderDetail/main?orderNum=" + orderNum, convertArgs);
            if(map.isEmpty()){
                log.error("买家---->收获提醒通知  没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.BUYER,BUYER_RECEIPT_REMINDER,map);
        } catch (Exception e) {
            log.error("买家---->收获提醒通知 发送失败");
            e.printStackTrace();
        }
    }


    /**
     * <p> 买家下单成功 发站内消息给卖家 </p>
     * @param openId
     * @param dragonName
     * @param dragonNum
     * @return void
     * @author 昊天
     * @date 2019/1/28 11:18 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendOrderSuccessNoticeToSeller(String openId,String dragonName,String dragonNum){
        try {
            WxUser wxUser = wxUserService.getWxUser(openId);
            if(Objects.isNull(wxUser)){
                //如果查不到则退出方法
                return;
            }
            DragonInfo dragon = dragonService.getDragon(dragonNum);
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setDragonName(dragonName);
            convertArgs.setUserName(wxUser.getNickName());
            Map<String, Object> map = msgRecordService.addMsgRecord(dragon.getOpenid(), MsgTypeEnum.SELLER_ORDER_NOTICE.getCode(), RoleEnum.SELLER.getCode(), "/pages/newCustomerOrderList/main?pageSize=20&pageNum=1&who=seller&dragonNum="+dragonNum, convertArgs);
            if(map.isEmpty()){
                log.error("卖家---->下单成功消息  没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.SELLER,SELLER_ORDER_NOTICE,map);
        } catch (Exception e) {
            log.error("卖家---->下单成功消息 发送失败");
            e.printStackTrace();
        }
    }

    /**
     * <p> 申请退款发送给卖家站内消息 </p>
     * @param openId
     * @param dragonName
     * @param orderNum
     * @return void
     * @author 昊天
     * @date 2019/1/28 9:58 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void snedRefundToSeller(String openId,String dragonName,String orderNum){
        try {
            WxUser wxUser = wxUserService.getWxUser(openId);
            if(Objects.isNull(wxUser)){
                //如果查不到则退出方法
                return;
            }
            OrderInfo order = orderService.getOrderByOrderNum(orderNum);
            DragonInfo dragon = dragonService.getDragon(order.getDragonNum());
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setUserName(wxUser.getNickName());
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map =msgRecordService.addMsgRecord(dragon.getOpenid(),MsgTypeEnum.SELLER_REFUND_APPLY.getCode(),RoleEnum.SELLER.getCode(),"/pages/newOrderDetail/main?orderNum=" + orderNum, convertArgs);
            if(map.isEmpty()){
                log.error("卖家---->退款申请  没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.SELLER,SELLER_REFUND_APPLY,map);
        } catch (Exception e) {
            log.error("卖家---->退款申请 发送失败");
            e.printStackTrace();
        }
    }


    /**
     * <p> 卖家提现到帐 站内通知 </p>
     * @param openId
     * @param price
     * @return void
     * @author 昊天
     * @date 2019/1/28 11:25 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendWithdrawalToAccunt(String openId,Double price){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setPrice(price);
            Map<String, Object> map = msgRecordService.addMsgRecord(openId, MsgTypeEnum.SELLER_WITHDRAWAL_TO_ACCUNT.getCode(), RoleEnum.SELLER.getCode(),"", convertArgs);
            if(map.isEmpty()){
                log.error("卖家---->提现到账  没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.SELLER,SELLER_WITHDRAWAL_TO_ACCUNT,map);
        } catch (Exception e) {
            log.error("卖家---->提现到账 发送失败");
            e.printStackTrace();
        }
    }

    /**
     * <p> 卖家接龙截单提醒 站内通知 </p>
     * @param openId
     * @param dragonName
     * @param dragonNum
     * @return void
     * @author 昊天
     * @date 2019/1/28 11:38 AM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendDragonEndNotice(String openId,String dragonName,String dragonNum){
        try {
            ConvertArgs convertArgs = new ConvertArgs();
            convertArgs.setDragonName(dragonName);
            Map<String, Object> map = msgRecordService.addMsgRecord(openId, MsgTypeEnum.SELLER_DRAGON_NOTICE.getCode(), RoleEnum.SELLER.getCode(),"/pages/newCustomerOrderList/main?pageSize=20&pageNo=1&who=seller&dragonNum ="+dragonNum , convertArgs);
            if(map.isEmpty()){
                log.error("卖家---->接龙截单提醒通知  没有添加到数据库");
            }
            rabbitMqUtil.sendMsgToQueue(RoleEnum.SELLER,SELLER_DRAGON_NOTICE,map);
        } catch (Exception e) {
            log.error("卖家---->接龙截单提醒通知 发送失败");
            e.printStackTrace();
        }
    }
}
