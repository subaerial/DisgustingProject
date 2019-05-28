package com.mhc.yunxian.utils;

import com.mhc.yunxian.bean.admin.FindWxUserRequest;
import com.mhc.yunxian.dao.MoneyRecordDao;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.WxUserDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.MoneyRecordEnum;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class template {

    @Autowired
    private WxUserDao wxUserService;


    @Autowired
    private DrawMoneyService drawMoneyService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private MoneyRecordService moneyRecordService;

    @Autowired
    private OrderInfoDao orderInfoDao;

    @Autowired
    private DragonService dragonService;

    @Autowired
    private MoneyRecordDao moneyRecordDao;

    public void method(){

        FindWxUserRequest findWxUserRequest = new FindWxUserRequest();
        findWxUserRequest.setState(3);

        List<WxUser> wxUsers = wxUserService.selectAllUser(findWxUserRequest); //所有卖家

        for(WxUser wxUser : wxUsers){

            System.out.println("开始对卖家执行操作："+wxUser.getNickName());

            List<DrawMoney> drawMonies = drawMoneyService.getDrawMoneyDetail(wxUser.getOpenid());//当前卖家下的所有提现，按照时间倒序

            List<OrderInfo> orderInfos = orderService.getOrderBySellerId(wxUser.getOpenid());//当前卖家下所有订单

            Balance balance = balanceService.getBalance(wxUser.getOpenid());//用户余额

            System.out.println("开始根据付款未收货订单增加用户余额："+wxUser.getNickName());

            for(OrderInfo orderInfo : orderInfos){
                if(orderInfo.getOrderStatus() == OrderStatusEnum.PENDING_DELIVERY.getCode()
                        || orderInfo.getOrderStatus() == OrderStatusEnum.PENDING_RECEIVED.getCode()){
                    System.out.println("根据付款未收货订单增加用户余额："+orderInfo.getOrderNum());
                    System.out.println("余额为："+balance.getBalance() +" + " + orderInfo.getOrderMoney()
                    + " = " +(balance.getBalance()+orderInfo.getOrderMoney()));
                    balance.setBalance(balance.getBalance()+orderInfo.getOrderMoney());
                }
            } //增加用户余额

            balanceService.updateBalanceByOpenid(balance);

            System.out.println("余额增加结束，此时余额为："+balance.getBalance());

            int balanceTemp = balance.getBalance();

            System.out.println("开始根据订单生成交易记录表："+wxUser.getNickName());


            for(int i = 0;i < orderInfos.size();i++){//按时间倒序

                if(orderInfos.get(i).getOrderStatus() == OrderStatusEnum.PENDING_PAYMENT.getCode() ||
                        orderInfos.get(i).getOrderStatus() == OrderStatusEnum.CANCELLED.getCode() ||
                        orderInfos.get(i).getOrderStatus() == OrderStatusEnum.REFUNDING.getCode()){
                    continue;
                }//订单状态为 待付款，退款中，已取消 不生成交易明细表

                if(drawMonies.size() >0 && drawMonies.get(0).getCreateTime().getTime() > orderInfos.get(i).getCreateTime().getTime() ){
                    //处理提现
                    MoneyRecord moneyRecordDraw = new MoneyRecord();

                    moneyRecordDraw.setCreateTime(drawMonies.get(0).getCreateTime());

                    moneyRecordDraw.setMoney(drawMonies.get(0).getDrawMoney());

                    moneyRecordDraw.setOpenid(drawMonies.get(0).getOpenid());

                    moneyRecordDraw.setDrawMoneyId(String.valueOf(drawMonies.get(0).getId()));

                    moneyRecordDraw.setBalance(String.valueOf(balanceTemp));

                    balanceTemp += drawMonies.get(0).getDrawMoney();

                    if(drawMonies.get(0).getStatus() == 1){
                        moneyRecordDraw.setRecordType(MoneyRecordEnum.IN_THE_PRESENT.getCode());
                        moneyRecordDraw.setCause("提现中");
                    }else{
                        moneyRecordDraw.setRecordType(MoneyRecordEnum.PRESENT_COMPLETION.getCode());
                        moneyRecordDraw.setCause("提现完成");
                    }

                    System.out.println("生成提现明细表:drawMoneyId="+drawMonies.get(0).getId());

                    moneyRecordService.add(moneyRecordDraw);

                    drawMonies.remove(0); //移除处理过的提现记录

                }


                MoneyRecord moneyRecord = new MoneyRecord();

                moneyRecord.setCreateTime(orderInfos.get(i).getCreateTime());

                moneyRecord.setOrderNum(orderInfos.get(i).getOrderNum());

                moneyRecord.setMoney(orderInfos.get(i).getOrderMoney());

                moneyRecord.setOpenid(wxUser.getOpenid());


                if(orderInfos.get(i).getOrderStatus() == OrderStatusEnum.PENDING_DELIVERY.getCode() ||
                        orderInfos.get(i).getOrderStatus() == OrderStatusEnum.PENDING_RECEIVED.getCode() ||
                        orderInfos.get(i).getOrderStatus() == OrderStatusEnum.COMPELETED.getCode() ){
                    moneyRecord.setCause("交易收入-"+orderInfos.get(i).getOrderNum());
                    moneyRecord.setRecordType(MoneyRecordEnum.IMCOME.getCode());
                    moneyRecord.setBalance(String.valueOf(balanceTemp));
                    balanceTemp -= orderInfos.get(i).getOrderMoney();
                }

                if(orderInfos.get(i).getOrderStatus() == OrderStatusEnum.REFUNDED.getCode()){
                    moneyRecord.setCause("退款-"+orderInfos.get(i).getOrderNum());
                    moneyRecord.setRecordType(MoneyRecordEnum.REFUNED.getCode());
                    moneyRecord.setBalance(String.valueOf(balanceTemp));
                    balanceTemp += orderInfos.get(i).getOrderMoney();
                }

                moneyRecordService.add(moneyRecord);

                System.out.println("生成交易明细表成功");


            }

            System.out.println("该用户处理完成："+ wxUser.getNickName());

        }
    }


}
