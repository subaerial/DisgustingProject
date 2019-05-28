package com.mhc.yunxian.service;


import com.alibaba.fastjson.JSONObject;
import com.mhc.yunxian.constants.Constants;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.SystemParamDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.MoneyRecordEnum;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.utils.CommonUtils;
import com.mhc.yunxian.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ScheduledService {

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
    GoodsInfoService goodsInfoService;

    @Autowired
    MoneyRecordService moneyRecordService;

    @Autowired
    BalanceService balanceService;

    @Autowired
    SystemParamDao systemParamDao;

    @Autowired
    WxUserService wxUserService;

    @Autowired
    WxService wxService;

    @Autowired
    SystemParamService systemParamService;

//    @Scheduled(cron = "0 */1 * * * ?")//每5分钟执行一次
//    public void scheduledOrder() {
//
//        log.info("进入商品数量检查定时任务");
//        List<DragonInfoVO> dragonInfoList = dragonService.getAllDragonInfo();
//        int flag = 0;
//        for (DragonInfoVO dragonInfo:dragonInfoList) {
//            if (dragonInfo.getDragonStatus() == 1){
//                continue;
//            }
//            List<DragonGoods> dragonGoods = dragonGoodsService.getGoodsNUM(dragonInfo.getDragonNum());
//            for (DragonGoods dg:dragonGoods) {
//                GoodsInfo goodsInfo = goodsInfoService.getGoods(dg.getGoodsNum());
//                if (goodsInfo.getTotalNumber() == 0){
//                    flag +=1;
//                }
//            }
//
//            //所以商品卖完，结束接龙
//            if (flag == dragonGoods.size()){
//                DragonInfoVO info = new DragonInfoVO();
//                info.setDragonNum(dragonInfo.getDragonNum());
//                info.setDragonStatus(1);
//                if (!dragonService.updateDragonStatus(info)){
//                    log.error("接龙状态修改失败");
//                }
//            }
//
//        }



//    }


    //定时扫描数据库,订单发货24H后自动收获
    @Transactional(rollbackFor = {Exception.class,IOException.class,NullPointerException.class,DataException.class})
    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行一次
    public void scheduledOrder() {
        log.info("扫描已发货订单定时器运行中");

        SystemParam systemParam = systemParamService.selectOneByParamGroup("LOCK_KEY_ORDER");

        if(null == systemParam){
            throw new DataException("订单锁读取失败");
        }

        if("1".equals(systemParam.getParamValue())){
            log.info("已加锁。");
            return ;
        }

        systemParam.setParamValue("1");//加锁

        if(systemParamService.updateSystemParam(systemParam) < 1){
            log.error("订单锁加锁失败");
            return ;
        }

        try{
            List<OrderInfo> orderInfos = orderInfoDao.selectOrderByOrderStatus(2);

            for(OrderInfo orderInfo : orderInfos){
                long time = System.currentTimeMillis();//获取当前时间毫秒数
                long leftTime = time - 24*60*60*1000;//时间左偏移
                if (orderInfo.getSendTime().getTime() < leftTime){
                    orderInfo.setOrderStatus(OrderStatusEnum.COMPELETED.getCode());

                    orderInfo.setComfirmTime(new Date());

                    if(orderService.updateOrder(orderInfo)){

                        DragonInfo dragonInfo = dragonService.getDragon(orderInfo.getDragonNum());

                        if(null == dragonInfo){
                            throw new DataException("接龙表获取错误，回滚");
                        }

                        log.info("更新发货1天后订单成功，订单号："+orderInfo.getOrderNum());
                    }else{
                        log.info("更新发货1天后订单失败，订单号："+orderInfo.getOrderNum());
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.toString());
        }finally {
            systemParam.setParamValue("0");//解锁

            if(systemParamService.updateSystemParam(systemParam) < 1){
                log.error("订单锁解锁失败");
            }
            log.info("更新发货1天后订单结束");

        }
    }

    //定时更新微信Access_token
    @Transactional(rollbackFor = {Exception.class,IOException.class,NullPointerException.class,DataException.class})
//    @Scheduled(cron = "0 */1 * * * ?")//每分钟执行一次
    public void scheduledAccessToken() {

        log.info("更新access_token开始");


        WxAccessToken wxAccessToken = wxService.getWxAccessToken();

        if(wxAccessToken == null){
            wxAccessToken = new WxAccessToken();

            JSONObject jsonObject = CommonUtils.getNewAccessToken();

            if(jsonObject.getString("errcode") == null){

                wxAccessToken.setAccessToken(jsonObject.getString("access_token"));

                long deadline = System.currentTimeMillis() + 1000 * Integer.valueOf(jsonObject.getString("expires_in"));

                wxAccessToken.setDeadline(new Date(deadline));

                if(wxService.insertWxAccessToken(wxAccessToken) <  1){
                    log.error("ACCESS_TOKEN插入失败");
                }else{
                    log.info("ACCESS_TOEN插入成功");
                }

            }else{
                log.info(jsonObject.getString("errmsg"));
            }

        }else{

            long nowDate = System.currentTimeMillis();

            if( (nowDate + 5 *60 * 1000) >= wxAccessToken.getDeadline().getTime()){
                JSONObject jsonObject = CommonUtils.getNewAccessToken();

                if(jsonObject.getString("errcode") == null){

                    wxAccessToken.setAccessToken(jsonObject.getString("access_token"));

                    long deadline = System.currentTimeMillis() + 1000 * Integer.valueOf(jsonObject.getString("expires_in"));

                    wxAccessToken.setDeadline(new Date(deadline));

                    if(wxService.updateWxAccessToken(wxAccessToken) <  1){
                        log.error("ACCESS_TOKEN更新失败");
                    }else{
                        log.info("ACCESS_TOEN更新成功");
                    }

                }else{
                    log.info(jsonObject.getString("errmsg"));
                }
            }

        }



    }

}
