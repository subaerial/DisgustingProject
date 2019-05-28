package com.mhc.yunxian.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.coupon.MyCouponDetail;
import com.mhc.yunxian.bean.coupon.MyGotCouponDetail;
import com.mhc.yunxian.bean.request.param.ShopInfoRequestParam;
import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.CouponDao;
import com.mhc.yunxian.dao.CouponUserDao;
import com.mhc.yunxian.dao.DragonAddrDao;
import com.mhc.yunxian.dao.model.AttentionShop;
import com.mhc.yunxian.dao.model.OrderInfo;
import com.mhc.yunxian.dao.model.Shop;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.enums.OrderStatusEnum;
import com.mhc.yunxian.enums.ShopEnum;
import com.mhc.yunxian.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/12/6.
 *
 * @author Alin
 */
@Controller
@RequestMapping("/shop")
@Slf4j
@Api(value = "ShopController", description = "卖家店铺相关")
public class ShopController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private WxUserService userService;
    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private DragonAddrDao dragonAddrDao;
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private AttentionShopService attentionShopService;

    @Autowired
    private CouponUserDao couponUserDao;

    private static final int REMARK_LENGTH=140;

    /**
     * 新增店铺信息
     */
    @ResponseBody
    @ApiOperation(value = "/addShopInfo", notes = "新增店铺信息")
    @RequestMapping(value = "/addShopInfo", method = RequestMethod.POST)
    public BaseResponse addShopInfo(@RequestBody ShopInfoRequestParam param) {
        BaseResponse responseParam = new BaseResponse();
        Boolean flag = shopService.addShopInfo(param);
        Map<String, Object> map = new HashMap<>();
        map.put("success", flag);
        responseParam.setMap(map);
        return responseParam;
    }

    /**
     * 编辑店铺信息
     */
    @ResponseBody
    @ApiOperation(value = "/updateShopInfo", notes = "编辑店铺信息")
    @RequestMapping(value = "/updateShopInfo", method = RequestMethod.POST)
    public BaseResponse updateShopInfo(@RequestBody ShopInfoRequestParam param) {
        BaseResponse responseParam = new BaseResponse();
        if(StringUtils.isNotBlank(param.getShopIntro()) && REMARK_LENGTH < param.getShopIntro().length()){
            return responseParam.build(RespStatus.SHOP_SHOPINTRO_LIMIT);
        }
        Boolean flag = shopService.updateShopInfo(param);
        Map<String, Object> map = new HashMap<>();
        map.put("success", flag);
        responseParam.setMap(map);
        return responseParam;
    }

    /***
     * 查询店铺信息(卖家 老接口/getMyBalanceAndOrderCount)
     * 店家专用
     */
    @ResponseBody
    @ApiOperation(value = "/getShopInfo", notes = "查询店铺信息")
    @RequestMapping(value = "/getShopInfo", method = RequestMethod.POST)
    public BaseResponse getShopInfo(@RequestBody ShopInfoQuery query) {
        BaseResponse baseResponse = new BaseResponse();
        WxUser wxUser = userService.getUserBySessionId(query.getSessionId());
        if (null == wxUser) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        query.setShopkeeperOpenId(wxUser.getOpenid());
        Shop shop = shopService.statisticsShopInfo(query);
        Map<String, Object> hashMap = new HashMap<>();
        // 默认店家已关注自己的店铺
        hashMap.put("isAttention", 0);
        hashMap.put("shopInfo", shop);
        hashMap.put("isWhite", wxUser.getIsWhite());
        hashMap.put("dragonButIsOpen", wxUser.getDragonButIsOpen());
        // 子查询获取店家所有订单
        List<OrderInfo> sellerAllOrders = orderService.getOrderBySellerId(wxUser.getOpenid());
        this.setReturnValues(hashMap, sellerAllOrders);
        // 查询商家创建的未失效的优惠券
        List<MyCouponDetail> myCouponList = couponDao.getMyCouponList(wxUser.getId());
        List<MyCouponDetail> collect = myCouponList.stream().filter(myCouponDetail -> myCouponDetail.getGmtEndTime().after(new Date())
                && Integer.valueOf(0).equals(myCouponDetail.getState()))
                .collect(Collectors.toList());
        hashMap.put("coupon", collect.size());
        hashMap.put("couponList", collect);
        hashMap.put("address", dragonAddrDao.selectAllAddrByOpenid(wxUser.getOpenid()).size());
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    /***
     * 查询店铺信息
     * 用户专用
     */
    @ResponseBody
    @ApiOperation(value = "/browseShopInfo", notes = "查询店铺信息")
    @RequestMapping(value = "/browseShopInfo", method = RequestMethod.POST)
    public BaseResponse browseShopInfo(@RequestBody ShopInfoQuery query) {
        BaseResponse baseResponse = new BaseResponse();
        WxUser wxUser = userService.getUserBySessionId(query.getSessionId());
        if (null == wxUser) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (null == query.getShopId()) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        Shop shop = shopService.statisticsShopInfo(query);
        if (null == shop) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        Map<String, Object> hashMap = new HashMap<>();
        // 默认未关注
        Integer isAttenion = ShopEnum.CANCEL_ATTENTION.getCode();
        ShopInfoQuery shopInfoQuery = new ShopInfoQuery();
        shopInfoQuery.setUserId(Long.valueOf(wxUser.getId()));
        shopInfoQuery.setShopId(shop.getShopId());
        List<AttentionShop> attentionShops = attentionShopService.queryAttentionShopList(shopInfoQuery);
        if (CollectionUtils.isNotEmpty(attentionShops)) {
            AttentionShop attentionShop = attentionShops.get(0);
            isAttenion = attentionShop.getStatus();
        }
        hashMap.put("isAttention", isAttenion);
        hashMap.put("shopInfo", shop);
        List<OrderInfo> userAllOrders = orderService.getOrderByOpenid(wxUser.getOpenid());
        // 设置返回的订单相关参数
        this.setReturnValues(hashMap, userAllOrders);
        // 设置用户的券和地址
        this.setUserCouponAndAddress(wxUser, hashMap);
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    private void setUserCouponAndAddress(WxUser wxUser, Map<String, Object> hashMap) {
        // 查询用户已领取的券未失效未使用的
        List<MyGotCouponDetail> myGotCouponDetails = couponUserDao.myGotCouponList(wxUser.getId());
        List<MyGotCouponDetail> collect = myGotCouponDetails.stream().filter(myCouponDetail ->
                        myCouponDetail.getGmtEndTime().after(new Date())
                ).collect(Collectors.toList());
        hashMap.put("coupon", collect.size());
        hashMap.put("couponList", collect);
        hashMap.put("address", dragonAddrDao.selectAllAddrByOpenid(wxUser.getOpenid()).size());
    }

    /**
     * 用户个人中心
     */
    @ResponseBody
    @ApiOperation(value = "/userCenter", notes = "查询店铺信息")
    @RequestMapping(value = "/userCenter", method = RequestMethod.POST)
    public BaseResponse userCenter(@RequestBody BaseQuery query) {
        BaseResponse baseResponse = new BaseResponse();
        WxUser wxUser = userService.getUserBySessionId(query.getSessionId());
        if (null == wxUser) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        Map<String, Object> hashMap = new HashMap<>();
        List<OrderInfo> userAllOrders = orderService.getOrderByOpenid(wxUser.getOpenid());
        this.setReturnValues(hashMap, userAllOrders);
        // 查询用户已领取的券未失效未使用的
        this.setUserCouponAndAddress(wxUser, hashMap);
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    /**
     * @param hashMap   返回的map参数
     * @param allOrders 用户或者商家所有订单
     */
    private void setReturnValues(Map<String, Object> hashMap, List<OrderInfo> allOrders) {
        Map<Integer, List<OrderInfo>> map = allOrders.stream().collect(Collectors.groupingBy(OrderInfo::getOrderStatus));
        List<OrderInfo> unPay = Optional.ofNullable(map.get(OrderStatusEnum.PENDING_PAYMENT.getCode())).orElse(new ArrayList<>());
        // 待付款的需要过滤子订单
        List<OrderInfo> unPayOrders = unPay.stream().filter(orderInfo -> StringUtils.isBlank(orderInfo.getParentOrderNum())).collect(Collectors.toList());
        List<OrderInfo> unShipped = Optional.ofNullable(map.get(OrderStatusEnum.PENDING_DELIVERY.getCode())).orElse(new ArrayList<>());
        List<OrderInfo> shipped = Optional.ofNullable(map.get(OrderStatusEnum.PENDING_RECEIVED.getCode())).orElse(new ArrayList<>());
        List<OrderInfo> finish = Optional.ofNullable(map.get(OrderStatusEnum.COMPELETED.getCode())).orElse(new ArrayList<>());
        List<OrderInfo> refund = Optional.ofNullable(map.get(OrderStatusEnum.REFUNDING.getCode())).orElse(new ArrayList<>());
        List<OrderInfo> refundOrders = refund.parallelStream().filter(orderInfo -> StringUtils.isBlank(orderInfo.getParentOrderNum())).collect(Collectors.toList());
        hashMap.put("unpay", unPayOrders.size());
        hashMap.put("unshipped", unShipped.size());
        hashMap.put("shipped", shipped.size());
        hashMap.put("finish", finish.size());
        hashMap.put("refund", refundOrders.size());
    }

    /***
     * 统计月成交订单量
     * 本月销售额
     * 复购口碑
     * 已提现金额
     * 余额(可提现)
     * 暂不用
     */
    @Deprecated
    @ResponseBody
    @ApiOperation(value = "/statisticsShopInfo", notes = "店铺统计信息")
    @RequestMapping(value = "/statisticsShopInfo", method = RequestMethod.POST)
    public BaseResponse statisticsShopInfo(@RequestBody ShopInfoQuery query) {
        BaseResponse baseResponse = new BaseResponse();
        WxUser user = userService.getUserBySessionId(query.getSessionId());
        if (null == user) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (StringUtils.isBlank(query.getShopkeeperOpenId())) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        Shop shop = shopService.statisticsShopInfo(query);
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("shopInfo", shop);
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    /**
     * 我的店铺-进行中的接龙
     */
    @ResponseBody
    @ApiOperation(value = "/dragonInProgress", notes = "我的店铺-进行中的接龙")
    @RequestMapping(value = "/dragonInProgress", method = RequestMethod.POST)
    public BaseResponse dragonInProgress(@RequestBody ShopInfoQuery query) {

        WxUser user = userService.getUserBySessionId(query.getSessionId());
        if (null == user) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (null == query.getShopId()) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        PageHelper.startPage(query.getPageNo(), query.getPageSize());
        BaseResponse baseResponse = shopService.dragonInProgress(query);
        // 填充店铺其他信息
        return baseResponse;
    }

    /**
     * 我的店铺-复购口碑商品
     */
    @ResponseBody
    @ApiOperation(value = "/repurchaseGoods", notes = "我的店铺-复购口碑商品")
    @RequestMapping(value = "/repurchaseGoods", method = RequestMethod.POST)
    public BaseResponse repurchaseGoods(@RequestBody ShopInfoQuery query) {
        if (null == query.getShopId()) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        BaseResponse baseResponse = new BaseResponse();
        List<GoodsInfoResponse> goodsInfoResponses = shopService.countGoodsRepurchase(query);
        List<GoodsInfoResponse> collect = goodsInfoResponses.stream().sorted(Comparator.comparing(GoodsInfoResponse::getRepurchase).reversed()).collect(Collectors.toList());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("goodsInfoResponses", collect);
        baseResponse.setMap(hashMap);
        // 填充店铺其他信息
        return baseResponse;
    }

}
