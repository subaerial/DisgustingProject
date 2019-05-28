package com.mhc.yunxian.service.impl;

import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.dao.DragonGoodsDao;
import com.mhc.yunxian.dao.OrderGoodsDao;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.*;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.JsonUtils;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/12/5.
 *
 * @author Alin
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {


    @Autowired
    private OrderInfoDao orderInfoDao;
    @Autowired
    private DragonService dragonService;
    @Autowired
    private OrderGoodsDao orderGoodsDao;
    @Autowired
    private GoodsInfoService goodsInfoService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private DrawMoneyService drawMoneyService;
    @Autowired
    private OrderGoodsService orderGoodsService;
    @Autowired
    private MoneyRecordService moneyRecordService;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private DragonGoodsDao dragonGoodsDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    RemoteCache redisCache;

    private static final String SHOP_REPURCHASE_PREFIX = "SHOP_REPURCHASE_PREFIX:";

    private static final String USER_REPURCHASE_SELLER = "USER_REPURCHASE_SELLER:";

    private static final String USER_REPURCHASE_BUYER = ",BUYER:";

    private static final String GOODS_REPURCHASE_PREFIX = "GOODS_REPURCHASE_PREFIX:";

    /**
     * 统计店铺的复购(接龙)订单数 re-purchase
     */
    @Override
    public Integer countDragonRepurchase(ShopInfoQuery query) {
        Long repurchaseNum = shopRepurchaseNum(query);

        return Math.toIntExact(repurchaseNum);
    }

    /**
     * 统计店铺商品的复购次数 re-purchase,返回商品列表
     * 1.查询店铺所有订单
     * 2.查询订单关联的商品列表,按买家分组
     * 3.按商品分组
     */
    @Override
    public List<GoodsInfoResponse> countGoodsRepurchase(ShopInfoQuery query) {
        WxUser wxUser = wxUserService.getWxUser(query.getShopkeeperOpenId());
        List<GoodsInfoResponse> responseList = new ArrayList<>();
        // 使用子查询方式获取卖家历史接龙的商品
        List<GoodsInfo> goodsInfos = goodsInfoService.getGoodsListByUser(wxUser.getOpenid());
        List<GoodsInfo> goodsInfoList = goodsInfoService.queryGoodsListByCreatorOpenId(wxUser.getOpenid());
        goodsInfoList.addAll(goodsInfos);
        List<GoodsInfo> list = goodsInfoService.filterAreadyDeletedGoods(goodsInfoList);
        List<GoodsInfo> allGoodsInfo = Lists.newArrayList();
        //去重
        for (GoodsInfo goodsInfo : list) {
            if (!allGoodsInfo.contains(goodsInfo)) {
                allGoodsInfo.add(goodsInfo);
            }
        }
        for (GoodsInfo info : allGoodsInfo) {
            GoodsInfoResponse response = new GoodsInfoResponse();
            DragonGoods byGoodsNum = dragonGoodsDao.findByGoodsNum(info.getGoodsNum());
            // 商品未发布接龙则不返回
            if (byGoodsNum != null) {
                List<String> dragonNum = new ArrayList<>();
                DragonInfo dragon = dragonService.getDragon(byGoodsNum.getDragonNum());
                if (dragon != null) {
                    if (DragonStatusEnum.IN_PROGRESS.getStatus().equals(dragon.getDragonStatus())) {
                        dragonNum.add(byGoodsNum.getDragonNum());
                    }
                }
                response.setDragonNums(dragonNum);
                response.setGoodsName(info.getGoodsName());
                response.setGoodsImg(info.getGoodsImg());
                response.setPrice(info.getPrice());
                response.setTotalNumber(info.getTotalNumber());
                Integer goodsRepurchase = this.getGoodsRepurchase(info.getGoodsNum());
                response.setRepurchase(goodsRepurchase);
                responseList.add(response);
            }
        }

        return responseList;
    }




    /**
     * 统计不同截单时间内的接龙人数
     */
    @Override
    public Integer countDragonUsersInCutOffTime() {
        return null;
    }

    /***
     * 统计累计接龙人数
     */
    @Override
    public Integer countDragonAllUsers(String dragonNum) {
        List<OrderInfo> orderInfos = orderInfoDao.selectOrder(dragonNum);
        int size = orderInfos.stream().collect(Collectors.groupingBy(OrderInfo::getOpenid)).size();
        return size;
    }

    /**
     * 统计店铺累计收入
     */
    @Override
    public Integer countDragonAllIncome(ShopInfoQuery query) {
        // 查询卖家发布的所有接龙生成的订单
        List<OrderInfo> orderInfos = orderInfoDao.selectOrderBySellerId(query.getShopkeeperOpenId());
        // 遍历订单集合,筛选出符合条件的订单
        Optional<Integer> reduce = orderInfos.stream().filter(orderInfo ->
                // 排除未完成,已退款,撤销和未付款的订单(有提现订单?)
                OrderStatusEnum.COMPELETED.equals(orderInfo.getOrderStatus()))
                .map(OrderInfo::getOrderMoney).reduce((a, b) -> a + b);

        return reduce.get();
    }

    /**
     * 统计不同截单时间段内的收入
     */
    @Override
    public Integer countDragonAllIncomeInCutOffTime() {
        return null;
    }

    /**
     * 统计优惠券的使用人数
     */
    @Override
    public Integer countAlreadyInUseCoupons() {
        return null;
    }

    /***
     * 统计店铺月成交订单量
     * 统计店铺本月销售额
     * 统计店铺复购口碑
     * 统计店铺已提现金额
     * 统计店铺余额(可提现)
     * 今日完成订单
     * 今日销售额
     */
    @Override
    public Shop countShopSalesAmountInfo(ShopInfoQuery query) {
        /**
         * 统计本月成交订单
         * 1.查询店主发布的所有接龙
         * 2.根据接龙编号查询本月已完成的所有订单,去除子订单
         * 3.统计店铺本月销售额
         */
        List<Shop> shops = shopMapper.queryShopInfo(query);
        if (CollectionUtils.isEmpty(shops)) {
            log.error("未找到卖家店铺信息卖家openId{}", JsonUtils.toJson(query.getShopkeeperOpenId()));
            return null;
        }
        Shop shop = shops.get(0);
        // 查询卖家发布的所有接龙生成的订单
        List<OrderInfo> orderInfos = orderInfoDao.selectOrderBySellerId(shop.getShopkeeperOpenId());
        // 获取本月第一天的时间毫秒
        Date firstDayOfMonth = DateUtil.getTimesMonthMorning();
        // 遍历订单集合,筛选出所有已完成的订单(补差子订单付款后也在其中)
        List<OrderInfo> allCompeletedOrders = orderInfos.stream().filter(orderInfo -> OrderStatusEnum.COMPELETED.getCode().equals(orderInfo.getOrderStatus()) ).collect(Collectors.toList());
        // 筛选出本月的已完成订单
        List<OrderInfo> compeletedOrdersOfMonth = allCompeletedOrders.stream().filter(orderInfo -> Optional.ofNullable(orderInfo.getUpdateTime()).orElse(new Date()).after(firstDayOfMonth)).collect(Collectors.toList());

        // 所有已完成订单
        shop.setShopFinishedOrder(Long.valueOf(allCompeletedOrders.size()));
        // 本月成交订单
        shop.setMonthlyFinishedOrder(Long.valueOf(compeletedOrdersOfMonth.size()));
        //筛选出本月的子订单
        List<OrderInfo> difOrders = orderInfos.stream().filter(orderInfo ->
             StringUtils.isNotBlank(orderInfo.getParentOrderNum()) && orderInfo.getCreateTime().after(firstDayOfMonth)
        ).collect(Collectors.toList());
        // 遍历符合条件的订单,统计本月销售额
        Optional<Integer> totalMoney = compeletedOrdersOfMonth.stream().map(OrderInfo::getOrderMoney).reduce((a, b) -> a + b);
        Integer monthMoney = totalMoney.orElse(0);
        for (OrderInfo orderInfo :difOrders){
            if(DifTypeEnum.RETREAT_ORDER.getType().equals(orderInfo.getOrderType())){
                monthMoney -= orderInfo.getOrderMoney();
            }
        }
        shop.setMonthlySales(monthMoney);
        // 获取当天凌晨0点时间
        Date morning = DateUtil.getTimesMorning();
        //当天的订单
        List<OrderInfo> compeletedOrdersOfDay = compeletedOrdersOfMonth.stream().filter(orderInfo -> Optional.ofNullable(orderInfo.getUpdateTime()).orElse(new Date()).after(morning))
                .collect(Collectors.toList());
        shop.setTodayFinishedOrder(Long.valueOf(compeletedOrdersOfDay.size()));
        Integer turnoverToday = compeletedOrdersOfDay.stream().map(OrderInfo::getOrderMoney).reduce((a, b) -> a + b).orElse(0);
        shop.setTurnoverToday(turnoverToday);
        /**
         * 统计店铺复购口碑
         * 1.查询店主所有接龙
         * 2.根据接龙编号查询所有订单
         * 3.按下单用户分组
         */
        Long repurchaseNum = shopRepurchaseNum(query);
        shop.setRepurchaseOrderCount(repurchaseNum);

        // 统计店铺已提现金额
        List<DrawMoney> drawMoneyList = drawMoneyService.getDrawMoneyDetail(shop.getShopkeeperOpenId());
        Integer totalDrawMoney = drawMoneyList.stream().filter(drawMoney -> MoneyRecordEnum.PRESENT_COMPLETION.getCode().equals(drawMoney.getStatus() + 1))
                .map(DrawMoney::getDrawMoney).reduce((a, b) -> a + b).orElse(Integer.valueOf(0));
        shop.setAlreadyWithdrawCash(totalDrawMoney);

        // 统计店铺余额(可提现)
        Balance balance = balanceService.getBalance(shop.getShopkeeperOpenId());
        shop.setCanWithdrawCash(Optional.ofNullable(balance.getBalance()).orElse(0));

        // 更新shop表
        shopMapper.updateByPrimaryKeySelective(shop);
        return shop;
    }


    public Long shopRepurchaseNum(ShopInfoQuery query) {
        long start = System.currentTimeMillis();
        WxUser wxUser = new WxUser();
        if (query.getShopId() != null) {
            Shop shop = shopMapper.selectByPrimaryKey(query.getShopId());
            // 使用子查询方式获取卖家历史接龙的商品
            Long repurchaseNum = this.getShopRepurchaseNum(shop.getShopkeeperOpenId());
            return repurchaseNum;
        } else if (query.getSessionId() != null) {
            wxUser = wxUserService.getUserBySessionId(query.getSessionId());
        } else if (query.getShopkeeperOpenId() != null) {
            wxUser = wxUserService.getWxUser(query.getShopkeeperOpenId());
        }

        // 使用子查询方式获取卖家历史接龙的商品
        Long repurchaseNum = this.getShopRepurchaseNum(wxUser.getOpenid());
        long end = System.currentTimeMillis();
        log.info("shopRepurchaseNum()耗时{}", end - start);

        return repurchaseNum;
    }

    @Override
    public Long getShopRepurchaseNum(String shopkeeperOpenId) {

        String ShopRepurchase = SHOP_REPURCHASE_PREFIX + shopkeeperOpenId;
        String repurchase = redisCache.get(ShopRepurchase);
        if (StringUtils.isNotBlank(repurchase)) {
            return Long.valueOf(repurchase);
        } else {
            log.warn("缓存击穿....shopkeeperOpenId:{}", shopkeeperOpenId);
            Long repurchaseNum = countShopRepurchase(shopkeeperOpenId);
            redisCache.set(ShopRepurchase, repurchaseNum.toString());
            return repurchaseNum;
        }
    }

    @Override
    public Long updateShopRepurchase(String shopkeeperOpenId) {
        String key = SHOP_REPURCHASE_PREFIX + shopkeeperOpenId;
        Long repurchaseNum = countShopRepurchase(shopkeeperOpenId);
        redisCache.set(key, repurchaseNum.toString());
        log.info("更新了商店铺缓存:{},复购为:{}",key,redisCache.get(key));
        return repurchaseNum;
    }


    @Override
    public Boolean syncShopRepurchase() {
        List<Shop> shops = shopMapper.queryAll();
        log.info("同步复购缓存开始.........");
        long start = System.currentTimeMillis();
        for (Shop shop : shops) {
            Long count = this.countShopRepurchase(shop.getShopkeeperOpenId());
            redisCache.set(SHOP_REPURCHASE_PREFIX + shop.getShopkeeperOpenId(), count.toString());
        }
        long end = System.currentTimeMillis();
        log.info("同步复购缓存结束.........共耗时:{}", end - start);
        return Boolean.TRUE;
    }

    public Long countShopRepurchase(String shopkeeperOpenId) {
        List<GoodsInfo> goodsInfos = goodsInfoService.getGoodsListByUser(shopkeeperOpenId);
        List<GoodsInfo> goodsInfoList = goodsInfoService.queryGoodsListByCreatorOpenId(shopkeeperOpenId);
        goodsInfoList.addAll(goodsInfos);

        //去重
        List<GoodsInfo> collect1 = goodsInfoList.stream().distinct().collect(Collectors.toList());

        Long repurchaseNum = 0L;
        // 查询商品所有订单
        // 根据用户openid分组
        // 订单列表长度即为复购次数+1
        for (GoodsInfo info : collect1) {
            List<OrderGoodsInfo> orderGoodsInfos = orderGoodsDao.selectOrderGoodsByGoodsNum(info.getGoodsNum());
            if (CollectionUtils.isNotEmpty(orderGoodsInfos)) {
                List<OrderGoodsInfo> collect = orderGoodsInfos.parallelStream().filter(orderGoodsInfo -> {
                    OrderInfo orderInfo = orderInfoDao.selectOrderByOrderNum(orderGoodsInfo.getOrderNum());
                    boolean b = true;
                    if (orderInfo != null) {
                        if(StringUtils.isNotBlank(orderInfo.getParentOrderNum())){
                            b = false;
                        }else {
                            if ((OrderStatusEnum.CANCELLED.getCode().equals(orderInfo.getOrderStatus())
                                    || OrderStatusEnum.PENDING_PAYMENT.getCode().equals(orderInfo.getOrderStatus())
                                    || OrderStatusEnum.REFUNDED.getCode().equals(orderInfo.getOrderStatus()))) {
                                b = false;
                            }
                        }
                    }
                    return b;
                }).collect(Collectors.toList());

                Map<String, List<OrderGoodsInfo>> collectByOpenId = collect.parallelStream().collect(Collectors.groupingBy(OrderGoodsInfo::getOpenid));
                Iterator<String> orderGoodsIter = collectByOpenId.keySet().iterator();
                while (orderGoodsIter.hasNext()) {
                    String openid = orderGoodsIter.next();
                    List<OrderGoodsInfo> orderGoods = collectByOpenId.get(openid);
                    repurchaseNum += (orderGoods.size() - 1);
                }
            }
        }
        return repurchaseNum;
    }

    /**
     * <p> 获取商品复购 </p>
     * @param goodsNum
     * @return Integer
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     *
     */
    @Override
    public Integer getGoodsRepurchase(String goodsNum) {
        String key = GOODS_REPURCHASE_PREFIX + goodsNum;
        String goodsRepurchase = redisCache.get(key);
        if(StringUtils.isNotBlank(goodsRepurchase)){
            return Integer.parseInt(goodsRepurchase);
        }else {
            final Integer repurChaseCount = this.countGoodsRepurchase(goodsNum);
            redisCache.set(key,String.valueOf(repurChaseCount));
            return repurChaseCount;
        }
    }

    /**
     * <p> 更新商品复购 </p>
     * @param goodsNum
     * @return void
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     *
     */
    @Override
    public void updateGoodsRepurchase(String goodsNum) {
        String key = GOODS_REPURCHASE_PREFIX + goodsNum;
        Integer repurchase = countGoodsRepurchase(goodsNum);
        redisCache.set(key,String.valueOf(repurchase));
        log.info("更新了商品缓存:{},复购为:{}",key,redisCache.get(key));
    }


    /**
     * <p> 计算商品的复购 </p>
     * @param goodsNum
     * @return Integer
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     *
     */
    private Integer countGoodsRepurchase(String goodsNum){
        Integer repurchaseNum = 0;
        //查询订单商品
        List<OrderGoodsInfo> orderGoodsInfos = orderGoodsDao.selectOrderGoodsByGoodsNum(goodsNum);
        if(CollectionUtils.isEmpty(orderGoodsInfos)){
            repurchaseNum = 0 ;
        }else {
            List<OrderGoodsInfo> collect = orderGoodsInfos.stream().filter(orderGoodsInfo -> {
                OrderInfo orderInfo = orderInfoDao.selectOrderByOrderNum(orderGoodsInfo.getOrderNum());
                if(null == orderInfo){
                    return Boolean.FALSE;
                }
                //过滤等待付款、取消、退款的订单
                if (OrderStatusEnum.PENDING_PAYMENT.getCode().equals(orderInfo.getOrderStatus())
                        || OrderStatusEnum.CANCELLED.getCode().equals(orderInfo.getOrderStatus())
                        || OrderStatusEnum.REFUNDED.getCode().equals(orderInfo.getOrderStatus())) {
                    return Boolean.FALSE;
                }

                return Boolean.TRUE;
            }).collect(Collectors.toList());

            Map<String, List<OrderGoodsInfo>> collectByOpenId = collect.stream().collect(Collectors.groupingBy(OrderGoodsInfo::getOpenid));
            Iterator<String> orderGoodsIter = collectByOpenId.keySet().iterator();

            while (orderGoodsIter.hasNext()) {
                String openid = orderGoodsIter.next();
                List<OrderGoodsInfo> orderGoods = collectByOpenId.get(openid);
                repurchaseNum += orderGoods.size() - 1;
            }
        }
        return repurchaseNum;
    }


    /**
     * <p> 获得用户管理下的复购缓存 </p>
     * @param buyerOpenId
     * @param sellerOpenId
     * @return Integer
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     */
    @Override
    public Integer getUserRepurchase(String buyerOpenId,String sellerOpenId){
        String key = USER_REPURCHASE_SELLER + sellerOpenId +USER_REPURCHASE_BUYER +buyerOpenId;
        String repurchase = redisCache.get(key);
        if(StringUtils.isNotBlank(repurchase)){
            return Integer.parseInt(repurchase);
        }else {
            final Integer repurChaseCount = this.countUserRepurChase(buyerOpenId, sellerOpenId);
            redisCache.set(key,String.valueOf(repurChaseCount));
            return repurChaseCount;
        }
    }

    /**
     * <p> 更新缓存 </p>
     * @param buyerOpenId
     * @param sellerOpenId
     * @return void
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     *
     */
    @Override
    public void updateUserRepurchase(String buyerOpenId,String sellerOpenId){
        String key = USER_REPURCHASE_SELLER + sellerOpenId +USER_REPURCHASE_BUYER +buyerOpenId;
        //重新计算
        Integer repurChase = countUserRepurChase(buyerOpenId, sellerOpenId);
        redisCache.set(key,String.valueOf(repurChase));
        log.info("更新了缓存:{},复购数为:{}",key,redisCache.get(key));
    }

    /**
     * <p> 用户管理 计算买家在卖家下的复购口碑 </p>
     * @param buyerOpenId
     * @param sellerOpenId
     * @return Integer
     * @author 昊天
     * @date
     * @since V1.1.0-SNAPSHOT
     */
    private Integer countUserRepurChase(String buyerOpenId,String sellerOpenId){
        //查询出该买家的所有订单
        List<OrderInfo> orders = orderService.getOrderByOpenid(buyerOpenId);
        //myOrders存放在该卖家店铺下的订单
        List<OrderInfo> myOrders = Lists.newArrayList();
        for (OrderInfo o : orders) {
            //过滤待付款、取消、退款完成的订单
            if (OrderStatusEnum.PENDING_PAYMENT.getCode().equals(o.getOrderStatus()) ||
                    OrderStatusEnum.REFUNDED.getCode().equals(o.getOrderStatus()) ||
                    OrderStatusEnum.CANCELLED.getCode().equals(o.getOrderStatus())) {
                continue;
            }
            //过滤子订单
            if (org.apache.commons.lang.StringUtils.isNotBlank(o.getParentOrderNum())) {
                continue;
            }
            //过滤不在该卖家下购买的订单
            DragonInfo dragonInfo = dragonService.getDragon(o.getDragonNum());
            if (null == dragonInfo) {
                continue;
            }
            if (!sellerOpenId.equals(dragonInfo.getOpenid())) {
                continue;
            }
            myOrders.add(o);
        }
        Integer repurChaseCount = 0;
        List<OrderGoodsInfo> orderGoodsInfoList = Lists.newArrayList();
        for (OrderInfo myOrder : myOrders) {
            //查询出订单商品
            List<OrderGoodsInfo> orderGoods = orderGoodsService.getOrderGoods(myOrder.getOrderNum());
            orderGoodsInfoList.addAll(orderGoods);
        }
        //按照商品num分组
        Map<String, List<OrderGoodsInfo>> orderGoodsMap = orderGoodsInfoList.stream().collect(Collectors.groupingBy(OrderGoodsInfo::getGoodsNum));
        for (String goodsNum : orderGoodsMap.keySet()) {
            List<OrderGoodsInfo> goods = orderGoodsMap.get(goodsNum);
            repurChaseCount += (goods.size() - 1);
        }
        return repurChaseCount;
    }
}
