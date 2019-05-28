package com.mhc.yunxian.controller;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.coupon.*;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.CouponUserDao;
import com.mhc.yunxian.dao.ShopMapper;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.enums.*;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @author Administrator
 */
@RestController
@Slf4j
@RequestMapping("/yunxian/coupon")
public class CouponController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private CouponUserDao couponUserDao;
    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    RemoteCache redisCache;

    @Autowired
    private DragonGoodsService dragonGoodsService;

    @Autowired
    private DragonService dragonService;

    @Autowired
    private GoodsInfoService goodsInfoService;

    @Autowired
    private OrderService orderService;

    @Value("coupon.key")
    private String couponKey;

    private static final Integer HAS_GOT = 1;

    private static final Integer NOT_GOT = 0;

    /**
     * 创建红包
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/addCoupon", method = RequestMethod.POST)
    public BaseResponse addCoupon(@RequestBody @Valid AddCouponRequest request, BindingResult errors) {

        BaseResponse response = new BaseResponse();

        if (errors.hasErrors()) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        try {

            WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

            if (wxUser == null) {
                return response.build(RespStatus.SESSION_ID_EXPIRE);
            }

            if (request.getCouponAmount() < 1 || request.getTotalAmount() < 1) {
                return response.build(RespStatus.AMOUNT_CAN_NOT_LESS_THAN_ONE);
            }
            int couponType = request.getCouponType();
            List<String> goodsNumList = request.getGoodsNumList();

            if (CouponTypeEnum.DISCOUNT_ASSIGN_GOODS.getType().equals(couponType) || CouponTypeEnum.DISCOUNT_OVER_ALL.getType().equals(couponType)) {
                // 折扣必须小于99折
                if (request.getCouponAmount() > 99 || request.getCouponAmount() < 0) {
                    return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT.getKey(), "折扣必须在0.1~9.9折之间");
                }
            }
            if (CouponTypeEnum.MONEY_ASSIGN_GOODS.getType().equals(couponType) || CouponTypeEnum.DISCOUNT_ASSIGN_GOODS.getType().equals(couponType)) {
                if (CollectionUtils.isEmpty(goodsNumList)) {
                    return response.build(RespStatus.GOODS_NUM_CAN_NOT_BE_BLANK);
                }
            }
            // 遍历商品列表,查看是否包含已删除的商品
            for (String goodsNum : goodsNumList) {
                GoodsInfo goods = goodsInfoService.getGoods(goodsNum);
                if (null == goods || Optional.ofNullable(goods.getDeleted()).orElse(Boolean.FALSE)) {
                    return response.build(RespStatus.GOODS_NOT_EXIST_OR_DELETED);
                }
            }
            if (!CollectionUtils.isEmpty(goodsNumList) && goodsNumList.size() > 10) {
                return response.build(RespStatus.GOODS_TOO_MANY);
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return response.build(RespStatus.DATE_CAN_NOT_BLANK);
            }

            if (request.getEndTime().getTime() < System.currentTimeMillis()) {
                return response.build(RespStatus.END_TIME_ERROR);
            }

            if (request.getEndTime().getTime() < request.getStartTime().getTime()) {
                return response.build(RespStatus.TIME_ERROR);
            }

            if (!couponService.addCoupon(request)) {
                return response.build(RespStatus.COUPON_ADD_ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("创建红包错误：", e);
            return response.build(RespStatus.SYSTEM_ERROR);
        }
        return response;
    }


    /**
     * 卖家红包列表(商家)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/myCouponList", method = RequestMethod.POST)
    public MyCouponListResponse myCouponList(@RequestBody @Valid MyCouponListRequest request, BindingResult errors) {

        MyCouponListResponse response = new MyCouponListResponse();

        if (errors.hasErrors()) {
            return (MyCouponListResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        try {
            WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
            if (wxUser == null) {
                return (MyCouponListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
            }

            List<MyCouponDetail> result = couponService.getAllMyCouponList(wxUser.getId());
            // 根据sellerCouponStatus筛选
            Integer sellerCouponStatus = request.getSellerCouponStatus();
            List<MyCouponDetail> collect = null;
            if (sellerCouponStatus != null && Integer.valueOf(0).equals(sellerCouponStatus)) {
                // 进行中的
                collect = result.stream().filter(myCouponDetail -> myCouponDetail.getState() == 0
                        // 结束时间在当前时间之后
                        && myCouponDetail.getGmtEndTime().after(new Date())
                ).sorted(Comparator.comparing(MyCouponDetail::getGmtEndTime).reversed())
                        // 逻辑分页
                        .skip((request.getPage() - 1) * request.getPageSize()).limit(request.getPageSize())
                        .collect(Collectors.toList());
            } else {
                // 已失效的
                collect = result.stream().filter(myCouponDetail -> myCouponDetail.getState() == -1
                        // 结束时间在当前时间之后
                        || myCouponDetail.getGmtEndTime().before(new Date())
                ).sorted(Comparator.comparing(MyCouponDetail::getGmtEndTime).reversed())
                        // 逻辑分页
                        .skip((request.getPage() - 1) * request.getPageSize()).limit(request.getPageSize())
                        .collect(Collectors.toList());
            }
            // 统计券的使用数量,设置适用商品列表,填充响应数据
            collect.forEach(e -> {
                Integer integer = couponUserDao.countCouponUsedAmount(e.getCouponId());
                e.setHasUseAmount(integer);
                if (StringUtils.isNotBlank(e.getGoodsNum())) {
                    List<String> goodsNumList = Arrays.asList(e.getGoodsNum().split(","));
                    List<String> goodsNameList = new ArrayList<>();

                    for (String goodsNum : goodsNumList) {
                        GoodsInfo goodsInfo = goodsInfoService.getGoods(goodsNum);
                        if (goodsInfo != null) {
                            goodsNameList.add(goodsInfo.getGoodsName());
                        }
                    }
                    e.setGoodsNameList(goodsNameList);
                }
            });
            List<MyCouponDetail> details = collect.stream().sorted(Comparator.comparing(MyCouponDetail::getGmtCreateTime).reversed())
                    .collect(Collectors.toList());
            response.setCouponDetails(details);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("卖家红包列表错误：", e);
            return (MyCouponListResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        return response;
    }

    @RequestMapping(value = "/shopCanGetCouponList", method = RequestMethod.POST)
    @ResponseBody
    public MyCouponListResponse shopCanGetCouponList(@RequestBody ShopInfoQuery query) {
        MyCouponListResponse response = new MyCouponListResponse();
        WxUser buyer = wxUserService.getUserBySessionId(query.getSessionId());
        if (buyer == null) {
            return (MyCouponListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        String sellerId;
        if (StringUtils.isBlank(query.getShopkeeperOpenId())) {
            List<Shop> shops = shopService.queryShop(query);
            sellerId = shops.get(0).getShopkeeperOpenId();
        } else {
            sellerId = query.getShopkeeperOpenId();
        }
        WxUser seller = wxUserService.getWxUser(sellerId);
        //查看登陆用户领取的红包
        List<MyGotCouponDetail> buyerCoupons = couponUserService.getMyCouponList(buyer.getId());
        List<Integer> buyerCouponIds = buyerCoupons.stream().map(MyGotCouponDetail::getCouponId).collect(Collectors.toList());
        //查看该店铺下红包
        List<MyCouponDetail> sellerCoupons = couponService.myCouponList(seller.getId());
        // 卖家订单列表
        List<OrderInfo> sellerOrders = orderService.getOrderBySellerId(sellerId);
        //过滤除当前买家 在该店铺下的订单
        List<OrderInfo> buyerOrders = sellerOrders.stream().filter(orderInfo -> orderInfo.getOpenid().equals(buyer.getOpenid())).collect(Collectors.toList());
        //过滤下架和下单限制的
        List<MyCouponDetail> firstList = Lists.newArrayList();
        for (MyCouponDetail sellerCoupon : sellerCoupons) {
            Integer isHasGot = NOT_GOT;
            if (buyerCouponIds.contains(sellerCoupon.getCouponId())) {
                isHasGot = HAS_GOT;
            }
            sellerCoupon.setIsHasGot(isHasGot);
            if (CouponStateEnum.EXPIRE_COUPON.getCode().equals(sellerCoupon.getState())) {
                //判读红包是否下架
                if (buyerCouponIds.contains(sellerCoupon.getCouponId())) {
                    //如果买家已经领取该红包则显示
                    firstList.add(sellerCoupon);
                }
            } else {
                if (sellerCoupon.getHasGotAmount() < sellerCoupon.getTotalAmount()) {
                    //判断红包领取数量是否充足
                    if (CouponLimitStateEnum.NEW_USER.getState().equals(sellerCoupon.getUserLimitState())) {
                        //如果是新用户，下单数小于限制数
                        if (buyerOrders.size() < sellerCoupon.getUserLimitNum()) {
                            firstList.add(sellerCoupon);
                        }
                    } else if (CouponLimitStateEnum.OLD_USER.equals(sellerCoupon.getUserLimitState())) {
                        //如果是旧用户，下单数大于限制数
                        if (buyerOrders.size() > sellerCoupon.getUserLimitNum()) {
                            firstList.add(sellerCoupon);
                        }
                    } else {
                        firstList.add(sellerCoupon);
                    }
                } else {
                    if (buyerCouponIds.contains(sellerCoupon.getCouponId())) {
                        //如果买家已经领取该红包则显示
                        firstList.add(sellerCoupon);
                    }
                }
            }
        }
        List<MyCouponDetail> secondList = Lists.newArrayList();
        if (StringUtils.isNotBlank(query.getDragonNum())) {
            //对接龙进行过滤
            List<DragonGoods> dragonGoodsList = dragonGoodsService.getGoodsNUM(query.getDragonNum());
            List<String> dragonGoodsNums = dragonGoodsList.stream().map(DragonGoods::getGoodsNum).collect(Collectors.toList());
            for (MyCouponDetail coupon : firstList) {
                String goodsNums = coupon.getGoodsNum();
                if (StringUtils.isNotBlank(goodsNums)) {
                    //限制使用的商品
                    List<String> goodsNumList = Arrays.asList(goodsNums.split(","));
                    goodsNumList.forEach(x -> {
                        //有任意一个满足的商品则添加
                        if (dragonGoodsNums.contains(x)) {
                            //这边会造成重复
                            secondList.add(coupon);
                        }
                    });
                } else {
                    //没有限制说明通用
                    secondList.add(coupon);
                }
            }
        } else {
            secondList.addAll(firstList);
        }
        //按创建时间排序(去重)
        List<MyCouponDetail> finalResult = secondList.stream().distinct().sorted(Comparator.comparing(MyCouponDetail::getGmtCreateTime).reversed()).collect(Collectors.toList());
        response.setCouponDetails(finalResult);
        return response;
    }

    /**
     * 展示店铺可领取红包列表(废弃)
     */
    @RequestMapping(value = "/shopCouponList", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public MyCouponListResponse shopCouponList(@RequestBody ShopInfoQuery query) {
        WxUser wxUser = wxUserService.getUserBySessionId(query.getSessionId());
        MyCouponListResponse response = new MyCouponListResponse();
        if (wxUser == null) {
            return (MyCouponListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(query.getShopkeeperOpenId())) {
            return (MyCouponListResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        List<Shop> shops = shopMapper.queryShopInfo(query);
        if (CollectionUtils.isEmpty(shops)) {
            return (MyCouponListResponse) response.build(RespStatus.SUCCESS);
        }
        Shop shop = shops.get(0);
        WxUser seller = wxUserService.getWxUser(shop.getShopkeeperOpenId());
        // 卖家订单列表
        List<OrderInfo> sellerOrders = orderService.getOrderBySellerId(seller.getOpenid());
        List<MyCouponDetail> list = new ArrayList<>();
        // 查询商家未过期的券
        List<MyCouponDetail> myCouponDetails = couponService.myCouponList(seller.getId());
        Iterator<MyCouponDetail> iterator = myCouponDetails.iterator();
        while (iterator.hasNext()) {
            MyCouponDetail next = iterator.next();
            next.setIsHasGot(0);
            CouponUser couponUser = couponUserDao.selectByUidAndCouponId(wxUser.getId(), next.getCouponId());
            if (couponUser != null) {
                // 已有人领取,判断是不是当前用户,不是: 不扎实 ,是:展示
                if (couponUser.getUserId().equals(wxUser.getId())) {
                    // 是当前用户
                    next.setIsHasGot(1);
                } else {
                    // 不是当前用户,跳过
                    continue;
                }
            } else {
                // 没有人领过
                if (next.getState() == -1) {
                    // 没人领过但是已经下架,不展示
                    continue;
                }
                String s = redisCache.get(couponKey + next.getCouponId());
                if (org.apache.commons.lang3.StringUtils.isBlank(s)) {
                    //  redis获取不到券的剩余数量时
                    s = String.valueOf(next.getTotalAmount() - next.getIsHasGot());
                }
                Integer couponCount = Integer.valueOf(s);
                if (couponCount <= 0) {
                    // 发放完了,领取过得用户展示,没领取过的不展示
                    continue;
                }

            }

            //使用商品
            String goodsNum = next.getGoodsNum();
            if (goodsNum != null) {
                String[] goodNum = goodsNum.split(",");
                List<String> goodsNameList = Lists.newArrayList();
                for (String info : goodNum) {
                    GoodsInfo goods = goodsInfoService.getGoods(info);
                    goodsNameList.add(goods.getGoodsName());
                }
                next.setGoodsNameList(goodsNameList);
            }
            // 卖家下单用户openId匹配当前用户openId
            List<OrderInfo> collect = sellerOrders.stream().filter(orderGoodsInfo -> orderGoodsInfo.getOpenid().equals(wxUser.getOpenid()))
                    .collect(Collectors.toList());
            // 新用户下单数 < 限制数
            if (CouponLimitStateEnum.NEW_USER.getState().equals(next.getUserLimitState())
                    && collect.size() < next.getUserLimitNum()) {
                // 接龙详情页,只展示接龙里面有的商品对应的券和通用券
                if (StringUtils.isNotBlank(query.getDragonNum())) {
                    this.filterCoupon(query.getDragonNum(), list, next);
                } else {
                    list.add(next);
                }

            }
            // 新用户下单数 > 限制数
            if (CouponLimitStateEnum.OLD_USER.getState().equals(next.getUserLimitState())
                    && collect.size() > next.getUserLimitNum()) {
                // 接龙详情页,只展示接龙里面有的商品对应的券和通用券
                if (StringUtils.isNotBlank(query.getDragonNum())) {
                    this.filterCoupon(query.getDragonNum(), list, next);
                } else {
                    list.add(next);
                }
            }
            // 适用所有用户不过滤
            if (CouponLimitStateEnum.ALL_USER.getState().equals(next.getUserLimitState())) {
                list.add(next);
            }
        }


        Set<MyCouponDetail> collect = new HashSet<>(list);
        ArrayList<MyCouponDetail> myCouponDetailsList = Lists.newArrayList(collect);

        List<MyCouponDetail> collect1 = myCouponDetailsList.stream().sorted(Comparator.comparing(MyCouponDetail::getGmtCreateTime).reversed()).collect(Collectors.toList());

        response.setCouponDetails(collect1);
        return response;
    }

    /**
     * @param dragonNum    接龙编号
     * @param list         存放符合条件的券的集合
     * @param couponDetail 待过滤的券
     */
    private void filterCoupon(String dragonNum, List<MyCouponDetail> list, MyCouponDetail couponDetail) {
        List<DragonGoods> dragonGoodsList = dragonGoodsService.getGoodsNUM(dragonNum);
        String goodsNum = couponDetail.getGoodsNum();
        String splitString = ",";
        boolean notBlank = StringUtils.isNotBlank(goodsNum);
        if (notBlank && goodsNum.contains(splitString)) {
            String[] split = goodsNum.split(splitString);
            for (String str : split) {
                Optional<DragonGoods> any = dragonGoodsList.stream().filter(x -> x.getGoodsNum().contains(str)).findAny();
                if (any.isPresent()) {
                    // 可能添加重复的券
                    list.add(couponDetail);
                }
            }
        } else if (notBlank) {
            Optional<DragonGoods> any = dragonGoodsList.stream().filter(x -> x.getGoodsNum().equals(goodsNum)).findAny();
            if (any.isPresent()) {
                list.add(couponDetail);
            }
        }
        if (!notBlank) {
            // 通用券不过滤
            list.add(couponDetail);
        }
    }

    /**
     * 买家红包列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/myGotCouponList", method = RequestMethod.POST)
    public MyCouponListResponse myGotCouponList(@RequestBody @Valid MyCouponListRequest request, BindingResult errors) {

        MyCouponListResponse response = new MyCouponListResponse();
        if (errors.hasErrors()) {
            return (MyCouponListResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        try {
            WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

            if (wxUser == null) {
                return (MyCouponListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
            }
            Integer couponStatus = request.getCouponStatus();

            List<MyGotCouponDetail> result = null;
            List<MyGotCouponDetail> allCouponList = addGoodsAndShopInfo(wxUser);

            if (couponStatus != null && Integer.valueOf(0).equals(couponStatus)) {
                // 返回可用的红包
                result = allCouponList.stream().filter(myGotCouponDetail ->
                        myGotCouponDetail.getGmtEndTime().after(new Date())).collect(Collectors.toList());

                /**
                 * 对于指定产品的直减券、折扣券：
                 * 需要其对应的进行中的接龙来决定跳转的页面，
                 * 1.查询出所有所有包含指定商品的接龙，
                 * 2.判断接龙是否进行中
                 * 3.如果满足则返回接龙编号
                 */

                result.forEach(myGotCouponDetail -> {
                    if (CouponTypeEnum.MONEY_ASSIGN_GOODS.getType().equals(myGotCouponDetail.getCouponType())
                            || CouponTypeEnum.DISCOUNT_ASSIGN_GOODS.getType().equals(myGotCouponDetail.getCouponType())) {

                        List<String> dragonNums = couponService.inProcessDragon(myGotCouponDetail.getGoodsNum());
                        myGotCouponDetail.setDragonNumList(dragonNums);

                    }
                });

            }
            if (couponStatus != null && Integer.valueOf(-1).equals(couponStatus)) {
                // 返回已失效的红包
                result = allCouponList.stream().filter(myGotCouponDetail ->
                        myGotCouponDetail.getGmtEndTime().before(new Date())).collect(Collectors.toList());

            }
            if (couponStatus != null && Integer.valueOf(1).equals(couponStatus)) {
                // 返回已使用的
                result = allCouponList.stream().filter(myGotCouponDetail ->
                        Integer.valueOf(1).equals(myGotCouponDetail.getIsUsed())
                ).collect(Collectors.toList());
            }
            response.setCouponDetails(result);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("买家红包列表错误：", e);
            return (MyCouponListResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        return response;
    }

    private List<MyGotCouponDetail> addGoodsAndShopInfo(WxUser wxUser) {
        List<MyGotCouponDetail> result = couponService.getAllCouponList(wxUser.getId());
        if (!CollectionUtils.isEmpty(result)) {
            result.stream().forEach(myGotCouponDetail -> {
                if (StringUtils.isNotBlank(myGotCouponDetail.getGoodsNum())) {
                    List<String> goodsNumList = Arrays.asList(myGotCouponDetail.getGoodsNum().split(","));
                    List<String> goodsNameList = new ArrayList<>();
                    for (String goodsNum : goodsNumList) {
                        GoodsInfo goodsInfo = goodsInfoService.getGoods(goodsNum);
                        if (goodsInfo != null && !goodsInfo.getDeleted()) {
                            goodsNameList.add(goodsInfo.getGoodsName());
                        }
                    }
                    myGotCouponDetail.setGoodsNameList(goodsNameList);
                    if (goodsNumList.size() == 1) {
                        String goodsNum = goodsNumList.get(0);
                        DragonGoods dragonGoods = dragonGoodsService.findByGoodsNum(goodsNum);
                        if (dragonGoods == null) {
                            log.error("接龍商品信息不存在!商品编号{}", goodsNum);
                            //throw new DataException("商品不存在");
                        }
                        if (dragonGoods != null && StringUtils.isNotBlank(dragonGoods.getDragonNum())) {
                            DragonInfo dragonInfo = dragonService.getDragon(dragonGoods.getDragonNum());
                            if (dragonInfo == null) {
                                log.error("接龍商品信息不存在!商品编号{}", dragonGoods.getDragonNum());
                                //throw new DataException("接龙不存在");
                            }
                            //判断接龙是否可以使用电子卷并且还在进行中
                            // if (dragonInfo.getIsCoupon() == 1 && dragonInfo.getDragonStatus() == 0){
                            if (dragonInfo != null && dragonInfo.getDragonStatus() == 0) {
                                myGotCouponDetail.setDragonNum(dragonInfo.getDragonNum());
                            }
                        }
                    }
                }
                // 填充信息
                addCouponInfo(myGotCouponDetail);
            });
        }
        return result;
    }

    private void addCouponInfo(MyGotCouponDetail myGotCouponDetail) {
        // 查询发券店主的店铺信息
        ShopInfoQuery query = new ShopInfoQuery();
        query.setShopkeeperOpenId(myGotCouponDetail.getSellerOpenid());
        Shop shop = shopMapper.queryShopInfo(query).get(0);
        myGotCouponDetail.setShopName(shop.getShopName());
        myGotCouponDetail.setShopId(shop.getShopId());
        // 获取相应券的已使用数量
        Integer integer = couponUserDao.countCouponUsedAmount(myGotCouponDetail.getCouponId());
        myGotCouponDetail.setAlreadyUseNum(integer);
    }


    /**
     * 红包领取页(券详情)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/couponGetPage", method = RequestMethod.POST)
    public CouponGetPageResponse couponGetPage(@RequestBody @Valid CouponGetPageRequest request, BindingResult errors) {

        CouponGetPageResponse response = new CouponGetPageResponse();

        if (errors.hasErrors()) {
            return (CouponGetPageResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        try {

            Coupon coupon = couponService.getCoupon(request.getCouponId());

            if (coupon == null) {
                return (CouponGetPageResponse) response.build(RespStatus.COUPON_NOT_EXIST);
            }
            response.setState(coupon.getState());
            //商品不为空，
            if (StringUtils.isNotBlank(coupon.getGoodsNum())) {

                List<String> goodsNumList = Arrays.asList(coupon.getGoodsNum().split(","));

                List<String> goodsNameList = new ArrayList<>();

                for (String goodsNum : goodsNumList) {
                    GoodsInfo goodsInfo = goodsInfoService.getGoods(goodsNum);

                    if (goodsInfo != null) {
                        goodsNameList.add(goodsInfo.getGoodsName());
                    }
                }

                List<String> dragonNum = couponService.inProcessDragon(coupon.getGoodsNum());
                response.setDragonNumList(dragonNum);

                response.setGoodsNameList(goodsNameList);

                if (goodsNumList.size() == 1) {

                    String goodsNum = goodsNumList.get(0);

                    DragonGoods dragonGoods = dragonGoodsService.findByGoodsNum(goodsNum);
                    if (dragonGoods != null) {
                        //throw new DataException("该商家暂未发布相关商品的接龙");
                        if (StringUtils.isNotBlank(dragonGoods.getDragonNum())) {
                            DragonInfo dragonInfo = dragonService.getDragon(dragonGoods.getDragonNum());
                            if (dragonInfo == null) {
                                throw new DataException("接龙不存在");
                            }
                            //判断接龙是否可以使用电子卷并且还在进行中
                            // if (dragonInfo.getIsCoupon() == 1 && dragonInfo.getDragonStatus() == 0){
                            if (dragonInfo.getDragonStatus() == 0) {
                                response.setDragonNum(dragonInfo.getDragonNum());
                            }
                        }
                    }

                }

            }

            //卖家信息
            WxUser wxUser = wxUserService.getWxUserById(coupon.getUserId());
            if (wxUser == null) {
                return (CouponGetPageResponse) response.build(RespStatus.SYSTEM_ERROR);
            }
            Shop shopByKeeperOpenid = shopMapper.getShopByKeeperOpenid(wxUser.getOpenid());
            if (shopByKeeperOpenid == null) {
                return (CouponGetPageResponse) response.build(RespStatus.SYSTEM_ERROR);
            }
            //进入者
            WxUser buyUser = wxUserService.getUserBySessionId(request.getSessionId());
            if (buyUser == null) {
                return (CouponGetPageResponse) response.build(RespStatus.USER_NOT_EXIST);
            }

            int orderNum = orderService.countCompleteNumByOpenid(buyUser.getOpenid());

            if (coupon.getUserId().equals(buyUser.getId())) {
                response.setIsSeller(1);
            }

            if (couponService.isGotCoupon(buyUser.getId(), coupon.getCouponId())) {
                response.setIsGot(1);
            }

            PageHelper.startPage(request.getPage(), request.getPageSize());
            List<BuyerDetail> buyerDetails = couponService.getBuyerList(coupon.getCouponId());
            // 遍历领取用户列表,添加已使用券的使用接龙信息
            AtomicInteger alreadyUseNum = new AtomicInteger(0);
            buyerDetails.stream().forEach(buyerDetail -> {
                boolean flag = Integer.valueOf(1).equals(buyerDetail.getIsUsed());
                if (flag) {
                    alreadyUseNum.getAndIncrement();
                    DragonInfo dragonInfo = orderService.getDragonInfoByCouponUserId(buyerDetail.getCouponUserId());
                    if (null != dragonInfo) {
                        buyerDetail.setDragonName(dragonInfo.getDragonTitle());
                        buyerDetail.setDragonNum(dragonInfo.getDragonNum());
                    } else {
                        log.error("根据已使用券用户关联id未找到对应的接龙订单信息{}", buyerDetail.toString());
                    }
                }
            });
            response.setBuyerDetailList(buyerDetails);
            response.setAlreadyUseNum(alreadyUseNum.get());
            response.setCouponAmount(coupon.getCouponAmount());
            response.setCouponType(coupon.getCouponType());
            response.setGmtStartTime(coupon.getGmtStartTime());
            response.setGmtEndTime(coupon.getGmtEndTime());
            response.setTotalAmount(coupon.getTotalAmount());
            response.setHasGotAmount(coupon.getHasGotAmount());
            response.setSellerName(wxUser.getNickName());
            response.setSellerHeadImage(wxUser.getHeadImgUrl());
            response.setOpenId(wxUser.getOpenid());
            response.setUserLimitState(coupon.getUserLimitState());
            response.setUserLimitNum(coupon.getUserLimitNum());
            response.setOrderNum(orderNum);
            response.setShopId(shopByKeeperOpenid.getShopId());

            if (coupon.getTotalAmount().equals(coupon.getHasGotAmount())) {
                response.setTotalGotTime(
                        coupon.getGmtUpdateTime().getTime() - coupon.getGmtCreateTime().getTime()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("红包领取页错误错误：", e);
            return (CouponGetPageResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        return response;
    }


    /**
     * 买家领取红包
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/receiveCoupon", method = RequestMethod.POST)
    public BaseResponse receiveCoupon(@RequestBody @Valid GetCouponRequest request, BindingResult errors) {

        BaseResponse response = new BaseResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (errors.hasErrors()) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        Coupon coupon = couponService.getCoupon(request.getCouponId());

        if (coupon == null) {
            return response.build(RespStatus.COUPON_NOT_EXIST);
        }

//        int orderNum = orderService.countCompleteNumByOpenid(wxUser.getOpenid());
//
//        // 用户限制购买数量=1 且   订单数 > 用户限制购买数量
//        if (coupon.getUserLimitState() == 1 && orderNum > coupon.getUserLimitNum()) {
//            return response.build(200, "该红包限定购买记录小于" + coupon.getUserLimitNum() + "的新用户领取");
//        } else if (coupon.getUserLimitState() == 2 && orderNum < coupon.getUserLimitNum()) {
//            return response.build(200, "该红包限定购买记录大于" + coupon.getUserLimitNum() + "的老用户领取");
//        }

        if (coupon.getGmtEndTime().getTime() <= System.currentTimeMillis()) {
            return response.build(RespStatus.COUPON_EXPIRE);
        }

        // 查看用户是否已经领取该优惠券
        if (couponService.isGotCoupon(wxUser.getId(), coupon.getCouponId())) {
            // 你已经抢过这个红包了
            return response.build(RespStatus.HAS_GOT_COUPON);
        }

        if (coupon.getHasGotAmount() >= coupon.getTotalAmount()) {
            // 红包派发完了
            return response.build(RespStatus.COUPON_IS_BLANK);
        }
        if (coupon.getState() == -1) {
            return response.build(RespStatus.COUPON_EXPIRE.getKey(), "券已经下架");
        }
        /**
         * 1.查询券创建人(卖家)信息
         * 2.查询卖家的所有订单
         * 3.筛选出用户在卖家的订单
         */
        WxUser shopkeeper = wxUserService.getWxUserById(coupon.getUserId());

        List<OrderInfo> orderBySellerId = orderService.getOrderBySellerId(shopkeeper.getOpenid());
        Set<OrderInfo> collect = orderBySellerId.stream().filter(orderInfo ->
                // 筛选该用户的订单
                orderInfo.getOpenid().equals(wxUser.getOpenid()) &&
                        // 排除子订单
                        StringUtils.isBlank(orderInfo.getParentOrderNum()) &&
                        // 筛选出已完成,待发货,已发货,待收货的订单
                        (OrderStatusEnum.COMPELETED.getCode().equals(orderInfo.getOrderStatus()) ||
                                OrderStatusEnum.PENDING_DELIVERY.getCode().equals(orderInfo.getOrderStatus()) ||
                                OrderStatusEnum.PENDING_RECEIVED.getCode().equals(orderInfo.getOrderStatus())
                        )).collect(Collectors.toSet());
        if (coupon.getUserLimitState() == 1 && collect.size() > coupon.getUserLimitNum()) {
            // 针对新用户,若新用户下单大于getUserLimitNum()则不能领取
            return response.build(RespStatus.COUPON_FAILED, "该红包限定购买记录小于" + coupon.getUserLimitNum() + "的新用户领取");
        } else if (coupon.getUserLimitState() == 2 && collect.size() < coupon.getUserLimitNum()) {
            // 针对老用户,若老用户下单小于getUserLimitNum()则不能领取
            return response.build(RespStatus.COUPON_FAILED, "该红包限定购买记录大于" + coupon.getUserLimitNum() + "的老用户领取");
        }

        String s = redisCache.get(couponKey + request.getCouponId());
        if (org.apache.commons.lang3.StringUtils.isBlank(s)) {
            //  redis获取不到券数量时
            s = coupon.getTotalAmount().toString();
        }
        Integer couponCount = Integer.valueOf(s);
        if (couponCount <= 0) {
            return response.build(RespStatus.COUPON_IS_BLANK);
        }
        //对用户和红包加锁，防止用户重复点击
        synchronized (this) {
            try {
                if (couponService.receiveCoupon(wxUser.getId(), request.getCouponId())) {
                    // 领取成功券减一
                    redisCache.incr(couponKey + request.getCouponId(), -1);
                } else {
                    return response.build(RespStatus.COUPON_GET_FAILED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("领取红包错误：", e);
                return response.build(RespStatus.SYSTEM_ERROR);
            }
        }
        return response;
    }


    /**
     * 可下单红包列表 (废弃)
     *
     * @param request
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/canOrderCouponList", method = RequestMethod.POST)
    public CanOrderCouponListResponse canOrderCouponList(@RequestBody @Valid CanOrderCouponListRequest
                                                                 request, BindingResult errors) {

        CanOrderCouponListResponse response = new CanOrderCouponListResponse();

        if (errors.hasErrors()) {
            return (CanOrderCouponListResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        try {

            WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

            if (wxUser == null) {
                return (CanOrderCouponListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
            }

            if (StringUtils.isBlank(request.getOpenid()) || CollectionUtils.isEmpty(request.getGoodsNumList())) {
                return (CanOrderCouponListResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
            }

            Map map = new HashMap();

            map.put("openid", request.getOpenid());
            map.put("list", request.getGoodsNumList());
            map.put("uid", wxUser.getId());

            PageHelper.startPage(request.getPage(), request.getPageSize());
            List<CanUseCouponDetail> result = couponService.selectCanUseCoupon(map);

            int canUseNum = couponService.selectCanUseCouponNum(map);

            response.setCanOrderNum(canUseNum);
            response.setCanUseCouponDetailList(result);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("可下单红包列表错误：", e);
            return (CanOrderCouponListResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        return response;
    }

    /**
     * 可下单红包列表
     *
     * @param request
     * @param errors
     * @return
     */
    @PostMapping("/getEffectCoupon")
    public BaseResponse getEffectCoupon(@RequestBody @Valid CanOrderCouponListRequest
                                                request, BindingResult errors) {
        if (errors.hasErrors()) {
            throw new DataException(RespStatus.ILLEGAL_ARGUMENT.getDesc());
        }
        return couponService.getEffectCoupon(request);
    }

    /**
     * 上架下架优惠券(更新优惠券)
     */
    @PostMapping("/updateCoupon")
    public BaseResponse getEffectCoupon(@RequestBody CouponRequest request) {
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(request, coupon);
        if (Integer.valueOf(0).equals(coupon.getState())) {
            // 上架
            if (coupon.getGmtEndTime().before(new Date())) {
                return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
            }
        }
        int update = couponService.update(coupon);
        if (update > 0) {
            return new BaseResponse().build(RespStatus.SUCCESS);
        }
        return new BaseResponse().build(RespStatus.OPERATION_FAILURE);
    }
}
