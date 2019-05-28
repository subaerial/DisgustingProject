package com.mhc.yunxian.utils;

import com.google.gson.Gson;
import com.mhc.yunxian.cache.JedisProducer;
import com.mhc.yunxian.cache.LockPool;
import com.mhc.yunxian.configuration.WebSocketServer;
import com.mhc.yunxian.enums.RoleEnum;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.utils
 * @author: 昊天
 * @date: 2019/2/14 3:35 PM
 * @since V1.1.0-SNAPSHOT
 */
@Component
@Slf4j
public class RabbitMqUtil implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback{

    @Value("${yunxian.exchange.buyer}")
    private String buyerExchange;

    @Value("${yunxian.exchange.seller}")
    private String sellerExchange;

    private RabbitTemplate rabbitTemplate;

    private static final Gson gson = new Gson();

    @Autowired
    public RabbitMqUtil(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this);
    }

    /**
     * <p> 根据身份和路由键发送消息 </p>
     * @param roleEnum   身份枚举类
     * @param routingKey 路由键
     * @param map        消息
     * @return void
     * @author 昊天
     * @date 2019/2/14 3:45 PM
     * @since V1.1.0-SNAPSHOT
     *
     */
    public void sendMsgToQueue(RoleEnum roleEnum, String routingKey, Map<String,Object> map){
        String exchange;
        if(RoleEnum.SELLER.equals(roleEnum)){
            exchange = sellerExchange;
        }else {
            exchange = buyerExchange;
        }
        CorrelationData correlationData = new CorrelationData(String.valueOf(System.currentTimeMillis()));
        rabbitTemplate.convertAndSend(exchange,routingKey,map,correlationData);
    }

    /**
     * 确认消息是否到达exchange中
     * @param correlationData
     * @param b
     * @param s
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if(b){
            log.info("消息推送成功，编号:{}",correlationData.getId());
        }else {
            log.warn("消息发送失败，id：{},错误信息：{}",correlationData.getId(),s);
        }
    }

    /**
     * 消息没有到达队列会回调此方法
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.warn("消息发送失败---message:{},code:{},text:{},exchange:{},routingKey:{}",message.getBody(),replyCode,replyText,exchange,routingKey);
    }

    @RabbitListener(queues = {"buyer.order","buyer.shipping","buyer.refunded","buyer.refundRefused","buyer.dif","buyer.receipt"})
    public void listenBuyerMsg(Message message,Channel channel) throws IOException {
        //把消息转成map
        try {
            String msg = new String(message.getBody(), "UTF-8");
            Map map = gson.fromJson(msg, Map.class);
            String openId = String.valueOf(map.get("openId"));
            for (WebSocketServer webSocketServer : WebSocketServer.webSocketSet) {
                //如果监听到消息时该用户在线，则实时推送
                if (openId.equals(webSocketServer.getOpenId()) && RoleEnum.BUYER.getCode().equals(webSocketServer.getRole())) {
                    webSocketServer.sendMessage(msg);
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            //出现异常 拒收消息
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = {"seller.order","seller.refund","seller.withdrawal","seller.dragon"})
    public void listenSellerMsg(Message message,Channel channel) throws IOException {
        try {
            String msg = new String(message.getBody(),"UTF-8");
            Map map = gson.fromJson(msg, Map.class);
            String openId = String.valueOf(map.get("openId"));
            for (WebSocketServer webSocketServer : WebSocketServer.webSocketSet){
                //如果监听到消息时该用户在线，则实时推送
                if(openId.equals(webSocketServer.getOpenId()) && RoleEnum.SELLER.getCode().equals(webSocketServer.getRole())) {
                    webSocketServer.sendMessage(msg);
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            //出现异常 拒收消息
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            e.printStackTrace();
        }
    }
}
