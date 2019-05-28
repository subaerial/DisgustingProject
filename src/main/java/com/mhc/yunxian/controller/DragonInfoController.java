package com.mhc.yunxian.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.index.DragonInfoVO;
import com.mhc.yunxian.bean.index.GetIndexDragonListRequest;
import com.mhc.yunxian.bean.index.GetIndexDragonListResponse;
import com.mhc.yunxian.bean.request.param.BaseRequestParam;
import com.mhc.yunxian.bean.request.param.DragonDetailRequestParam;
import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.AttentionShopVO;
import com.mhc.yunxian.bean.response.BrowsedDragonResponse;
import com.mhc.yunxian.bean.response.DragonDetailRepurchaseVO;
import com.mhc.yunxian.cache.JedisProducer;
import com.mhc.yunxian.cache.LockPool;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.*;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.dao.query.BrowseRecordQuery;
import com.mhc.yunxian.dao.query.DragonInfoQuery;
import com.mhc.yunxian.enums.*;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.DateUtils;
import com.mhc.yunxian.utils.JsonUtils;
import com.mhc.yunxian.utils.KeyTool;
import com.mhc.yunxian.utils.OSSClientUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author
 */

@RestController
@RequestMapping("/yunxian")
@Api
@Slf4j
public class DragonInfoController {


    @Autowired
    WxUserService wxUserService;

    @Autowired
    DragonService dragonService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsInfoService goodsService;

    @Autowired
    OrderGoodsService orderGoodsService;

    @Autowired
    PartyInfoService partyInfoService;

    @Autowired
    DragonGoodsService dragonGoodsService;

    @Autowired
    CommentService commentService;

    @Autowired
    SendAddrService sendAddrService;

    @Autowired
    TaskService taskService;

    @Autowired
    MyUserService myUserService;

    @Autowired
    OSSClientUtil ossClientUtil;

    @Autowired
    BalanceService balanceService;

    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private BrowseRecordDao browseRecordDao;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private DragonAddrDao dragonAddrDao;
    @Autowired
    private GoodsInfoDao goodsInfoDao;
    @Autowired
    private AttentionShopMapper attentionShopMapper;
    @Autowired
    private OrderInfoDao orderInfoDao;
    @Autowired
    private OrderGoodsDao orderGoodsDao;

    @Autowired
    private Environment environment;

    @Autowired
    private DragonShowService dragonShowService;

    @Autowired
    private GoodsInfoService goodsInfoService;

    @Autowired
    private MsgRecordService msgRecordService;

    @Autowired
    private JedisProducer jedisProducer;

    @Autowired
    private RemoteCache redisCache;

    private static final String reg = "^[1]+\\d{10}$";
    private static final Pattern pattern = Pattern.compile(reg);

    private static final Integer NAME_LENGTH = 20;
    private static final Integer TITLE_LENGTH = 24;
    private static final Integer REMARK_LENGTH = 200;
    private static final Integer DRAGON_IMAGE_MIX = 2;
    private static final Integer DRAGON_IMAGE_MAX = 9;

    private static final Integer ORDER_BY_REPURCHASE_COUNT = 3;
    /**
     * 截单时间为0，发货时间为24
     */
    private static final Integer MAX_TIME = 24;
    /**
     * 截单时间为48，发货时间为1
     */
    private static final Integer MIN_TIME = -47;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 获取全部接龙信息V7
     * 此接口从2.0开始已废弃
     *
     * @return
     */
    @RequestMapping(value = "/v7/getAllDragonInfo", method = RequestMethod.POST)
    @Deprecated
    public GetIndexDragonListResponse getAllDragonInfoV7(@RequestBody GetIndexDragonListRequest request) {
        return dragonService.getAllDragonInfoV7(request);
    }

    /**
     * 获取轮播图片
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/getBroadcastImg", method = RequestMethod.GET)
    public BroadcastResponse getBroadcastImg() {
        BroadcastResponse broadcastResponse = new BroadcastResponse();
        List<Banner> list = dragonService.getBroadcastImg();
        broadcastResponse.setBannerList(list);
        return broadcastResponse;
    }

    /**
     * 获取接龙号
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getDragonNum", method = RequestMethod.POST)
    public ReleaseDragonResponse releaseDragon(@RequestBody ReleaseDragonRequest request) {

        final ReleaseDragonResponse response = new ReleaseDragonResponse();

        //生成接龙号
        final String dragonNum = "D" + KeyTool.createOrderNo();
        response.setDragonNum(dragonNum);

        final WxUser user = wxUserService.getUserBySessionId(request.getSessionId());
        if (null == user) {
            return (ReleaseDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        final List<DragonInfo> list = dragonService.getDragonByOpenid(user.getOpenid());
        if (null == list || list.size() == 0) {
            response.setPhone("");
        } else {
            response.setPhone(list.get(0).getPhone());
        }
        return response;
    }

    /**
     * 发布子接龙V4
     *
     * @param dragonRequest
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/v4/addSubDragon", method = RequestMethod.POST)
    public CreateDragonResponse createSubDragonV(@RequestBody CreateDragonRequest dragonRequest, HttpServletRequest httpServletRequest) throws Exception {

        final CreateDragonResponse response = new CreateDragonResponse();


        //判断该接龙是否已存在
        final DragonInfo dragonInfo = dragonService.getDragon(dragonRequest.getDragonNum());
        if (dragonInfo != null) {
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_IS_EXIST);
        }

        if (dragonRequest.getData() == null || dragonRequest.getData().size() < 1) {
            return (CreateDragonResponse) response.build(RespStatus.GOODS_NOT_NULL);
        }

        for (Goods goods : dragonRequest.getData()) {
            if (goods.getPrice() == null || goods.getPrice() == 0) {
                return (CreateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
            }
        }

        //先根据sessionId查询用户openid
        WxUser wxUser = wxUserService.getUserBySessionId(dragonRequest.getSessionId());
        if (wxUser == null) {
            return (CreateDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        //保存用户手机号
        if (wxUser.getPhone().equals("0")) {
            wxUser.setPhone(dragonRequest.getPhone());
            wxUserService.updateUserByOpenid(wxUser);
        }

        //判断用户是否登陆
        if (wxUser.getUpdateTime() == null) {
            return (CreateDragonResponse) response.build(RespStatus.YOU_NOT_LOGIN);
        }


        dragonRequest.setOpenid(wxUser.getOpenid());

        try {
            dragonService.addInfoV3(dragonRequest);
            DragonInfo parentDragonInfo = dragonService.getDragon(dragonRequest.getParentDragonNum());
            parentDragonInfo.setSubDragonNum(dragonRequest.getDragonNum());
            dragonService.updateDragonStatus(parentDragonInfo);//此方法更新接龙信息
        } catch (Exception e) {
            e.printStackTrace();
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_ADD_ERROR);
        }

        //发送模版消息标记
        taskService.noticeOfDragonToSeller(dragonRequest, wxUser);

        //todo 保存是否可用电子卷字段

        return response;
    }

    /**
     * 发布接龙V4
     * 2018-12-06 改动
     * 1.发货时间long改为String接收
     * 2.新增接龙视频字段dragonVideo
     * 3.新增截单时间cutOffTime
     * 注意:
     * 一次性发货时,接龙结束时间为截单时间
     *
     * @param dragonRequest
     * @return
     */
    @ApiOperation(value = "/addDragon", notes = "发布接龙V4")
    @RequestMapping(value = "/v4/addDragon", method = RequestMethod.POST)
    public CreateDragonResponse createDragonV4(@RequestBody @Valid CreateDragonRequest dragonRequest, BindingResult errors) throws Exception {

        //分布式锁
        Jedis jedis = jedisProducer.getJedis();
        String requestId = UUID.randomUUID().toString();
        boolean lock = LockPool.tryGetDistributeLock(jedis, dragonRequest.getSessionId(), requestId, 5000);

        final CreateDragonResponse response = new CreateDragonResponse();

        if (errors.hasErrors()) {
            response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        if (!lock) {
            return (CreateDragonResponse) response.build(505, "请不要重复提交");
        }
        //判断该接龙是否已存在
        final DragonInfo dragonInfo = dragonService.getDragon(dragonRequest.getDragonNum());
        if (dragonInfo != null) {
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_IS_EXIST);
        }

        if (dragonRequest.getData() == null || dragonRequest.getData().size() < 1) {
            return (CreateDragonResponse) response.build(RespStatus.GOODS_NOT_NULL);
        }

        for (Goods goods : dragonRequest.getData()) {
            if (goods.getPrice() == null || goods.getPrice() == 0) {
                return (CreateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
            }
        }

        //先根据sessionId查询用户openid
        WxUser wxUser = wxUserService.getUserBySessionId(dragonRequest.getSessionId());
        if (wxUser == null) {
            return (CreateDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        //保存用户手机号
        if (String.valueOf(0).equals(wxUser.getPhone())) {
            wxUser.setPhone(dragonRequest.getPhone());
            wxUserService.updateUserByOpenid(wxUser);
        }

        //判断用户是否登陆
        if (wxUser.getUpdateTime() == null) {
            return (CreateDragonResponse) response.build(RespStatus.YOU_NOT_LOGIN);
        }
        /**
         * 配送方式包含自提的必须传递自提地址id
         */
        if (verifyAddress(dragonRequest.getDragonAddrIds(), dragonRequest.getIsDelivery())) {
            return (CreateDragonResponse) response.build(RespStatus.ADDR_IS_NULL);
        }
        dragonRequest.setOpenid(wxUser.getOpenid());
        DeliveryCycle deliveryCycle = dragonRequest.getDeliveryCycle();
        if (deliveryCycle != null && !DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(deliveryCycle.getCycleType())) {
            dragonRequest.setEndTime(null);
        }
        /**
         * 参数校验
         */
        if (StringUtils.isNotBlank(dragonRequest.getTitle()) && TITLE_LENGTH < dragonRequest.getTitle().length()) {
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_TITLE_LIMIT);
        }
        if (StringUtils.isBlank(dragonRequest.getDragonImage())) {
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_IMAGE_LIMIT);
        }
        String[] split = dragonRequest.getDragonImage().split(",");
        if (split.length > DRAGON_IMAGE_MAX || split.length < DRAGON_IMAGE_MIX) {
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_IMAGE_LIMIT);
        }

        Matcher matcher = pattern.matcher(dragonRequest.getPhone());
        if (!matcher.matches()) {
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_PHONE_LIMIT);
        }
        if (DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(dragonRequest.getDeliveryCycle().getCycleType())) {
            //一次性发货
            if (null == dragonRequest.getEndTime()) {
                return (CreateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
            }
            Long endTime = dragonRequest.getEndTime();
            Long nowTime = System.currentTimeMillis();
            //结束时间不能早于当前时间
            if (endTime < nowTime) {
                return (CreateDragonResponse) response.build(RespStatus.END_TIME_LIMIT);
            }
        } else {
            //周期性发货
            if (MIN_TIME > dragonRequest.getDeliveryCycle().getCutHour() || dragonRequest.getDeliveryCycle().getCutHour() > MAX_TIME) {
                return (CreateDragonResponse) response.build(RespStatus.CUT_TIME_LIMIT);
            }
        }
        try {
            dragonService.addInfoV3(dragonRequest);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return (CreateDragonResponse) response.build(RespStatus.DRAGON_ADD_ERROR);
        }
        //释放锁
        LockPool.releaseDistributeLock(jedis, dragonRequest.getSessionId(), requestId);

        //发送模版消息标记
        taskService.noticeOfDragonToSeller(dragonRequest, wxUser);

        return response;
    }


