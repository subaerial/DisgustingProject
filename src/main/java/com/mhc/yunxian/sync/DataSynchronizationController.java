package com.mhc.yunxian.sync;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.dao.DragonAddrDao;
import com.mhc.yunxian.dao.GoodsInfoDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.WxUserDao;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.service.MyUserService;
import com.mhc.yunxian.service.StatisticsService;
import com.mhc.yunxian.utils.JsonUtils;
import io.swagger.annotations.Api;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Created by Administrator on 2018/12/13.
 *
 * @author Alin
 */
@Api
@Slf4j
@Controller
@RequestMapping
public class DataSynchronizationController {

    @Autowired
    private WxUserDao wxUserDao;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private DragonAddrDao dragonAddrDao;
    @Autowired
    private GoodsInfoDao goodsInfoDao;
    @Autowired
    private MyUserService myUserService;
    @Autowired
    private RemoteCache redisCache;

    /**
     * 为有用户身份为卖家且具备发布接龙功能的用户同步生成Shop店铺信息
     */
    @RequestMapping(value = "/syncShopInfo", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    public BaseResponse syncShopInfo() {
        BaseResponse response = new BaseResponse();
        log.info("开始同步数据...");
        List<WxUser> wxUsers = wxUserDao.getAllUserForDataSync();
        log.info("卖家总数量{}", wxUsers.size());
        int count = 0;
        for (WxUser wxUser : wxUsers) {
            // 查询是否已有店铺信息,有则跳过
            ShopInfoQuery query = new ShopInfoQuery();
            query.setShopkeeperOpenId(wxUser.getOpenid());
            List<Shop> shops = shopMapper.queryShopInfo(query);
            if (CollectionUtils.isNotEmpty(shops)) {
                count++;
                continue;
            }
            Shop shop = new Shop();
            shop.setShopName(wxUser.getNickName());
            shop.setShopkeeperOpenId(wxUser.getOpenid());
            shop.setShopkeeperNickname(wxUser.getNickName());
            shop.setShopHeadPicture(wxUser.getHeadImgUrl());
            shop.setGmtCreate(new Date());
            shop.setIsDeleted(false);
            shop.setShopIntro(wxUser.getNickName() + "的小店~");
            List<DragonAddr> dragonAddrs = dragonAddrDao.selectAllAndOrderByCreateTimeDesc(wxUser.getOpenid());
            if (CollectionUtils.isNotEmpty(dragonAddrs)) {
                DragonAddr dragonAddr = dragonAddrs.get(0);
                shop.setLatitude(dragonAddr.getLatitude());
                shop.setLongitude(dragonAddr.getLongitude());
                shop.setShowAddr(dragonAddr.getAddr());
            }
            int i = shopMapper.insertSelective(shop);
            if (i < 1) {
                log.info("店铺信息更新失败，用户为：{}", wxUser.toString());
            }
        }
        log.info("同步数据完成...本次共同步卖家店铺信息数量" + (wxUsers.size() - count));
        addSalesInfo(wxUsers);
        return response;
    }

    private void addSalesInfo(List<WxUser> wxUsers) {

        log.info("统计店铺销售数据并更新店铺信息开始...");
        for (WxUser wxUser : wxUsers) {
            ShopInfoQuery query = new ShopInfoQuery();
            query.setShopkeeperOpenId(wxUser.getOpenid());
            // 统计店铺销售数据并更新
            Shop sellerShop = statisticsService.countShopSalesAmountInfo(query);
            if (null == sellerShop) {
                log.error("统计店铺销售数据并更新操作失败!卖家用户信息{}", JsonUtils.toJson(wxUser));
            }
        }
        log.info("统计店铺销售数据并更新店铺信息结束...");
    }

    /**
     * 商品数据订正
     */
    @RequestMapping(value = "/correctionGoodsInfoData", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    public BaseResponse correctionGoodsInfoData() {
        // 查询所有商品信息列表
        List<GoodsInfo> goodsInfos = goodsInfoDao.queryAll();
        log.info("开始同步更新商品数据...商品总数{}", goodsInfos.size());
        for (GoodsInfo goodsInfo : goodsInfos) {
            if (!Optional.ofNullable(goodsInfo.getDeleted()).orElse(Boolean.FALSE)) {
                goodsInfo.setDeleted(false);
                goodsInfoDao.updateGoods(goodsInfo);
            }
        }
        log.info("同步更新商品数据完成...");
        return new BaseResponse().build(200, "ok");
    }

    /**
     * 接龙自提地址数据订正
     */
    @RequestMapping(value = "/correctionDragonAddr", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    public BaseResponse correctionDragonAddr() {
        // 查询所有接龙自提地址信息列表
        List<DragonAddr> dragonAddrs = dragonAddrDao.queryAll();
        log.info("开始同步更新接龙自提地址...商品总数{}", dragonAddrs.size());
        for (DragonAddr dragonAddr : dragonAddrs) {
            if (!Optional.ofNullable(dragonAddr.getDeleted()).orElse(Boolean.FALSE)) {
                dragonAddr.setDeleted(false);
                dragonAddrDao.updateByPrimaryKeySelective(dragonAddr);
            }
        }
        log.info("同步更新更新接龙自提地址完成...");
        return new BaseResponse().build(200, "ok");
    }

    /**
     * 接龙自提地址数据订正
     */
    @RequestMapping(value = "/syncShopRepurchase", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    public BaseResponse syncShopRepurchase() {
        statisticsService.syncShopRepurchase();
        return new BaseResponse().build(200, "ok");
    }

    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @RequestMapping(value = "syncGoodsRepurchase",method = RequestMethod.POST)
    public BaseResponse syncGoodsRepurchase(){
        BaseResponse response = new BaseResponse();
        log.info("开始同步商品复购缓存。。。");
        long start = System.currentTimeMillis();
        List<GoodsInfo> goodsInfos = goodsInfoDao.queryAll();
        for (GoodsInfo goodsInfo :goodsInfos){
            statisticsService.updateGoodsRepurchase(goodsInfo.getGoodsNum());
        }
        log.info("同步同步商品复购缓存完毕，耗时：{}",System.currentTimeMillis()-start);
        return response;
    }


    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @RequestMapping(value = "syncUserRepurchase",method = RequestMethod.POST)
    public BaseResponse syncUserRepurchase(){
        BaseResponse response = new BaseResponse();
        log.info("开始用户管理下用户复购缓存。。。");
        long start = System.currentTimeMillis();
        List<MyUser> userList = myUserService.getAll();
        for (MyUser myUser :userList){
            statisticsService.updateUserRepurchase(myUser.getUserOpenid(),myUser.getMyOpenid());
        }
        log.info("同步用户管理下用户复购缓存完毕，耗时：{}",System.currentTimeMillis()-start);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "countUserRepurChase",method = RequestMethod.POST)
    public BaseResponse countUserRepurChase(String buyerOpenId,String sellerOpenId){
        BaseResponse response = new BaseResponse();
        statisticsService.updateUserRepurchase(buyerOpenId, sellerOpenId);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "countShopRepurchase",method = RequestMethod.POST)
    public BaseResponse countShopRepurchase(String openOd){
        BaseResponse response = new BaseResponse();
        statisticsService.updateShopRepurchase(openOd);
        return response;
    }
}