    /**
     * 提前结束接龙
     *
     * @param dragonRequest
     * @return
     */
    @RequestMapping(value = "/endInAdvance", method = RequestMethod.POST)
    public UpdateDragonResponse endInAdvance(@RequestBody UpdateDragonRequest dragonRequest, HttpServletRequest httpServletRequest) {

        final UpdateDragonResponse response = new UpdateDragonResponse();
        //先根据sessionId查询用户openid
        WxUser wxUser = wxUserService.getUserBySessionId(dragonRequest.getSessionId());
        if (wxUser == null) {
            return (UpdateDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(dragonRequest.getDragonNum())) {
            return (UpdateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        dragonRequest.setOpenid(wxUser.getOpenid());

        if (!dragonService.updateDragonEndTime(dragonRequest)) {
            return (UpdateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }

        return (UpdateDragonResponse) response.build(200, "ok");
    }

    /**
     * 接龙上架或下架
     *
     * @param dragonNum
     * @param sessionId
     * @param state
     * @return
     */
    @RequestMapping(value = "/v20/updownDragon", method = RequestMethod.POST)
    public BaseResponse updown(@RequestParam(value = "dragonNum", required = false) String dragonNum,
                               @RequestParam(value = "sessionId", required = false) String sessionId,
                               @RequestParam(value = "dragonStatus", required = false) Integer state) {
        return dragonService.updownDragon(dragonNum, sessionId, state);
    }

    /**
     * 发布评论
     *
     * @param sendCommentRequest
     * @return
     */
    @RequestMapping(value = "/sendComment", method = RequestMethod.POST)
    public SendCommentResponse sendComment(@RequestBody SendCommentRequest sendCommentRequest, HttpServletRequest httpServletRequest) {

        final SendCommentResponse response = new SendCommentResponse();
        //先根据sessionId查询用户openid
        WxUser wxUser = wxUserService.getUserBySessionId(sendCommentRequest.getSessionId());
        if (wxUser == null) {
            return (SendCommentResponse) response.build(RespStatus.USER_NOT_EXIST);
        }
        sendCommentRequest.setOpenid(wxUser.getOpenid());

        if (sendCommentRequest.getComment().getBytes().length > 256) {
            return (SendCommentResponse) response.build(RespStatus.COMMENT_LENGTH_TOLONG);
        }

        if (!commentService.sendComment(sendCommentRequest)) {
            return (SendCommentResponse) response.build(RespStatus.COMMENT_ADD_FAIL);
        }
        response.build(200, "ok");
        return response;
    }


    /**
     * 接龙详情-接龙记录
     *
     * @return request
     */
    @RequestMapping(value = "/dragonHistory", method = RequestMethod.POST)
    public DragonHistoryResponse dragonHistory(@RequestBody DragonHistoryRequest request) {

        DragonHistoryResponse response = new DragonHistoryResponse();

        try {
            response = orderService.DragonHistory(request);
        } catch (Exception e) {
            e.printStackTrace();
            return (DragonHistoryResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 查询接龙信息V4
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/v4/findDragon", method = RequestMethod.POST)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public FindDragonResponse findDragonV4(@RequestBody FindDragonRequest request) {
        final FindDragonResponse response = new FindDragonResponse();
        // final List<FindDragon> findDragonList = Lists.newArrayList();
        final List<GoodsInfo> goodsInfoList = Lists.newArrayList();
        final List<CommentInfo> comments = Lists.newArrayList();
        WxUser looker = new WxUser();
        if (request.getSessionId() != null) {
            looker = wxUserService.getUserBySessionId(request.getSessionId());
            if (null == looker) {
                return (FindDragonResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
            }
        }
        //查询接龙信息
        DragonInfo dragonInfo = dragonService.getDragon(request.getDragonNum());
        if (null == dragonInfo) {
            log.error("接龙不存在!接龙编号{}", request.getDragonNum());
            return (FindDragonResponse) response.build(RespStatus.DRAGON_NOT_EXIST);
        }
        //查询商家信息
        WxUser wxUser = wxUserService.getWxUser(dragonInfo.getOpenid());
        if (null == wxUser) {
            log.error("未查询到商家信息!商家openId{}", dragonInfo.getOpenid());
            return (FindDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        String port = environment.getProperty("server.port");
        /**
         * 是否显示分享按钮：
         * 0：显示
         * 1：不显示
         */
        Integer show = 0;
        //Map<String, List<BrowseRecord>> map = Maps.newHashMap();
        if (port.equals("8080")) {
            //product版
            show = dragonShowService.selectBtnShow(VersionEnum.PRODUCT.getCode());
        }
        if (port.equals("8889")) {
            //release版
            show = dragonShowService.selectBtnShow(VersionEnum.RELEASE.getCode());
        }

        response.setIsShowBtn(show);

        //查询接龙中所有商品信息
        final List<DragonGoods> dragonGoodsList = dragonGoodsService.getGoodsNUM(request.getDragonNum());
        if (null == dragonGoodsList || dragonGoodsList.size() == 0) {
            log.error("查询接龙中所有商品信息失败!接龙编号dragonNum{}", request.getDragonNum());
            //return (FindDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        //复购次数
        int repurchaseNum;
        for (DragonGoods dragonGoods : dragonGoodsList) {
            //查询商品信息
            final GoodsInfo goodsInfo = goodsService.getGoods(dragonGoods.getGoodsNum());
            if (null == goodsInfo) {
                return (FindDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
            }
            Boolean deleted = goodsInfo.getDeleted();
            if (deleted) {
                log.error("商品已被删除,商品信息{}", JsonUtils.toJson(goodsInfo));
                continue;
            }
            goodsInfo.setLimitBuyNum(dragonGoods.getLimitBuyNum());
            goodsInfo.setGoodsName(dragonGoods.getGoodsName());
            //初始化复购次数为0
            repurchaseNum = 0;
            List<OrderGoodsInfo> orderGoodsInfos = orderGoodsDao.selectOrderGoodsByGoodsNum(dragonGoods.getGoodsNum());
            if (CollectionUtils.isNotEmpty(orderGoodsInfos)) {
                //去除取消或者退订的订单
                List<OrderGoodsInfo> collect = orderGoodsInfos.parallelStream().filter(orderGoodsInfo -> {
                    OrderInfo orderInfo = orderInfoDao.selectOrderByOrderNum(orderGoodsInfo.getOrderNum());
                    boolean b = true;
                    if (orderInfo != null) {
                        b = !(OrderStatusEnum.CANCELLED.getCode().equals(orderInfo.getOrderStatus())
                                || OrderStatusEnum.REFUNDED.getCode().equals(orderInfo.getOrderStatus()));
                    }
                    return b;
                }).collect(Collectors.toList());
                Map<String, List<OrderGoodsInfo>> collectByOpenId = collect.stream().collect(Collectors.groupingBy(OrderGoodsInfo::getOpenid));
                Iterator<String> orderGoodsIter = collectByOpenId.keySet().iterator();
                while (orderGoodsIter.hasNext()) {
                    String openid = orderGoodsIter.next();
                    List<OrderGoodsInfo> orderGoods = collectByOpenId.get(openid);
                    repurchaseNum += (orderGoods.size() - 1);
                    goodsInfo.setRepurchaseNum(repurchaseNum);

                    if (openid.equals(looker.getOpenid())) {

                        List<OrderGoodsInfo> collect1 = orderGoods.stream().filter(orderGoodsInfo -> {
                            OrderInfo orderByOrderNum = orderService.getOrderByOrderNum(orderGoodsInfo.getOrderNum());
                            if (orderByOrderNum != null && orderByOrderNum.getDragonNum().equals(request.getDragonNum())) {
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }).collect(Collectors.toList());

                        goodsInfo.setBuyNum(collect1.size());
                    }

                }
            }
            goodsInfoList.add(goodsInfo);
          /*  if (repurchaseNum != 0) {
                goodsInfoList.add(goodsInfo);
            }*/

        }
        final List<Comment> commentList = commentService.getComment(request.getDragonNum());
        if (null == commentList) {
            return (FindDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        for (Comment comment : commentList) {
            final CommentInfo commentInfo = new CommentInfo();
            WxUser wu = wxUserService.getWxUser(comment.getOpenid());
            commentInfo.setHeadImg(wu.getHeadImgUrl());
            commentInfo.setNickName(wu.getNickName());
            commentInfo.setComment(comment.getComment());
            comments.add(commentInfo);
        }
        //添加接龙浏览记录
        if (request.getSessionId() != null) {
            BrowseRecord browseRecord = new BrowseRecord();
            browseRecord.setDragonNum(request.getDragonNum());
            browseRecord.setUserId(looker.getId());
            List<BrowseRecord> list = dragonService.findTodayRecord(browseRecord);

            if (list.size() < 1) {
                browseRecord = new BrowseRecord();
                browseRecord.setDragonNum(request.getDragonNum());
                browseRecord.setUserId(looker.getId());
                browseRecord.setCreateTime(new Date());
                browseRecord.setSellerOpenid(dragonInfo.getOpenid());
                dragonService.addDragonBrowseRecord(browseRecord);
            } else {
                browseRecord = list.get(list.size() - 1);
                browseRecord.setCreateTime(new Date());
                dragonService.updateBrowseRecord(browseRecord);
            }
            //判断用户限购数量
            if (dragonInfo.getGlobalLimit() != 0) {
                int limitNum = orderGoodsService.selectLimitNumber(looker.getOpenid(), dragonInfo.getDragonNum());
                response.setUserLimit(limitNum);
            }
        }
        DragonInfo dragon = new DragonInfo();
        dragon.setOpenid(dragonInfo.getOpenid());
        dragon.setDragonStatus(DragonStatusEnum.IN_PROGRESS.getStatus());
        List<DragonInfo> dragonInfos = dragonService.selectDragonByStatusAndOpenid(dragon);
        ShopInfoQuery query = new ShopInfoQuery();
        query.setShopkeeperOpenId(dragonInfo.getOpenid());
        List<Shop> shops = shopMapper.queryShopInfo(query);
        if (CollectionUtils.isNotEmpty(shops)) {
            Shop shop = shops.get(0);
            response.setAddress(shop.getShowAddr());
            // TODO 缓存改造
            Long integer = statisticsService.getShopRepurchaseNum(dragonInfo.getOpenid());
            response.setRepurchase(integer.intValue());
            response.setShopName(shop.getShopName());
            response.setShopId(shop.getShopId());
        }
        response.setDragonInProcessCount(dragonInfos.size());
        //商家头像，昵称
        response.setHeadImg(wxUser.getHeadImgUrl());
        response.setNickName(wxUser.getNickName());
        response.setOpenid(wxUser.getOpenid());
        response.setOrderNumber(wxUser.getOrderNumber());
        //接龙标题，描述，图片，自提地点，联系方式，接龙人数，截至时间，接龙创建时间
        response.setDragonTitle(dragonInfo.getDragonTitle());
        response.setDragonRemark(dragonInfo.getDragonDesc());
        response.setDragonImgs(dragonInfo.getDragonImg());
        response.setIsDelivery(dragonInfo.getIsDelivery());
        response.setGlobalLimit(dragonInfo.getGlobalLimit());

        List<DragonAddr> dragonAddrList = dragonService.selectByDragonNum(dragonInfo.getDragonNum());
        if (dragonAddrList.size() < 1 && dragonInfo.getIsDelivery() != 1 && CollectionUtils.isNotEmpty(dragonAddrList)) {
            List<String> addrs = Arrays.asList(dragonInfo.getAddr().split("&"));
            for (String addr : addrs) {
                DragonAddr dragonAddr = new DragonAddr();
                dragonAddr.setAddr(addr);
                dragonAddrList.add(dragonAddr);
            }
        }

  /*      List<FindDragon> findDragonsUserInfo = Lists.newArrayList();
        for (FindDragon findDragon : findDragonList) {
            if (!findDragonsUserInfo.contains(findDragon)) {
                findDragonsUserInfo.add(findDragon);
            }
        }*/

        List<FindDragon> partDragonUser = dragonService.getPartDragonUser(request.getDragonNum(), null);

        response.setDragonAddr(dragonAddrList);
        response.setPhone(dragonInfo.getPhone());
        response.setPartyNumber(partDragonUser.size());
        if (StringUtils.isNotBlank(dragonInfo.getEndTime())) {
            response.setEndTime(DateUtils.timeToTimeStamp(dragonInfo.getEndTime()));
        }

        response.setCreateTime(dragonInfo.getCreateTime());
        //参与人头像，参与者购买纪录
        response.setData(partDragonUser);
        //商品信息：标题，图片，价格，总量
        response.setGoodsList(goodsInfoList);
        //送货时间，是否支持货到付款
        response.setIsCOD(dragonInfo.getCashOnDelivery());
        String sendTime = dragonInfo.getSendTime();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(sendTime)) {
            response.setSendTime(DateUtils.timeToTimeStamp(sendTime));
        }
        String cycle = dragonInfo.getDeliveryCycle();
        if (StringUtils.isNotBlank(cycle)) {
            Date date = dragonService.getDragonDeliveryCycleDate(cycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME);
            if (null != date) {
                response.setSendTime(date.getTime());
            }
        }
        response.setIsPayLater(dragonInfo.getIsPayLater());
        //评论内容
        response.setCommentList(comments);
        response.setCommentNumber(commentList.size());
        response.setIsCoupon(dragonInfo.getIsCoupon().intValue());

        return response;
    }


    /**
     * 查询接龙评论
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/findDragonComment", method = RequestMethod.POST)
    public FindDragonResponse findDragonComment(@RequestBody FindDragonRequest request) {

        final FindDragonResponse response = new FindDragonResponse();

        final List<CommentInfo> comments = Lists.newArrayList();

        //查询接龙信息
        DragonInfo dragonInfo = dragonService.getDragon(request.getDragonNum());

        if (null == dragonInfo) {
            return (FindDragonResponse) response.build(RespStatus.DRAGON_NOT_EXIST);
        }


        final List<Comment> commentList = commentService.getComment(request.getDragonNum());
        if (null == commentList) {
            return (FindDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        for (Comment comment : commentList) {
            final CommentInfo commentInfo = new CommentInfo();
            WxUser wu = wxUserService.getWxUser(comment.getOpenid());
            commentInfo.setHeadImg(wu.getHeadImgUrl());
            commentInfo.setNickName(wu.getNickName());
            commentInfo.setComment(comment.getComment());
            comments.add(commentInfo);
        }

        //评论内容
        response.setCommentList(comments);

        return response;
    }


    /**
     * 马上接龙V4
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/v4/receiveDragon", method = RequestMethod.POST)
    public ReceiveDragonResponse receiveDragonV4(@RequestBody ReceiveDragonRequest request) {
        final ReceiveDragonResponse response = new ReceiveDragonResponse();
        //先根据sessionId查询用户openid
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return (ReceiveDragonResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        DragonInfo dragonInfo = dragonService.getDragon(request.getDragonNum());
        if (dragonInfo == null) {
            return (ReceiveDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        if (dragonInfo.getGlobalLimit() != 0) {
            int userLimit = orderGoodsService.selectLimitNumber(wxUser.getOpenid(), request.getDragonNum());
            int buyNumber = 0;
            for (GoodsInfo e : request.getGoodsList()) {
                if (e.getTotalNumber() > 0) {
                    buyNumber++;
                }
            }
            if ((buyNumber + userLimit) > dragonInfo.getGlobalLimit()) {
                return (ReceiveDragonResponse) response.build(RespStatus.MANY_THAN_GLOBAL_LIMIT);
            }
        }
        request.setOpenid(wxUser.getOpenid());
        //生成订单号
        final String orderNum = "O" + KeyTool.createOrderNo();
        //获取该用户最近订单的下单地址
        List<OrderInfo> orderInfos = orderService.getOrderByOpenid(request.getOpenid());
        if (orderInfos.size() > 0) {
            response.setAddr(orderInfos.get(0).getAddress());
            response.setName(orderInfos.get(0).getAddrName());
            response.setPhone(orderInfos.get(0).getAddrPhone());
        }
        if (request.getDragonNum() != null) {
            List<OrderGoodsInfo> goodsInfos = orderGoodsService.selectLimitOrderGood(wxUser.getOpenid(), request.getDragonNum());
            List<String> resultGoodsNum = new ArrayList<>();
            for (GoodsInfo e : request.getGoodsList()) {
                if (e.getTotalNumber() > 0) {
                    int limitBuyNum = 999999;
                    int buyNum = e.getTotalNumber();
                    for (OrderGoodsInfo orderGoodsInfo : goodsInfos) {
                        if (e.getGoodsNum().equals(orderGoodsInfo.getGoodsNum())) {
                            buyNum += orderGoodsInfo.getBuyNumber();
                            limitBuyNum = orderGoodsInfo.getLimitBuyNum();
                        }
                    }
                    if (buyNum > limitBuyNum) {
                        resultGoodsNum.add(e.getGoodsNum());
                    }
                }
            }
            response.setGoodsNumList(resultGoodsNum.toArray(new String[resultGoodsNum.size()]));
        }
        response.setOrderNum(orderNum);
        return response;
    }

    @RequestMapping(value = "/v3/commitOrder", method = RequestMethod.POST)
    public BaseResponse commitOrderV3(@RequestBody CommitOrderRequest request) throws Exception {
        final CommitOrderResponse response = new CommitOrderResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (StringUtils.isBlank(request.getAddrName()) || StringUtils.isBlank(request.getAddress()) ||
                StringUtils.isBlank(request.getAddrPhone())) {
            return response.build(RespStatus.BUYER_INFO);
        }
        DragonInfo dragonInfo = dragonService.getDragon(request.getDragonNum());
        if (dragonInfo == null) {
            return response.build(RespStatus.DRAGON_NOT_EXIST);
        }
        if (dragonInfo.getDragonStatus() == 1) {
            return response.build(RespStatus.DRAGON_IS_END);
        }
        List<GoodsInfo> goodsInfos = request.getData();
        List<DragonGoods> dragonGoods = dragonGoodsService.getGoodsNUM(request.getDragonNum());
        List<OrderGoodsInfo> buyedGoods = orderGoodsService.selectLimitOrderGood(wxUser.getOpenid(), request.getDragonNum());
        List<String> resultGoodsNum = new ArrayList<>();
        for (GoodsInfo goodsInfo : goodsInfos) {
            // 若下单中有已删除的商品直接返回
            if (null == goodsInfo.getGoodsNum()) {
                log.error("商品编号为空,商品信息{}", JsonUtils.toJson(goodsInfo));
                return response.build(RespStatus.GOODS_IS_NOT_EXIST);
            }
            for (DragonGoods dragonGoods1 : dragonGoods) {
                //同商品
                if (dragonGoods1.getGoodsNum().equals(goodsInfo.getGoodsNum())) {
                    //限购
                    if (DragonLimitEnum.GOODS_LIMIT.getStatus().equals(dragonInfo.getGlobalLimit())
                            && dragonGoods1.getLimitBuyNum() != 0) {
                        //大于限购
                        if (goodsInfo.getTotalNumber() > dragonGoods1.getLimitBuyNum()) {
                            return response.build(RespStatus.DRAGON_GOODS_NUM_TO_MANY);
                        }
                    }

                    GoodsInfo goods = goodsService.getGoods(goodsInfo.getGoodsNum());

                    //大于库存
                    if (goodsInfo.getTotalNumber() > goods.getTotalNumber()) {
                        return response.build(RespStatus.GOODS_BUY_NUM_TOO_MANY);
                    }
                }
            }
            if (goodsInfo.getTotalNumber() > 0) {
                int limitBuyNum = 999999;
                int buyNum = goodsInfo.getTotalNumber();
                for (OrderGoodsInfo orderGoodsInfo : buyedGoods) {
                    if (goodsInfo.getGoodsNum().equals(orderGoodsInfo.getGoodsNum())) {
                        buyNum += orderGoodsInfo.getBuyNumber();
                        limitBuyNum = orderGoodsInfo.getLimitBuyNum();
                    }
                }
                if (buyNum > limitBuyNum) {
                    resultGoodsNum.add(goodsInfo.getGoodsNum());
                }
            }
        }
        if (resultGoodsNum.size() > 0) {
            return response.build(RespStatus.DRAGON_GOODS_NUM_TO_MANY);
        }
        try {
            orderService.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("下单失败!错误信息", e.getMessage());
            return response.build(500, "下单失败");
        }

        return response;
    }


    /**
     * 卖家，报名管理，根据订单状态查询该接龙的订单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dragonManage/findOrderByOrderStatus", method = RequestMethod.POST)
    public GetAllOrderResponse getOrder1(@RequestBody GetAllOrderRequest request) {
        GetAllOrderResponse response = new GetAllOrderResponse();
        //根据sessionId查询openid
        WxUser user = wxUserService.getUserBySessionId(request.getSessionId());
        if (null == user) {
            return (GetAllOrderResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        if (Integer.valueOf(1).equals(request.getUrlType()) && null != request.getMsgId()) {
            //如果是从消息中心进入 则把消息改为已读
            msgRecordService.updateReadStatus(request.getMsgId());
        }
        //我是卖家
        List<GetAllOrder> getAllOrders = Lists.newArrayList();
        List<OrderInfo> orderInfoList = new ArrayList<>();
        String dragonNum = request.getDragonNum();
        Integer orderStatus = request.getOrderStatus();

        if (StringUtils.isNotBlank(dragonNum)) {
            // 卖家接龙的订单管理: 查询该接龙下的所有订单按订单状态进行
            orderInfoList = orderInfoDao.queryDragonOrdersByOrderStatus(dragonNum, orderStatus);

        } else {
            // 卖家所有订单管理
            // 查询卖家所有接龙,抽取所有接龙编号
            List<String> collect = dragonService.getDragonByOpenid(user.getOpenid()).stream()
                    .map(DragonInfo::getDragonNum).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                List<OrderInfo> orderInfos = orderInfoDao.queryByDragonNums(collect);
                if (CollectionUtils.isNotEmpty(orderInfos) && null != orderStatus) {
                    orderInfoList = orderInfos.stream().filter(orderInfo -> orderInfo.getOrderStatus()
                            .equals(orderStatus)).collect(Collectors.toList());
                }
                if (null == orderStatus) {
                    // 订单状态为空时直接返回根据接龙编号集和查到的订单列表
                    orderInfoList = orderInfos;
                }
            }
        }
        // 待付款订单需要过滤子订单
        if (orderStatus == null || OrderStatusEnum.PENDING_PAYMENT.getCode().equals(orderStatus)
                || OrderStatusEnum.REFUNDING.getCode().equals(orderStatus)) {
            orderInfoList = orderInfoList.stream().filter(orderInfo -> org.apache.commons.lang3.StringUtils.isBlank(orderInfo.getParentOrderNum()))
                    .collect(Collectors.toList());
        }

        //开始遍历订单列表
        for (OrderInfo orderInfo : orderInfoList) {
            //查询买家信息
            WxUser wxUser = wxUserService.getWxUser(orderInfo.getOpenid());
            if (null == wxUser) {
                return (GetAllOrderResponse) response.build(200, "没有查询到对应信息");
            }
            //查询订单中购买的商品
            log.debug("orderInfo.getOrderNum()--->" + orderInfo.getOrderNum());
            List<OrderGoodsInfo> orderGoodsInfos = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
            if (null == orderGoodsInfos) {
                return (GetAllOrderResponse) response.build(200, "没有查询到对应信息");
            }
            List<Goods> goodsList = Lists.newArrayList();
            int realMoney = 0;
            for (OrderGoodsInfo ogInfo : orderGoodsInfos) {
                if (ogInfo.getBuyNumber() == 0) {
                    continue;
                }
                final Goods goods = new Goods();
                goods.setGoodsNum(ogInfo.getGoodsNum());
                goods.setGoodsName(ogInfo.getGoodsName());
                goods.setTotalNumber(ogInfo.getBuyNumber());
                goods.setGoodsImgs(ogInfo.getGoodsImg());
                goods.setPrice(ogInfo.getPrice());
                goods.setRealPrice(ogInfo.getRealPrice());
                realMoney += ogInfo.getBuyNumber() * ogInfo.getPrice();
                goodsList.add(goods);
            }
            final GetAllOrder getAllOrder = new GetAllOrder();
            getAllOrder.setHeadImg(wxUser.getHeadImgUrl());
            getAllOrder.setNickName(wxUser.getNickName());
            getAllOrder.setSellerPhone(wxUser.getPhone());
            getAllOrder.setOrderNum(orderInfo.getOrderNum());
            getAllOrder.setCreateTime(orderInfo.getCreateTime().getTime() + "");
            getAllOrder.setGoodsList(goodsList);
            getAllOrder.setGoodsType(orderGoodsInfos.size());
            getAllOrder.setTotalMoney(realMoney);
            getAllOrder.setRealMoney(orderInfo.getOrderMoney());
            getAllOrder.setOrderStatus(orderInfo.getOrderStatus());
            getAllOrder.setAddr(orderInfo.getAddress());
            getAllOrder.setPhone(orderInfo.getAddrPhone());
            getAllOrder.setUserName(orderInfo.getAddrName());
            getAllOrder.setRemark(orderInfo.getOrderRemark());
            getAllOrder.setBuyOpenid(orderInfo.getOpenid());
            getAllOrder.setIsCOD(orderInfo.getIsCod());
            getAllOrder.setIsPayLater(orderInfo.getIsPayLater());
            getAllOrder.setDragonAddr(orderInfo.getDragonAddr());
            getAllOrder.setDragonNum(orderInfo.getDragonNum());
            getAllOrder.setSellerDesc(orderInfo.getSellerDesc());
            DragonInfo dragon = dragonService.getDragon(orderInfo.getDragonNum());
            if (null != dragon) {
                getAllOrder.setDragonTitle(dragon.getDragonTitle());
            }
            //判断该笔交易是否使用优惠卷
            if (orderInfo.getCouponUserId() != null) {
                getAllOrder.setIsUsed(1);
                getAllOrder.setCouponMoney(orderInfo.getCouponAmount());
            } else {
                getAllOrder.setIsUsed(0);
            }
            if (orderInfo.getParentOrderNum() != null) {
                getAllOrder.setIsDifOrder(1);
            }
            OrderInfo orderInfo1 = orderService.getDifOrderByParentOrderNum(orderInfo.getOrderNum());
            if (null != orderInfo1) {
                // 设置该订单的子订单信息
                getAllOrder.setDifOrderNum(orderInfo1.getOrderNum());
                SubBizOrder subBizOrder = new SubBizOrder();
                subBizOrder.setCreateTime(orderInfo1.getCreateTime());
                List<OrderGoodsInfo> orderGoodsInfoList = orderGoodsDao.select(orderInfo1.getOrderNum());
                subBizOrder.setOrderGoodsInfoList(orderGoodsInfoList);
                subBizOrder.setOrderNum(orderInfo1.getOrderNum());
                subBizOrder.setOrderStatus(orderInfo1.getOrderStatus());
                subBizOrder.setRemark(orderInfo1.getOrderRemark());
                if (orderInfo1.getOrderType() != null) {
                    subBizOrder.setOrderType(orderInfo1.getOrderType());
                } else {
                    Integer orderType = orderService.getOrderType(orderInfo1.getOrderNum());
                    subBizOrder.setOrderType(orderType);
                }
                getAllOrder.setSubBizOrder(subBizOrder);
            }
            getAllOrder.setDeliveryType(orderInfo.getIsDelivery());
            getAllOrders.add(getAllOrder);
        }
        // 分页
        List<GetAllOrder> collect = getAllOrders.stream().sorted(Comparator.comparing(GetAllOrder::getCreateTime).reversed())
                .skip((request.getPageNo() - 1) * request.getPageSize()).limit(request.getPageSize())
                .collect(Collectors.toList());
        response.setData(collect);
        return response;
    }


    /**
     * 编辑接口V3
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/v3/editDragon", method = RequestMethod.POST)
    public EditDragonResponse editDragonV3(@RequestBody EditDragonRequest request) {

        final EditDragonResponse response = new EditDragonResponse();


        final List<OrderInfo> orderInfos = orderService.getOpenidByDragonNum(request.getDragonNum());
        if (orderInfos.size() != 0 && request.getIgnoreExpired() == 0) {
            return (EditDragonResponse) response.build(RespStatus.DRAGON_ALREADY_EXIST_ORDER);
        }


        final DragonInfo dragonInfo = dragonService.getDragon(request.getDragonNum());
        if (dragonInfo == null) {
            return (EditDragonResponse) response.build(RespStatus.DRAGON_NOT_EXIST);
        }
        //判断该接龙是否已结束并且不是重新发布
        if (dragonInfo.getDragonStatus() == 1 && request.getIgnoreExpired() == 0) {
            return (EditDragonResponse) response.build(RespStatus.DRAGON_IS_END);
        }

        final WxUser user = wxUserService.getWxUser(dragonInfo.getOpenid());
        if (user == null) {
            return (EditDragonResponse) response.build(RespStatus.USER_NOT_EXIST);
        }

        final List<DragonGoods> dragonGoods = dragonGoodsService.getGoodsNUM(request.getDragonNum());
        if (dragonGoods == null) {
            return (EditDragonResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        final List<GoodsInfo> list = Lists.newArrayList();
        for (DragonGoods dgInfo : dragonGoods) {
            final GoodsInfo goodsInfo = goodsService.getGoods(dgInfo.getGoodsNum());
            if (goodsInfo == null) {
                return (EditDragonResponse) response.build(RespStatus.GOODS_IS_NOT_EXIST);
            }
            goodsInfo.setLimitBuyNum(dgInfo.getLimitBuyNum());

            goodsInfo.setSpecification(dgInfo.getSpecification());
            goodsInfo.setTotalNumber(dgInfo.getCurrentNumber());
            goodsInfo.setPrice(dgInfo.getCurrentPrice());
            goodsInfo.setGoodsName(dgInfo.getGoodsName());
            goodsInfo.setGoodsImg(dgInfo.getGoodsImg());


            list.add(goodsInfo);
        }


        List<DragonAddr> dragonAddrList = dragonService.selectByDragonNum(dragonInfo.getDragonNum());

        if (dragonAddrList.size() < 1 && dragonInfo.getIsDelivery() != 1 && StringUtils.isNotBlank(dragonInfo.getAddr())) {

            List<String> addrs = Arrays.asList(dragonInfo.getAddr().split("&"));

            for (String addr : addrs) {

                DragonAddr dragonAddr = new DragonAddr();

                dragonAddr.setAddr(addr);

                dragonAddrList.add(dragonAddr);
            }

        }


        response.setDragonAddr(dragonAddrList);


        response.setCreateTime(dragonInfo.getCreateTime());
        response.setDragonImgs(dragonInfo.getDragonImg());
        response.setDragonRemark(dragonInfo.getDragonDesc());
        response.setDragonTitle(dragonInfo.getDragonTitle());

        if (StringUtils.isNotBlank(dragonInfo.getEndTime())) {
            response.setEndTime(DateUtils.timeToTimeStamp(dragonInfo.getEndTime()));
        }

        response.setHeadImg(user.getHeadImgUrl());
        response.setIsCOD(dragonInfo.getCashOnDelivery());
        response.setIsPayLater(dragonInfo.getIsPayLater());
        response.setNickName(user.getNickName());
        response.setPhone(dragonInfo.getPhone());
        if (StringUtils.isNotBlank(dragonInfo.getSendTime())) {
            response.setSendTime(DateUtils.timeToTimeStamp(dragonInfo.getSendTime()));
        }
        Integer hasBuy = 0;
        List<OrderInfo> orderInfos1 = orderInfoDao.selectOrder(request.getDragonNum());
        if (CollectionUtils.isNotEmpty(orderInfos1)) {
            hasBuy = 1;
        }
        response.setHasBuy(hasBuy);
        // 过滤已删除的商品
        List<GoodsInfo> goodsInfos = goodsService.filterAreadyDeletedGoods(list);
        response.setGoodsList(goodsInfos);
        response.setSessionId(user.getSessionId());
        response.setIsDelivery(dragonInfo.getIsDelivery());
        response.setGlobalLimit(dragonInfo.getGlobalLimit());
        response.setIsCoupon(dragonInfo.getIsCoupon().intValue());
        response.setDeliveryCycle(JsonUtils.toObj(dragonInfo.getDeliveryCycle(), DeliveryCycle.class));
        response.setDragonVideo(dragonInfo.getDragonVideo());
        return response;
    }

    /**
     * 保存修改接龙V3
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/v3/updateDragon", method = RequestMethod.POST)
    public UpdateDragonResponse updateDragonV3(@RequestBody UpdateDragonRequest request) {
        final UpdateDragonResponse response = new UpdateDragonResponse();

        WxUser user = wxUserService.getUserBySessionId(request.getSessionId());

        if (null == user) {
            return (UpdateDragonResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        request.setOpenid(user.getOpenid());

        if (null == request.getData() || request.getData().size() < 1) {
            return (UpdateDragonResponse) response.build(RespStatus.GOODS_NOT_NULL);
        }

        for (Goods goods : request.getData()) {
            if (goods.getPrice() == null || Integer.valueOf(0).equals(goods.getPrice())) {
                return (UpdateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
            }

            if (StringUtils.isBlank(goods.getSpecification()) || StringUtils.isBlank(goods.getGoodsName())) {
                return (UpdateDragonResponse) response.build(RespStatus.ILLEGAL_ARGUMENT);
            }
        }
        Integer isDelivery = request.getIsDelivery();
        if (verifyAddress(request.getDragonAddrIds(), isDelivery)) {
            return (UpdateDragonResponse) response.build(RespStatus.ADDR_IS_NULL);
        }

        DeliveryCycle cycle = request.getDeliveryCycle();
        if (cycle != null) {
            if (!DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(cycle.getCycleType())) {
                request.setEndTime(null);
            }
        }

        //根据sessionId查询openid

        try {
            dragonService.updateDragonV3(request);
        } catch (Exception e) {
            e.printStackTrace();
            return (UpdateDragonResponse) response.build(RespStatus.UPDATE_DRAGON_ERROR);
        }
        return response;
    }

    /**
     * 校验自提地址
     *
     * @param addressIds
     * @param isDelivery
     * @return
     */
    private boolean verifyAddress(List<Integer> addressIds, Integer isDelivery) {
        if (DragonDeliveryEnum.PICK_UP_By_SELF.getCode().equals(isDelivery)
                || DragonDeliveryEnum.PICK_UP_AND_EXPRESS.getCode().equals(isDelivery)
                || DragonDeliveryEnum.PICK_UP_By_SELF_AND_HOME_DELIVERY_SERVICE.getCode().equals(isDelivery)
                || DragonDeliveryEnum.PICK_UP_AND_EXPRESS_HOME_DELIVERY_SERVICE.getCode().equals(isDelivery)) {

            if (org.springframework.util.CollectionUtils.isEmpty(addressIds)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 删除商品信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/v2/delGoods", method = RequestMethod.POST)
    public BaseResponse delGoods(@RequestBody DelGoodsRequest request) {
        final BaseResponse response = new BaseResponse();

        GoodsInfo goodsInfo = goodsInfoDao.selectGoods(request.getGoodsNum());
        int i = 0;
        if (null != goodsInfo) {
            goodsInfo.setDeleted(true);
            i = goodsInfoDao.updateGoods(goodsInfo);
        }
        DragonGoods byGoodsNum = dragonGoodsService.findByGoodsNum(request.getGoodsNum());
        if (byGoodsNum != null) {
            byGoodsNum.setIsHidden((byte) 1);
            boolean flag = dragonGoodsService.updateDragonGoods(byGoodsNum);
            if (i != 0 && flag) {
                response.setCode(200);
                return response;
            }
        } else {
            log.error("接龙商品不存在!商品编号{}", request.getGoodsNum());
        }

        return response;
    }

    /**
     * 获取我的用户
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/myUser", method = RequestMethod.POST)
    public GetMyUserResponse getMyUser(@RequestBody GetMyUserRequest request) {
        long start = System.currentTimeMillis();
        final GetMyUserResponse response = new GetMyUserResponse();
        //根据sessionId查询openid
        WxUser us = wxUserService.getUserBySessionId(request.getSessionId());

        if (us == null) {
            return (GetMyUserResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        request.setOpenid(us.getOpenid());
        List<MyUser> list;
        if (ORDER_BY_REPURCHASE_COUNT.equals(request.getState())) {
            //如果排序规则是按复购口碑排，先不分页
            list = myUserService.getMyAllUser(request);
        } else {
            //其他排序，在sql中可以排
            PageHelper.startPage(request.getPage(), request.getSize());
            list = myUserService.getMyAllUser(request);
        }

        List<GetMyUser> userList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(list)) {
            response.setData(userList);
            return response;
        }
        String sellerOpenId = us.getOpenid();
        for (MyUser myUser : list) {
            WxUser wxUser = wxUserService.getWxUser(myUser.getUserOpenid());
            GetMyUser getMyUser = new GetMyUser();
            getMyUser.setHeadImg(wxUser.getHeadImgUrl());
            getMyUser.setNickName(wxUser.getNickName());
            //从订单中获取用户信息
            List<OrderInfo> orders = orderService.getOrderByOpenid(myUser.getUserOpenid());
            if (CollectionUtils.isEmpty(orders)) {
                //如果没有订单则过滤
                continue;
            }
            //orderInfo只用来显示收货地址、电话等作用
            OrderInfo orderInfo = new OrderInfo();
            for (OrderInfo o : orders) {
                if (StringUtils.isNotBlank(o.getAddrName()) && StringUtils.isNotBlank(o.getAddrPhone())
                        && StringUtils.isNotBlank(o.getAddress())) {
                    orderInfo = o;
                    break;
                }
            }
            Integer repurChaseCount = statisticsService.getUserRepurchase(myUser.getUserOpenid(), sellerOpenId);
            getMyUser.setUserName(orderInfo.getAddrName());
            getMyUser.setPhone(orderInfo.getAddrPhone());
            getMyUser.setUserAddr(orderInfo.getAddress());
            getMyUser.setCommitTime(orderInfo.getCreateTime());
            getMyUser.setBuyTime(myUser.getBuyTime());
            getMyUser.setTotalMoney(myUser.getTotalMoney());
            getMyUser.setOpenid(myUser.getUserOpenid());
            getMyUser.setRepurchaseCount(repurChaseCount);
            userList.add(getMyUser);
        }
        if (ORDER_BY_REPURCHASE_COUNT.equals(request.getState())) {
            //手动分页
            Integer offset = (request.getPage() - 1) * request.getSize();
            List<GetMyUser> orderbyRepurchase = userList.stream().sorted(Comparator.comparing(GetMyUser::getRepurchaseCount).reversed()).skip(offset).limit(request.getSize()).collect(Collectors.toList());
            response.setData(orderbyRepurchase);
        } else {
            response.setData(userList);
        }
        log.info("管理用户列表 方法耗时：{}", System.currentTimeMillis() - start);

        return response;
    }


    /**
     * 接龙是否已被下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/v3/dragonHaveOrder", method = RequestMethod.POST)
    public BaseResponse dragonHaveOrder(@RequestBody EditDragonRequest request) {
        final BaseResponse response = new BaseResponse();

        List<OrderInfo> orderInfos = orderService.getOpenidByDragonNum(request.getDragonNum());

        if (orderInfos.size() > 0) {
            return response.build(RespStatus.DRAGON_ALREADY_EXIST_ORDER);
        }

        return response;
    }

    /**
     * 卖家历史商品列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/historyGoodsList", method = RequestMethod.POST)
    public HistoryGoodsListResponse historyGoodsList(@RequestBody HistoryGoodsListRequest request) {
        final HistoryGoodsListResponse response = new HistoryGoodsListResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (null == wxUser) {
            return (HistoryGoodsListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        PageHelper.startPage(request.getPage(), request.getSize());
        List<GoodsInfo> list = goodsService.getGoodsListByUser(wxUser.getOpenid());
        // 过滤已删除的商品
        List<GoodsInfo> collect = goodsService.filterAreadyDeletedGoods(list);

        PageInfo<GoodsInfo> pageInfo = new PageInfo<>(collect);

        response.setPageInfo(pageInfo);

        return response;
    }


    /**
     * 卖家接龙主页
     *
     * @return
     */
    @RequestMapping(value = "/v5/getSellerDragonInfo", method = RequestMethod.POST)
    public GetAllDragonInfoResponse getSellerDragonInfo(@RequestBody GetSellerDragonInfoRequest request) {
        GetAllDragonInfoResponse response = new GetAllDragonInfoResponse();

        WxUser user;

        if (request.getOpenid() == null || request.getOpenid().isEmpty()) {
            user = wxUserService.getUserBySessionId(request.getSessionId());
        } else {
            user = wxUserService.getWxUser(request.getOpenid());
        }


        if (null == user) {
            return (GetAllDragonInfoResponse) response.build(RespStatus.USER_NOT_EXIST);
        }

        request.setOpenid(user.getOpenid());

        DragonInfo dragonInfoRequest = new DragonInfo();

        dragonInfoRequest.setOpenid(request.getOpenid());
        dragonInfoRequest.setDragonStatus(request.getDragonStatus());

        //获取所有的接龙信息
        PageHelper.startPage(request.getPage(), request.getSize());
        List<DragonInfo> dragonInfoList = dragonService.selectDragonByStatusAndOpenid(dragonInfoRequest);
        PageInfo<DragonInfo> pageInfo = new PageInfo<>(dragonInfoList);


        if (null == dragonInfoList && dragonInfoList.size() == 0) {
            return (GetAllDragonInfoResponse) response.build(RespStatus.CURRENT_NOT_DRAGON);
        }

        int totalPartyNumber = 0;

        List<Dragon> list = Lists.newArrayList();

        for (DragonInfo dragon : dragonInfoList) {
            //查询接龙发起者

            Dragon dragon1 = new Dragon();
            dragon1.setDragonImg(dragon.getDragonImg());
            dragon1.setDragonNum(dragon.getDragonNum());
            dragon1.setRemark(dragon.getDragonDesc());
            dragon1.setTitle(dragon.getDragonTitle());
            dragon1.setNickName(user.getNickName());
            dragon1.setHead(user.getHeadImgUrl());
            dragon1.setCreateTime(dragon.getCreateTime());
            dragon1.setIsDelivery(dragon.getIsDelivery());


            List<DragonAddr> dragonAddrList = dragonService.selectByDragonNum(dragon.getDragonNum());

            if (dragonAddrList.size() < 1 && dragon.getIsDelivery() != 1) {
                List<String> addrs = Arrays.asList(dragon.getAddr().split("&"));

                for (String addr : addrs) {

                    DragonAddr dragonAddr = new DragonAddr();

                    dragonAddr.setAddr(addr);

                    dragonAddrList.add(dragonAddr);
                }

            }

            dragon1.setDragonAddrs(dragonAddrList);


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                String deliveryCycle = dragon.getDeliveryCycle();
                DeliveryCycle deliveryCycle1 = JsonUtils.toObj(deliveryCycle, DeliveryCycle.class);
                if (DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(deliveryCycle1.getCycleType()) && StringUtils.isNotBlank(dragon.getEndTime())) {
                    date = simpleDateFormat.parse(dragon.getEndTime());
                    dragon1.setEndTime(date.getTime());
                }

            } catch (ParseException e) {
                e.printStackTrace();
                log.error("接龙详情结束时间有误:" + e);
                return (GetAllDragonInfoResponse) response.build(RespStatus.SYSTEM_ERROR);
            }


            List<OrderInfo> orderInfoList = orderService.getOpenidByDragonNum(dragon.getDragonNum());
            if (null == orderInfoList) {
                return (GetAllDragonInfoResponse) response.build(RespStatus.SYSTEM_ERROR);

            }
            //设置参与人数
            List<String> headImgList = Lists.newArrayList();

            List<OrderInfo> orderInfos = Lists.newArrayList();

            int dragonPartyNum = 0;

            for (OrderInfo info : orderInfoList) {
                if (info.getOrderStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getCode())
                        || info.getOrderStatus().equals(OrderStatusEnum.REFUNDED.getCode())
                        || info.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())
                        || info.getParentOrderNum() != null) {
                    continue;
                }
                WxUser wxUser = wxUserService.getWxUser(info.getOpenid());
                headImgList.add(wxUser.getHeadImgUrl());
                dragonPartyNum++;
                orderInfos.add(info);
            }

            dragon1.setPartNumber(dragonPartyNum);

            totalPartyNumber += dragonPartyNum;

            //设置参与者头像列表
            dragon1.setHeadImg(headImgList);


            //添加历史订单数目

            Integer count = orderService.countOrderNumByDragon(dragon.getDragonNum());

            dragon1.setOrderNumber(count);

            list.add(dragon1);

        }
        response.setTotalNumber(new Long(pageInfo.getTotal()).intValue());
        response.setTotalPartyNumber(totalPartyNumber);
        response.setData(list);
        response.setOrderNumber(user.getOrderNumber());
        response.setHead(user.getHeadImgUrl());
        response.setNickName(user.getNickName());
        response.setOrderNumber(user.getOrderNumber());
        response.setOpenid(user.getOpenid());

        return response;
    }

    /**
     * 接龙草稿添加
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dragon/addDraft", method = RequestMethod.POST)
    public BaseResponse addDraft(@RequestBody AddDraftRequest request) {
        final BaseResponse response = new BaseResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (null == wxUser) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        try {
            DragonDraft dragonDraft = new DragonDraft();

            dragonDraft.setCreateTime(new Date());
            dragonDraft.setDragonJson(request.getDragonJson());
            dragonDraft.setOpenid(wxUser.getOpenid());
            dragonDraft.setIsDelete(0);

            dragonService.updateDraft(dragonDraft);
        } catch (Exception e) {
            e.printStackTrace();
            return response.build(RespStatus.SYSTEM_ERROR.getKey(), e.getMessage());
        }

        return response;
    }

    /**
     * 接龙草稿获取
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dragon/getDraft", method = RequestMethod.POST)
    public GetDraftResponse getDraft(@RequestBody AddDraftRequest request) {
        final GetDraftResponse response = new GetDraftResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (null == wxUser) {
            return (GetDraftResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        try {
            DragonDraft dragonDraft = dragonService.getDraft(wxUser.getOpenid());
            if (dragonDraft != null) {
                response.setDragonJson(dragonDraft.getDragonJson());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return (GetDraftResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 接龙草稿删除
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dragon/deleteDraft", method = RequestMethod.POST)
    public BaseResponse deleteDraft(@RequestBody AddDraftRequest request) {
        final BaseResponse response = new BaseResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (null == wxUser) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        try {
            dragonService.deleteDraft(wxUser.getOpenid());
        } catch (Exception e) {
            e.printStackTrace();
            return response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 接龙自提地点获取
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getDragonAddr", method = RequestMethod.POST)
    public BaseResponse getDragonAddr(@RequestBody DragonAddrRequest request) {
        final BaseResponse response = new BaseResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }

        request.setOpenid(wxUser.getOpenid());

        List<DragonAddr> result = dragonService.getAllDragonAddr(request);

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("dragonAddr", result);

        response.setMap(map);

        return response;
    }

    /**
     * 接龙自提地点添加
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/addDragonAddr", method = RequestMethod.POST)
    public BaseResponse addDragonAddr(@RequestBody AddDragonAddrRequest request) {
        final BaseResponse response = new BaseResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        request.setOpenid(wxUser.getOpenid());

        if (StringUtils.isNotBlank(request.getName()) && NAME_LENGTH < request.getName().length()) {
            return response.build(RespStatus.ADDR_NAME_LIMIT);
        }

        try {
            dragonService.addDragonAddr(request);
        } catch (Exception e) {
            e.printStackTrace();
            return response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 接龙自提地点修改
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateDragonAddr", method = RequestMethod.POST)
    public BaseResponse delDragonAddr(@RequestBody AddDragonAddrRequest request) {
        final BaseResponse response = new BaseResponse();

        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());

        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        if (null == request.getDragonAddId()) {
            return response.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        if (StringUtils.isNotBlank(request.getName()) && NAME_LENGTH < request.getName().length()) {
            return response.build(RespStatus.ADDR_NAME_LIMIT);
        }
        DragonAddr dragonAddr = new DragonAddr();
        dragonAddr.setId(request.getDragonAddId());
        dragonAddr.setOpenid(wxUser.getOpenid());
        dragonAddr.setLatitude(request.getLatitude());
        dragonAddr.setLongitude(request.getLongitude());
        dragonAddr.setDetailAddr(request.getDetailAddr());
        dragonAddr.setName(request.getName());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(request.getDeleted())) {
            dragonAddr.setDeleted(Boolean.parseBoolean(request.getDeleted()));
        }
        int i = dragonAddrDao.updateByPrimaryKeySelective(dragonAddr);
        if (i > 0) {
            response.setCode(200);
            return response;
        }
        response.setCode(500);
        return response;
    }

    /**
     * 新增
     * 接龙详情页-复购名单,购买商品,复购口碑
     */
    @ApiOperation(value = "/dragonDetailRepurchaseInfo", notes = "接龙详情页-复购名单,购买商品,复购口碑")
    @RequestMapping(value = "/dragonDetailRepurchaseInfo", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse dragonDetailRepurchaseInfo(@RequestBody DragonDetailRequestParam param) {
        final BaseResponse response = new BaseResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(param.getSessionId());
        if (wxUser == null) {
            return response.build(RespStatus.SESSION_ID_EXPIRE);
        }
        List<DragonDetailRepurchaseVO> list = dragonService.dragonDetailRepurchaseInfo(param);
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("dragonDetail", list);
        response.setMap(hashMap);

        return response;
    }

    /**
     * 统计参与接龙的每个用户的复购次数
     */
    @Deprecated
    @RequestMapping(value = "/countRepurchase", method = RequestMethod.POST)
    public BaseResponse countRepurchase(@RequestBody BaseQuery query) {
        final BaseResponse response = new BaseResponse();

        return response;
    }

    /**
     * 浏览过的接龙
     */
    @RequestMapping(value = "/browsedDragon", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse browseDragon(@RequestBody BaseQuery query) {
        Long start = System.currentTimeMillis();
        BrowsedDragonResponse response = new BrowsedDragonResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(query.getSessionId());
        Set<String> sellerOpenIds = new HashSet<>();
        String port = environment.getProperty("server.port");
        Integer show = 0;
        Map<String, List<BrowseRecord>> map = Maps.newHashMap();
        if (port.equals("8080")) {
            //product版
            show = dragonShowService.selectDragonShow(VersionEnum.PRODUCT.getCode());
        }
        if (port.equals("8889")) {
            //release版
            show = dragonShowService.selectDragonShow(VersionEnum.RELEASE.getCode());
        }
        if (show.equals(CheckStatusEnum.START_CHECK.getCode())) {
            sellerOpenIds.add("ojzCP4nWQW3muiO_pcn7j6K1tJ5U");
        } else {
            if (wxUser == null) {
                return response.build(RespStatus.SESSION_ID_EXPIRE);
            }

            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("isWhite", wxUser.getIsWhite());
            hashMap.put("dragonButIsOpen", wxUser.getDragonButIsOpen());
            response.setMap(hashMap);
            // 查询浏览记录表最新的浏览记录
            BrowseRecordQuery browseRecordQuery = new BrowseRecordQuery();
            browseRecordQuery.setUserId(wxUser.getId());
            browseRecordQuery.setPageNo(query.getPageNo());
            browseRecordQuery.setPageSize(query.getPageSize());
            List<BrowseRecord> recordList = browseRecordDao.findLatestBrowseRecord(browseRecordQuery);

            //recordList = recordList.parallelStream().sorted(Comparator.comparing(BrowseRecord::getCreateTime).reversed()).collect(Collectors.toList());
            if (recordList.isEmpty() && query.getPageNo() == 1) {
                //如果浏览记录为空，查询最新的三个接龙，且不是同一个卖家
                List<String> openIds = dragonService.selectLatestThreeShopOfDragon();
                sellerOpenIds.addAll(openIds);

            } else {
                // 根据浏览记录按卖家分组
                map = recordList.stream().collect(Collectors.groupingBy(BrowseRecord::getSellerOpenid));
                sellerOpenIds = map.keySet();

            }
        }


        List<ShopDragonInfo> shopDragonInfoList = new ArrayList<>();
        Map<String, List<BrowseRecord>> finalMap = map;
        sellerOpenIds.parallelStream().forEach(sellerOpenId -> {
            // 查询卖家店铺信息
            ShopInfoQuery shopInfoQuery = new ShopInfoQuery();
            shopInfoQuery.setPageNo(1);
            shopInfoQuery.setPageSize(1);
            shopInfoQuery.setShopkeeperOpenId(sellerOpenId);
            List<Shop> shops = shopMapper.queryShopInfo(shopInfoQuery);
            if (CollectionUtils.isEmpty(shops)) {
                // 若未找到卖家店铺信息则跳过
                return;
            }
            Shop shop = shops.get(0);
            // 查询最新的进行中的接龙
            DragonInfoQuery dragonInfoQuery = new DragonInfoQuery();
            dragonInfoQuery.setOpenid(sellerOpenId);
            dragonInfoQuery.setDragonStatus(DragonStatusEnum.IN_PROGRESS.getStatus());
            List<DragonInfo> dragonInfos = Lists.newArrayList();
            // List<DragonInfo> browseDragon = Lists.newArrayList();

            if (MapUtils.isNotEmpty(finalMap)) {
                List<BrowseRecord> browseRecords = finalMap.get(sellerOpenId);
                Map<String, List<BrowseRecord>> collect = browseRecords.stream().collect(Collectors.groupingBy(BrowseRecord::getDragonNum));
                Iterator<String> browseDrago = collect.keySet().iterator();
                while (browseDrago.hasNext()) {
                    String dragonNum = browseDrago.next();
                    List<BrowseRecord> browseRecords1 = collect.get(dragonNum);
                    List<BrowseRecord> collect1 = browseRecords1.stream().sorted(Comparator.comparing(BrowseRecord::getCreateTime).reversed()).collect(Collectors.toList());
                    DragonInfo dragon = dragonService.getDragon(dragonNum);
                    dragon.setBrowseTime(collect1.get(0).getCreateTime());
                    dragonInfos.add(dragon);

                }

            } else {
                dragonInfos = dragonService.queryLatestDragon(dragonInfoQuery);
            }

            // 查询复购订单数 需要优化
            Long count = statisticsService.getShopRepurchaseNum(sellerOpenId);
            ShopDragonInfo shopDragonInfo = new ShopDragonInfo();
            //遍历接龙列表,填充响应数据
            List<DragonInfoVO> dragonInfoVOS = new ArrayList<>();

            dragonInfos.parallelStream().forEach(dragon -> {
                shopDragonInfo.setShopId(shop.getShopId());
                shopDragonInfo.setShopName(shop.getShopName());
                shopDragonInfo.setShopkeeperOpenId(shop.getShopkeeperOpenId());
                shopDragonInfo.setRepurchaseCount(count.intValue());
                shopDragonInfo.setShopHeadPicture(shop.getShopHeadPicture());
                // 查询接龙对应的
                DragonInfoVO dragonInfoVO = new DragonInfoVO();
                // 查询地址信息
                List<DragonAddr> dragonAddrList = dragonService.selectByDragonNum(dragon.getDragonNum());
                if (dragonAddrList.size() < 1 && 1 != dragon.getIsDelivery() && CollectionUtils.isNotEmpty(dragonAddrList)) {
                    List<String> addrs = Arrays.asList(dragon.getAddr().split("&"));
                    addrs.parallelStream().forEach(addr -> {
                        DragonAddr dragonAddr = new DragonAddr();
                        dragonAddr.setAddr(addr);
                        dragonAddrList.add(dragonAddr);
                    });
                }
                dragonInfoVO.setDragonAddrs(dragonAddrList);
                dragonInfoVO.setIsDelivery(dragon.getIsDelivery());
                dragonInfoVO.setCreateTime(dragon.getCreateTime());
                dragonInfoVO.setDragonImg(dragon.getDragonImg());
                dragonInfoVO.setDragonNum(dragon.getDragonNum());
                dragonInfoVO.setTitle(dragon.getDragonTitle());
                dragonInfoVO.setRemark(dragon.getDragonDesc());
                dragonInfoVO.setBrowseTime(dragon.getBrowseTime());
                List<String> headImgs = new ArrayList<>();
                String cycle = dragon.getDeliveryCycle();
                dragonInfoVO.setSendTime(dragon.getSendTime());
                if (org.apache.commons.lang3.StringUtils.isNotBlank(cycle)) {
                    DeliveryCycle deliveryCycle = JsonUtils.toObj(cycle, DeliveryCycle.class);
                    dragonInfoVO.setDeliveryCycle(deliveryCycle);
                    // 周期性截单时间
                    Date cutOffTime = dragonService.getDragonDeliveryCycleDate(cycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME);
                    if (null != cutOffTime) {
                        dragonInfoVO.setCutOffTime(DateUtil.SDF.format(cutOffTime));
                        dragonInfoVO.setEndTime(dragonInfoVO.getCutOffTime());
                    } else {
                        dragonInfoVO.setCutOffTime(dragon.getEndTime());
                        dragonInfoVO.setEndTime(dragon.getEndTime());
                    }
                    if (DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(deliveryCycle.getCycleType())) {
                        if (StringUtils.isNotBlank(dragonInfoVO.getSendTime())) {
                            dragonInfoVO.setSendTime(dragonInfoVO.getSendTime().substring(0, dragonInfoVO.getSendTime().lastIndexOf(":")));
                        }
                        dragonInfoVO.setEndTime(dragon.getEndTime());
                    } else {
                        // 周期性发货时间
                        Integer[] dayOfWeek = deliveryCycle.getDayOfWeek();
                        Integer hour = deliveryCycle.getHour();
                        if (dayOfWeek.length >= 1) {
                            StringBuilder msg = new StringBuilder("每");
                            for (Integer i : dayOfWeek) {
                                msg.append(WeekEnum.getDayByNum(i)).append(" ");
                            }
                            msg.append(hour).append("点");
                            dragonInfoVO.setSendTime(msg.toString());
                        } else {

                            Date dragonDeliveryCycleDate = dragonService.getDragonDeliveryCycleDate(cycle, DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME);
                            if (dragonDeliveryCycleDate != null) {
                                dragonInfoVO.setSendTime(DateUtil.SDF.format(dragonDeliveryCycleDate));
                            } else {
                                dragonInfoVO.setSendTime(dragon.getSendTime());
                            }
                        }
                    }
                } else {
                    // 旧数据没有deliveryCycle
                    if (StringUtils.isNotBlank(dragon.getCutOffTime())) {
                        dragonInfoVO.setCutOffTime(dragon.getCutOffTime());
                    } else {
                        dragonInfoVO.setCutOffTime(dragon.getEndTime());
                    }
                    if (StringUtils.isNotBlank(dragonInfoVO.getSendTime())) {
                        dragonInfoVO.setSendTime(dragonInfoVO.getSendTime().substring(0, dragonInfoVO.getSendTime().lastIndexOf(":")));
                    }
                    dragonInfoVO.setEndTime(dragon.getCutOffTime());
                }
                List<FindDragon> partDragonUser = dragonService.getPartDragonUser(dragon.getDragonNum(), null);
                partDragonUser.parallelStream().forEach(x ->
                        headImgs.add(x.getHeadImg())
                );
                dragonInfoVO.setIsDelivery(dragon.getIsDelivery());
                dragonInfoVO.setPartyNumber(partDragonUser.size());
                dragonInfoVO.setHeadImg(headImgs);
                //dragonInfoVO.setOrderNumber(orderService.countOrderNumByDragon(dragon.getDragonNum()));
                //Long alreadyCompeteOrderCount = orderService.countBizOrderByDragonNum(dragon.getDragonNum());
                //对返回的日期进行format
                if (StringUtils.isNotBlank(dragonInfoVO.getCutOffTime())) {
                    dragonInfoVO.setCutOffTime(dragonInfoVO.getCutOffTime().substring(0, dragonInfoVO.getCutOffTime().lastIndexOf(":")));
                }
                dragonInfoVOS.add(dragonInfoVO);
            });
            if (CollectionUtils.isNotEmpty(dragonInfoVOS)) {
                // 店铺存在未结束的接龙时返回店铺信息
                dragonInfoVOS.forEach(x -> {
                    if (Objects.isNull(x.getBrowseTime())) {
                        x.setBrowseTime(new Date());
                    }
                });
                List<DragonInfoVO> collect1 = dragonInfoVOS.stream().sorted(Comparator.comparing(DragonInfoVO::getBrowseTime).reversed()).collect(Collectors.toList());
                shopDragonInfo.setDragonInfoVOList(collect1);
                BrowseRecordQuery recordQuery = new BrowseRecordQuery();
                recordQuery.setSellerOpenid(shopDragonInfo.getShopkeeperOpenId());
                recordQuery.setPageNo(query.getPageNo());
                recordQuery.setPageSize(1);
                List<BrowseRecord> browseRecord = browseRecordDao.findLatestBrowseRecord(recordQuery);
                if (CollectionUtils.isNotEmpty(browseRecord)) {
                    shopDragonInfo.setBrowseTime(browseRecord.get(0).getCreateTime());
                }
                shopDragonInfoList.add(shopDragonInfo);
            }
        });

        List<ShopDragonInfo> collect = shopDragonInfoList.stream()
                .sorted(Comparator.comparing(ShopDragonInfo::getBrowseTime).reversed())
                .collect(Collectors.toList());
        // 浏览过的接龙逻辑分页
        response.setShopDragonInfoList(collect);
        Long end = System.currentTimeMillis();
        log.info("browsedDragon接口耗时：{}", end - start);
        return response;
    }

    /**
     * 我关注的店铺
     */
    @RequestMapping(value = "/followedShops", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse attentionShops(@RequestBody BaseQuery query) {
        Long start = System.currentTimeMillis();
        BrowsedDragonResponse response = new BrowsedDragonResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(query.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        List<AttentionShopVO> attentionShopVOList = new ArrayList<>();
        ShopInfoQuery shopInfoQuery = new ShopInfoQuery();
        shopInfoQuery.setUserId(Long.valueOf(wxUser.getId()));
        shopInfoQuery.setStatus(ShopEnum.ALREADY_ATTENTION.getCode());
        shopInfoQuery.setPageNo(query.getPageNo());
        shopInfoQuery.setPageSize(query.getPageSize());
        List<AttentionShop> attentionShops = attentionShopMapper.queryAttentionShopList(shopInfoQuery);
        if (org.springframework.util.CollectionUtils.isEmpty(attentionShops)) {
            // 没找到到返回空数组
            response.setAttentionShopVOList(attentionShopVOList);
            return response;
        }
        attentionShops.parallelStream().forEach(attentionShop -> {
            AttentionShopVO attentionShopVO = new AttentionShopVO();
            // 根据店铺信息查询商家的进行中的接龙
            DragonInfo dragon = new DragonInfo();
            dragon.setOpenid(attentionShop.getShopkeeperOpenId());
            dragon.setDragonStatus(DragonStatusEnum.IN_PROGRESS.getStatus());
            List<DragonInfo> dragonInfos = dragonService.selectDragonByStatusAndOpenid(dragon);
            List<DragonInfoVO> dragonInfoVOList = new ArrayList<>();
            // 遍历正在进行的接龙列表,统计每个接龙的参与人数
            if (CollectionUtils.isNotEmpty(dragonInfos)) {
                dragonInfos.parallelStream().forEach(dragonInfo -> {
                    List<FindDragon> partDragonUser = dragonService.getPartDragonUser(dragonInfo.getDragonNum(), null);
                    DragonInfoVO dragonInfoVO = new DragonInfoVO();
                    dragonInfoVO.setPartyNumber(partDragonUser.size());
                    dragonInfoVO.setTitle(dragonInfo.getDragonTitle());
                    dragonInfoVO.setDragonImg(dragonInfo.getDragonImg());
                    dragonInfoVO.setDragonNum(dragonInfo.getDragonNum());
                    dragonInfoVOList.add(dragonInfoVO);
                });
            }
            Long repurchaseOrder = statisticsService.getShopRepurchaseNum(attentionShop.getShopkeeperOpenId());
            attentionShopVO.setShopId(attentionShop.getShopId());
            attentionShopVO.setShopName(attentionShop.getShopName());
            attentionShopVO.setShopHeadPicture(attentionShop.getShopHeadPicture());
            attentionShopVO.setRepurchase(repurchaseOrder.intValue());
            attentionShopVO.setDragonInProcessCount(dragonInfoVOList.size());
            attentionShopVO.setDragonInfoVOList(dragonInfoVOList);
            attentionShopVO.setShopkeeperOpenId(attentionShop.getShopkeeperOpenId());
            attentionShopVOList.add(attentionShopVO);
        });

        List<AttentionShopVO> collect = attentionShopVOList.stream().skip(query.getOffset()).limit(query.getPageSize()).collect(Collectors.toList());
        response.setAttentionShopVOList(collect);
        Long end = System.currentTimeMillis();
        log.info("followedShops接口耗时：{}", end - start);
        return response;
    }


    /**
     * 判断接龙是否在进行中
     */
    @RequestMapping(value = "/isDragonInProcess", method = RequestMethod.POST)
    public BaseResponse isDragonInProgress(@RequestBody Dragon dragon, BindingResult result) {
        BaseResponse response = new BaseResponse();
        if (result.hasErrors()) {
            throw new DataException("参数错误");
        }

        DragonInfo getInfo = dragonService.getDragon(dragon.getDragonNum());

        Integer dragonStatus = getInfo.getDragonStatus();
        response.setCode(dragonStatus);
        return response;
    }


    /**
     * <p> 获取接龙下的发货时间和截单时间 </p>
     *
     * @param dragon
     * @return Map<String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               Date>
     * @author 昊天
     * @date 2019/2/22 5:17 PM
     * @since V1.1.0-SNAPSHOT
     */
    private Map<String, Date> getTimeFromDragon(DragonInfo dragon) throws ParseException {
        Map<String, Date> map = new HashMap<>();
        DeliveryCycle deliveryCycle = JsonUtils.toObj(dragon.getDeliveryCycle(), DeliveryCycle.class);
        Date sendTime = null;
        Date cutTime = null;
        if (null == deliveryCycle) {
            if (StringUtils.isNotBlank(dragon.getSendTime())) {
                sendTime = SDF.parse(dragon.getSendTime());
            }
            if (StringUtils.isNotBlank(dragon.getEndTime())) {
                cutTime = SDF.parse(dragon.getEndTime());
            }
        } else {

            if (DeliveryCycleEnum.NOT_DELIVERY_CYCLE.getCode().equals(deliveryCycle.getCycleType())) {
                //一次性发货
                if (StringUtils.isNotBlank(dragon.getSendTime())) {
                    sendTime = SDF.parse(dragon.getSendTime());
                }
                if (StringUtils.isNotBlank(dragon.getEndTime())) {
                    cutTime = SDF.parse(dragon.getEndTime());
                }
            } else {
                //周期性发货
                sendTime = dragonService.getDragonDeliveryCycleDate(dragon.getDeliveryCycle(), DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME);
                cutTime = dragonService.getDragonDeliveryCycleDate(dragon.getDeliveryCycle(), DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME);
            }
        }
        map.put("sendTime", sendTime);
        map.put("cutTime", cutTime);
        return map;
    }

    /**
     * <p> 本期接龙详情————个人订单统计 </p>
     *
     * @param request
     * @param result
     * @return DragonOrderCountResponse
     * @author 昊天
     * @date 2019/2/19 10:18 AM
     * @since V1.1.0-SNAPSHOT
     */
    @PostMapping("OrderCount")
    public DragonOrderCountResponse orderCount(@RequestBody DragonOrderCountRequest request, BindingResult result) {
        DragonOrderCountResponse response = new DragonOrderCountResponse();
        if (result.hasErrors()) {
            //参数绑定错误
            response.build(RespStatus.ILLEGAL_ARGUMENT);
            return response;
        }
        WxUser user = wxUserService.getUserBySessionId(request.getSessionId());
        if (Objects.isNull(user)) {
            response.build(RespStatus.SESSION_ID_EXPIRE);
            return response;
        }
        PageHelper.startPage(request.getPageNo(), request.getPageSize());
        List<DragonOrderCount> orderCountList = dragonService.queryDragonOrderCount(request.getDragonNum());
        if (CollectionUtils.isNotEmpty(orderCountList)) {
            //地址处理
            orderCountList.forEach(dragonOrderCount -> {
                if (DragonDeliveryEnum.EXPRESS_DELIVERY.getCode().equals(dragonOrderCount.getIsDelivery())
                        || DragonDeliveryEnum.HOME_DELIVERY_SERVICE.getCode().equals(dragonOrderCount.getIsDelivery())) {
                    dragonOrderCount.setDeliveryAddr(dragonOrderCount.getAddress());
                }
                List<OrderGoodsInfo> orderGoods = orderGoodsService.getOrderGoods(dragonOrderCount.getOrderNum());
                List<DragonOrderCountGood> list = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(orderGoods)) {
                    orderGoods.forEach(orderGoodsInfo -> {
                        DragonOrderCountGood good = new DragonOrderCountGood();
                        good.setName(orderGoodsInfo.getGoodsName());
                        good.setBuyNumber(orderGoodsInfo.getBuyNumber());
                        list.add(good);
                    });
                }
                dragonOrderCount.setGoodsList(list);
            });
        }
        response.setDragonOrderCounts(orderCountList);
        return response;
    }

    /**
     * <p> 复制下单用户列表 </p>
     *
     * @param request
     * @return CopyNameResponse
     * @author 昊天
     * @date 2019/2/19 11:07 AM
     * @since V1.1.0-SNAPSHOT
     */
    @PostMapping("/CopyName")
    public CopyNameResponse copyName(@RequestBody DragonOrderCountRequest request) throws ParseException {
        CopyNameResponse response = new CopyNameResponse();
        WxUser user = wxUserService.getUserBySessionId(request.getSessionId());
        if (Objects.isNull(user)) {
            response.build(RespStatus.SESSION_ID_EXPIRE);
            return response;
        }
        DragonInfo dragon = dragonService.getDragon(request.getDragonNum());
        //设置发货时间
        Map<String, Date> time = this.getTimeFromDragon(dragon);
        response.setSendTime(time.get("sendTime"));

        List<DragonOrderCount> orderCountList = dragonService.queryDragonOrderCount(request.getDragonNum());
        if (CollectionUtils.isEmpty(orderCountList)) {
            response.setUserName(null);
        } else {
            StringBuilder str = new StringBuilder();
            Set<String> set = new HashSet<>();
            for (DragonOrderCount count : orderCountList) {
                //去重
                set.add(count.getNickName());
            }
            for (String s : set) {
                str.append(s).append(",");
            }
            //删除最后一个逗号
            response.setUserName(str.deleteCharAt(str.length() - 1).toString());
        }
        return response;
    }

    /**
     * <p> 统计下单的商品 </p>
     *
     * @param request
     * @return DragonOrderCountGoodsResponse
     * @author 昊天
     * @date 2019/2/19 1:34 PM
     * @since V1.1.0-SNAPSHOT
     */
    @PostMapping("/CountGoods")
    public DragonOrderCountGoodsResponse countGoods(@RequestBody DragonOrderCountRequest request) {
        DragonOrderCountGoodsResponse response = new DragonOrderCountGoodsResponse();
        WxUser user = wxUserService.getUserBySessionId(request.getSessionId());
        if (Objects.isNull(user)) {
            response.build(RespStatus.SESSION_ID_EXPIRE);
            return response;
        }
        List<DragonOrderCount> orderCountList = dragonService.queryDragonOrderCount(request.getDragonNum());
        List<DragonOrderCountGood> goodsList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(orderCountList)) {
            //为空返回空数组
            response.setGoodsList(goodsList);
            return response;
        }
        for (DragonOrderCount count : orderCountList) {
            List<OrderGoodsInfo> orderGoods = orderGoodsService.getOrderGoods(count.getOrderNum());
            if (CollectionUtils.isNotEmpty(orderGoods)) {
                for (OrderGoodsInfo info : orderGoods) {
                    DragonOrderCountGood good = new DragonOrderCountGood();
                    good.setName(info.getGoodsName());
                    good.setBuyNumber(info.getBuyNumber());
                    goodsList.add(good);
                }
            }
        }
        //按照名称分组
        Map<String, List<DragonOrderCountGood>> listMap = goodsList.stream().collect(Collectors.groupingBy(DragonOrderCountGood::getName));
        List<DragonOrderCountGood> realList = Lists.newArrayList();
        for (String name : listMap.keySet()) {
            //统计总数
            Optional<Integer> reduce = listMap.get(name).stream().map(DragonOrderCountGood::getBuyNumber).reduce((a, b) -> a + b);
            DragonOrderCountGood good = new DragonOrderCountGood();
            good.setName(name);
            good.setBuyNumber(reduce.get());
            realList.add(good);
        }

        //按购买数量排序
        List<DragonOrderCountGood> list = realList.stream().sorted(Comparator.comparing(DragonOrderCountGood::getBuyNumber).reversed()).collect(Collectors.toList());
        response.setGoodsList(list);
        return response;
    }

}
