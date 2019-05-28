package com.mhc.yunxian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.index.DragonInfoVO;
import com.mhc.yunxian.bean.index.GetIndexDragonListRequest;
import com.mhc.yunxian.bean.index.GetIndexDragonListResponse;
import com.mhc.yunxian.bean.index.SellerInfo;
import com.mhc.yunxian.bean.request.param.DragonDetailRequestParam;
import com.mhc.yunxian.bean.response.DragonDetailRepurchaseVO;
import com.mhc.yunxian.cache.RemoteCache;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.*;
import com.mhc.yunxian.dao.model.*;
import com.mhc.yunxian.dao.query.DragonInfoQuery;
import com.mhc.yunxian.enums.*;
import com.mhc.yunxian.exception.DataException;
import com.mhc.yunxian.service.*;
import com.mhc.yunxian.utils.DateUtils;
import com.mhc.yunxian.utils.JsonUtils;
import com.mhc.yunxian.utils.KeyTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class DragonServiceImpl implements DragonService {


    @Autowired
    DragonInfoDao dragonDao;

    @Autowired
    GoodsInfoDao goodsInfoDao;

    @Autowired
    DragonGoodsDao dragonGoodsDao;

    @Autowired
    PartyInfoDao partyInfoDao;

    @Autowired
    RemoteCache remoteCache;

    @Autowired
    BannerDao bannerDao;

    @Autowired
    BrowseRecordDao browseRecordDao;

    @Autowired
    OrderInfoDao orderInfoDao;

    @Autowired
    WxUserDao wxUserDao;

    @Autowired
    OrderService orderService;

    @Autowired
    DragonDraftDao dragonDraftDao;

    @Autowired
    DragonAddrDao dragonAddrDao;

    @Autowired
    AddrToDragonDao addrToDragonDao;

    @Autowired
    private Environment environment;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private DragonShowService dragonShowService;
    @Autowired
    private OrderGoodsService orderGoodsService;
    @Autowired
    private OrderGoodsDao orderGoodsDao;
    @Autowired
    private StatisticsService statisticsService;

    @Override
    public List<DragonInfo> getAllDragon(Integer pageNum) {
        return dragonDao.selectAllDragon(pageNum);
    }

    @Override
    public List<DragonInfo> getAllDragonInfo() {
        return dragonDao.selectAllDragonInfo();
    }


    @Override
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public void addInfoV3(CreateDragonRequest request) {


        final DragonInfo dragonInfo = new DragonInfo();

        dragonInfo.setOpenid(request.getOpenid());
        dragonInfo.setCashOnDelivery(request.getIsCOD());
        dragonInfo.setDragonDesc(request.getRemark());
        dragonInfo.setDragonImg(request.getDragonImage());
        dragonInfo.setDragonNum(request.getDragonNum());
        dragonInfo.setDragonTitle(request.getTitle());
        //0:进行中，1：已结束
        dragonInfo.setDragonStatus(0);
        dragonInfo.setIsPayLater(request.getIsPayLater());
        dragonInfo.setIsDelivery(request.getIsDelivery());
        dragonInfo.setGlobalLimit(request.getGlobalLimit());

        dragonInfo.setEndTime(DateUtils.timeStamp2Date(request.getEndTime()));
        dragonInfo.setSendTime(DateUtils.timeStamp2Date(request.getSendTime()));

        /**
         * 新增字段
         * 1.dragonVideo 接龙视频url
         * 2.cutOffTime 截单时间: 一次性发货时,接龙结束时间为截单时间
         * 3.deliveryCycle  发货周期
         */
        dragonInfo.setDragonVideo(request.getDragonVideo());
        dragonInfo.setCutOffTime(request.getCutOffTime());
        dragonInfo.setDeliveryCycle(JsonUtils.toJson(request.getDeliveryCycle()));
        dragonInfo.setCreateTime(new Date());
        dragonInfo.setPhone(request.getPhone());

        final PartInfo partInfo = new PartInfo();
        partInfo.setCreateTime(new Date());
        partInfo.setDragonNum(request.getDragonNum());
        partInfo.setOpenid(request.getOpenid());
        //2:创建者，1:参与者
        partInfo.setUserType(2);

        //插入参与者表
        if (partyInfoDao.insertParty(partInfo) < 1) {
            throw new DataException("插入参与者信息失败");
        }

        //插入接龙信息表
        if (dragonDao.insertDragon(dragonInfo) < 1) {
            throw new DataException("插入接龙信息失败");
        }

        if (null == request.getData()) {
            throw new DataException("商品信息为空");
        }

        for (Goods goods : request.getData()) {
            GoodsInfo goodsInfo = new GoodsInfo();

            goodsInfo.setGoodsName(goods.getGoodsName());
            goodsInfo.setGoodsImg(goods.getGoodsImgs());
            goodsInfo.setPrice(goods.getPrice());
            goodsInfo.setTotalNumber(goods.getTotalNumber());
            goodsInfo.setSpecification(goods.getSpecification());

            if (StringUtils.isNotBlank(goods.getGoodsNum())) {
                goodsInfo = goodsInfoDao.selectGoods(goods.getGoodsNum());

                goodsInfo.setGoodsName(goods.getGoodsName());
                goodsInfo.setGoodsImg(goods.getGoodsImgs());
                goodsInfo.setPrice(goods.getPrice());
                goodsInfo.setTotalNumber(goods.getTotalNumber());
                goodsInfo.setSpecification(goods.getSpecification());
                goodsInfo.setUpdateTime(new Date());


                //更新商品信息表
                if (goodsInfoDao.updateGoods(goodsInfo) < 1) {
                    throw new DataException("更新商品信息失败");
                }
            } else {
                goodsInfo.setGoodsNum(KeyTool.createOrderNo());
                goodsInfo.setCreateTime(new Date());
                //插入商品信息表
                if (goodsInfoDao.insertGoods(goodsInfo) < 1) {
                    throw new DataException("插入商品信息失败");
                }
            }


            DragonGoods dragonGoods = new DragonGoods();
            dragonGoods.setGoodsNum(goodsInfo.getGoodsNum());
            dragonGoods.setDragonNum(request.getDragonNum());
            dragonGoods.setLimitBuyNum(goods.getLimitBuyNum());
            dragonGoods.setCreateTime(new Date());
            /**
             *	商品sku库存数(商品表TotalNumber与接龙商品表CurrentNumber数据一致)
             *	校验库存时校验接龙商品表	CurrentNumber
             */
            dragonGoods.setCurrentNumber(goodsInfo.getTotalNumber());
            dragonGoods.setCurrentPrice(goodsInfo.getPrice());
            dragonGoods.setGoodsName(goodsInfo.getGoodsName());
            dragonGoods.setGoodsImg(goodsInfo.getGoodsImg());
            dragonGoods.setSpecification(goodsInfo.getSpecification());

            //插入接龙商品关联表
            if (dragonGoodsDao.insertSelective(dragonGoods) < 1) {
                throw new DataException("插入接龙商品关联信息失败");
            }

        }


        for (Integer addrId : request.getDragonAddrIds()) {

            AddrToDragon addrToDragon = new AddrToDragon();

            addrToDragon.setAddrId(addrId);
            addrToDragon.setDragonNum(request.getDragonNum());

            if (addrToDragonDao.insertSelective(addrToDragon) < 1) {
                throw new DataException("插入接龙自提地址信息失败");
            }

        }

        //删除草稿
        try {
            dragonDraftDao.updateStatusByOpenid(request.getOpenid());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    //@Cacheable(value = RedisKeys.DRAGON_KEY,key = "'yunxian:dragon:id:'+#dragonNum",sync = true)
    public DragonInfo getDragon(String dragonNum) {
        return dragonDao.selectDragon(dragonNum);
    }

    @Override
    // @Cacheable(value = RedisKeys.DRAGON_KEY,key = "'yunxian:dragon:openid:'+#openid",sync = true)
    public List<DragonInfo> getDragonByOpenid(String openid) {
        return dragonDao.selectDragonByOpenid(openid);
    }

    /**
     * 更新接龙V3
     *
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = {DataException.class, Exception.class})
    public void updateDragonV3(UpdateDragonRequest request) {

        //删除商品接龙表商品
        List<DragonGoods> list = dragonGoodsDao.select(request.getDragonNum());
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(s -> {
                if (s != null) {
                    s.setIsHidden((byte) 1);
                    dragonGoodsDao.updateByPrimaryKeySelective(s);
                }
            });
        }

        final DragonInfo dragonInfo = new DragonInfo();

        DragonInfo oldDragon = dragonDao.selectDragon(request.getDragonNum());

        if (oldDragon == null) {
            log.error("原接龙不存在");
            throw new DataException("原接龙不存在");
        }

        dragonInfo.setOpenid(request.getOpenid());
        dragonInfo.setCashOnDelivery(request.getIsCOD());
        dragonInfo.setDragonDesc(request.getRemark());
        dragonInfo.setDragonImg(request.getDragonImage());
        dragonInfo.setDragonNum(request.getDragonNum());
        dragonInfo.setDragonTitle(request.getTitle());
        dragonInfo.setDragonStatus(0);//0:进行中，1：已结束
        dragonInfo.setUpdateTime(new Date());
        dragonInfo.setPhone(request.getPhone());
        dragonInfo.setIsPayLater(request.getIsPayLater());
        dragonInfo.setIsDelivery(request.getIsDelivery());
        dragonInfo.setGlobalLimit(request.getGlobalLimit());

        dragonInfo.setEndTime(DateUtils.timeStamp2Date(request.getEndTime()));
        dragonInfo.setSendTime(DateUtils.timeStamp2Date(request.getSendTime()));
        /**
         * 周期发货时间对象
         */
        dragonInfo.setDeliveryCycle(JsonUtils.toJson(request.getDeliveryCycle()));
        dragonInfo.setCutOffTime(request.getCutOffTime());
        dragonInfo.setDragonVideo(request.getDragonVideo());
        //插入接龙信息表

        if (dragonDao.updateDragon(dragonInfo) < 1) {
            log.error("插入商接龙信息失败");
            throw new DataException("插入商接龙信息失败");
        }

        for (Goods goods : request.getData()) {

            //商品信息表
            GoodsInfo goodsInfo = new GoodsInfo();
            goodsInfo.setGoodsName(goods.getGoodsName());
            goodsInfo.setGoodsImg(goods.getGoodsImgs());
            goodsInfo.setPrice(goods.getPrice());
            goodsInfo.setTotalNumber(goods.getTotalNumber());
            goodsInfo.setSpecification(goods.getSpecification());

            if (StringUtils.isNotBlank(goods.getGoodsNum())) {

                goodsInfo = goodsInfoDao.selectGoods(goods.getGoodsNum());

                goodsInfo.setGoodsName(goods.getGoodsName());
                goodsInfo.setGoodsImg(goods.getGoodsImgs());
                goodsInfo.setPrice(goods.getPrice());
                goodsInfo.setTotalNumber(goods.getTotalNumber());
                goodsInfo.setSpecification(goods.getSpecification());
                goodsInfo.setUpdateTime(new Date());

                //更新商品信息表
                if (goodsInfoDao.updateGoods(goodsInfo) < 1) {
                    log.error("更新商品信息失败");
                    throw new DataException("更新商品信息失败");
                }
            } else {
                goodsInfo.setGoodsNum(KeyTool.createOrderNo());
                goodsInfo.setCreateTime(new Date());

                //插入商品信息表
                if (goodsInfoDao.insertGoods(goodsInfo) < 1) {
                    log.error("插入商品信息失败");
                    throw new DataException("插入商品信息失败");
                }
            }

            DragonGoods dragonGoods = dragonGoodsDao.selectByGoodsNumAndDragonNum(request.getDragonNum(), goodsInfo.getGoodsNum());

            if (dragonGoods == null) {

                //接龙商品关联表
                dragonGoods = new DragonGoods();
                dragonGoods.setDragonNum(request.getDragonNum());
                dragonGoods.setGoodsNum(goodsInfo.getGoodsNum());
                dragonGoods.setCreateTime(new Date());
                dragonGoods.setLimitBuyNum(goods.getLimitBuyNum());
                dragonGoods.setCurrentNumber(goodsInfo.getTotalNumber());
                dragonGoods.setCurrentPrice(goodsInfo.getPrice());
                dragonGoods.setGoodsName(goodsInfo.getGoodsName());
                dragonGoods.setGoodsImg(goodsInfo.getGoodsImg());
                dragonGoods.setSpecification(goodsInfo.getSpecification());
                //插入接龙商品关联表
                if (dragonGoodsDao.insertSelective(dragonGoods) < 1) {
                    log.error("插入接龙商品信息失败");
                    throw new DataException("插入接龙商品信息失败");
                }
            } else {
                //接龙商品关联表
                dragonGoods.setDragonNum(request.getDragonNum());
                dragonGoods.setGoodsNum(goodsInfo.getGoodsNum());
                dragonGoods.setCreateTime(new Date());
                dragonGoods.setLimitBuyNum(goods.getLimitBuyNum());
                dragonGoods.setCurrentNumber(goodsInfo.getTotalNumber());
                dragonGoods.setCurrentPrice(goodsInfo.getPrice());
                dragonGoods.setGoodsName(goodsInfo.getGoodsName());
                dragonGoods.setGoodsImg(goodsInfo.getGoodsImg());
                dragonGoods.setSpecification(goodsInfo.getSpecification());
                //插入接龙商品关联表
                if (dragonGoodsDao.updateByPrimaryKeySelective(dragonGoods) < 1) {
                    log.error("更新接龙商品信息失败");
                    throw new DataException("更新接龙商品信息失败");
                }
            }


            //删除原先接龙地址
            try {

                addrToDragonDao.deleteByDragonNum(request.getDragonNum());

            } catch (Exception e) {
                log.error("删除接龙自提地址失败" + e);
                throw new DataException("删除接龙自提地址失败");

            }

            for (Integer addrId : request.getDragonAddrIds()) {

                AddrToDragon addrToDragon = new AddrToDragon();

                addrToDragon.setAddrId(addrId);
                addrToDragon.setDragonNum(request.getDragonNum());

                if (addrToDragonDao.insertSelective(addrToDragon) < 1) {
                    log.error("插入接龙自提地址信息失败");
                    throw new DataException("插入接龙自提地址信息失败");
                }

            }

        }
    }

    @Override
    public boolean updateDragonStatus(DragonInfo info) {
        return dragonDao.updateDragon(info) > 0;
    }

    @Override
    public boolean updateDragonEndTime(UpdateDragonRequest request) {

        DragonInfo dragonInfo = dragonDao.selectDragon(request.getDragonNum());

        if (null == dragonInfo || !dragonInfo.getOpenid().equals(request.getOpenid())) {
            return false;
        }

        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);

        dragonInfo.setEndTime(dateString);
        dragonInfo.setDragonStatus(1);


        return dragonDao.updateDragon(dragonInfo) > 0;

    }

    @Override
    public List<Banner> getBroadcastImg() {
        return bannerDao.getBroadcastImg();
    }

    @Override
    public List<DragonInfo> getAllDragonWithEndTime() {
        return dragonDao.getAllDragonWithEndTime();
    }

    @Override
    public DragonInfo getParentDragon(String dragonNum) {
        return dragonDao.selectDragonBySubDragonNum(dragonNum);
    }

    @Override
    public List<DragonInfo> selectDragonByStatusAndOpenid(DragonInfo dragonInfo) {
        return dragonDao.selectDragonByStatusAndOpenid(dragonInfo);
    }

    @Override

    public void addDragonBrowseRecord(BrowseRecord browseRecord) {

        if (browseRecordDao.insertSelective(browseRecord) < 1) {
            throw new DataException("插入接龙浏览记录失败");
        }
    }

    @Override
    public List<BrowseRecord> findTodayRecord(BrowseRecord browseRecord) {
        return browseRecordDao.findTodayRecord(browseRecord);
    }

    @Override
    public boolean updateBrowseRecord(BrowseRecord browseRecord) {

        if (browseRecordDao.updateByPrimaryKeySelective(browseRecord) < 1) {
            throw new DataException("更新接龙浏览记录失败");
        }

        return true;
    }

//    }

    @Override
    public int countSellerNumByTime(OperationStatisticsRequest request) {
        return dragonDao.countSellerNumByTime(request);
    }

    //    @Override
//    public List<BrowseRecord> findDragonBrowseRecord(BrowseRecord browseRecord){
//


    @Override
    public GetIndexDragonListResponse getAllDragonInfoV7(GetIndexDragonListRequest request) {
        GetIndexDragonListResponse response = new GetIndexDragonListResponse();

        List<String> userOpenidList = new ArrayList<>();

        String port = environment.getProperty("server.port");
        Integer show = 0;
        if (port.equals("8080")) {
            //product版
            show = dragonShowService.selectDragonShow(VersionEnum.PRODUCT.getCode());
        }
        if (port.equals("8889")) {
            //release版
            show = dragonShowService.selectDragonShow(VersionEnum.RELEASE.getCode());
        }

        if (request.getSessionId() != null) {
            WxUser user = wxUserDao.getUserBySessionId(request.getSessionId());
            if (user == null) {
                return (GetIndexDragonListResponse) response.build(RespStatus.SESSION_ID_EXPIRE);
            }
            PageHelper.startPage(1, 1);
            List<String> browseOpenidList = browseRecordDao.selectSellerOpenid(user.getId());

            if (browseOpenidList.size() < 1) {
                if (request.getPage() == 1) {
                    PageHelper.startPage(1, 1);
                    if (show == 1) {
                        userOpenidList.add("ojzCP4nWQW3muiO_pcn7j6K1tJ5U");
                    } else {
                        //按接龙发布顺序倒序找出卖家
                        List<String> sellerOpenidList = dragonDao.selectAllDragonOpenid();
                        userOpenidList.addAll(sellerOpenidList);
                    }

                }
            } else {

                if (show == 1) {
                    userOpenidList.add("ojzCP4nWQW3muiO_pcn7j6K1tJ5U");

                } else {
                    PageHelper.startPage(request.getPage(), request.getSize());
                    browseOpenidList = browseRecordDao.selectSellerOpenid(user.getId());
                    //在浏览表中找出买家浏览过的卖家
                    userOpenidList.addAll(browseOpenidList);
                }

            }
        } else {
            if (show == 1) {
                userOpenidList.add("ojzCP4nWQW3muiO_pcn7j6K1tJ5U");
            } else {
                PageHelper.startPage(request.getPage(), request.getSize());
                //按接龙发布顺序倒序找出卖家
                List<String> sellerOpenidList = dragonDao.selectAllDragonOpenid();
                userOpenidList.addAll(sellerOpenidList);
            }

        }

        //去重
        List<String> finalUserOpenidList = new ArrayList<>();
        for (int i = 0; i < userOpenidList.size(); i++) {
            if (!finalUserOpenidList.contains(userOpenidList.get(i))) {
                finalUserOpenidList.add(userOpenidList.get(i));
            }
        }

        if (finalUserOpenidList.size() < 1) {
            response.setSellerInfos(new ArrayList<>());
            return response;

        }
        //根据分页参数找出返回的卖家


        List<WxUser> wxUsers = wxUserDao.selectByOpenidList(finalUserOpenidList);

        if (request.getState() == 1 && request.getPage() == 1) {
            wxUsers.clear();
            wxUsers.add(wxUserDao.getUserBySessionId(request.getSessionId()));
        } else if (request.getState() == 1 && request.getPage() != 1) {
            wxUsers.clear();
        }


        List<SellerInfo> sellerInfos = new ArrayList<>();
        for (WxUser wxUser : wxUsers) {
            SellerInfo sellerInfo = new SellerInfo();

            sellerInfo.setHead(wxUser.getHeadImgUrl());
            sellerInfo.setNickName(wxUser.getNickName());
            sellerInfo.setOrderNumber(wxUser.getOrderNumber());

            PageHelper.startPage(1, request.getDragonSize());
            List<DragonInfo> dragonInfos = dragonDao.selectNoEndDragonByOpenid(wxUser.getOpenid());

            List<DragonInfoVO> dragonInfoVOS = new ArrayList<>();

            for (DragonInfo dragonInfo : dragonInfos) {
                DragonInfoVO dragonInfoVO = new DragonInfoVO();


                List<DragonAddr> dragonAddrList = this.selectByDragonNum(dragonInfo.getDragonNum());

                if (dragonAddrList.size() < 1 && dragonInfo.getIsDelivery() != 1) {
                    List<String> addrs = Arrays.asList(dragonInfo.getAddr().split("&"));

                    for (String addr : addrs) {

                        DragonAddr dragonAddr = new DragonAddr();

                        dragonAddr.setAddr(addr);

                        dragonAddrList.add(dragonAddr);
                    }

                }

                dragonInfoVO.setDragonAddrs(dragonAddrList);


                dragonInfoVO.setIsDelivery(dragonInfo.getIsDelivery());
                dragonInfoVO.setCreateTime(dragonInfo.getCreateTime());
                dragonInfoVO.setDragonImg(dragonInfo.getDragonImg());
                dragonInfoVO.setDragonNum(dragonInfo.getDragonNum());


                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                /*Date date = null;
                try {
                    date = simpleDateFormat.parse(dragonInfo.getEndTime());
                } catch (ParseException e) {
                    log.error("接龙详情结束时间有误:" + e);
                    return (GetIndexDragonListResponse) response.build(RespStatus.SYSTEM_ERROR);
                }*/
                dragonInfoVO.setEndTime(dragonInfo.getEndTime());
                dragonInfoVO.setTitle(dragonInfo.getDragonTitle());
                dragonInfoVO.setRemark(dragonInfo.getDragonDesc());

                List<OrderInfo> orderInfos = orderInfoDao.selectOrderBySellerIdIndex(dragonInfo.getDragonNum());

                List<String> headImgs = new ArrayList<>();

                int flag = 0;

                int partyNumber = 0;

                for (OrderInfo orderInfo : orderInfos) {
                    if (orderInfo.getParentOrderNum() != null) {
                        continue;
                    }

                    if (flag <= 10) {
                        WxUser buyer = wxUserDao.selectByOpenid(orderInfo.getOpenid());
                        if (buyer == null) {
                            return (GetIndexDragonListResponse) response.build(RespStatus.USER_NOT_EXIST);
                        }
                        headImgs.add(buyer.getHeadImgUrl());
                        flag++;
                    }

                    partyNumber++;

                }

                dragonInfoVO.setPartyNumber(partyNumber);

                dragonInfoVO.setHeadImg(headImgs);

                dragonInfoVO.setOrderNumber(orderService.countOrderNumByDragon(dragonInfo.getDragonNum()));

                dragonInfoVOS.add(dragonInfoVO);
            }

            sellerInfo.setDragonInfos(dragonInfoVOS);

            sellerInfos.add(sellerInfo);
        }

        response.setSellerInfos(sellerInfos);

        response.setTotalPartyNumber(orderInfoDao.selectAllOrderIndex());

        response.setTotalNumber(dragonDao.selectAllDragonIndex());

        return response;
    }

    @Override
    public void updateDraft(DragonDraft dragonDraft) {

        int i = dragonDraftDao.updateStatusByOpenid(dragonDraft.getOpenid());
        if (i < 1) {
            log.error("更新数据失败!");
        }
        dragonDraft.setId(null);
        int effect = dragonDraftDao.insertSelective(dragonDraft);
        if (effect < 1) {
            log.error("插入数据失败!");
        }
    }

    @Override
    public DragonDraft getDraft(String openid) {
        return dragonDraftDao.selectByOpenid(openid);
    }

    @Override
    public void deleteDraft(String openid) {
        dragonDraftDao.updateStatusByOpenid(openid);
    }


    @Override
    public List<DragonAddr> selectByDragonNum(String dragonNum) {
        return dragonAddrDao.selectByDragonNum(dragonNum);
    }

    @Override
    public void addDragonAddr(AddDragonAddrRequest request) {
        DragonAddr dragonAddr = new DragonAddr();

        dragonAddr.setName(request.getName());
        dragonAddr.setAddr(request.getAddr());
        dragonAddr.setLongitude(request.getLongitude());
        dragonAddr.setLatitude(request.getLatitude());
        dragonAddr.setOpenid(request.getOpenid());
        dragonAddr.setCreateTime(new Date());
        dragonAddr.setDetailAddr(request.getDetailAddr());

        dragonAddrDao.insertSelective(dragonAddr);

    }

    /**
     * 接龙详情-复购名单-复购商品-复购次数  老接口 /yunxian/dragonHistory
     *
     * @param param
     * @return
     */
    @Autowired
    private GoodsInfoService goodsInfoService;

    @Override
    public List<DragonDetailRepurchaseVO> dragonDetailRepurchaseInfo(DragonDetailRequestParam param) {
        //  查询接龙所有的商品
        List<DragonGoods> dragonGoods = dragonGoodsDao.select(param.getDragonNum());
        List<DragonDetailRepurchaseVO> list = new ArrayList<>();
        for (DragonGoods goods : dragonGoods) {
            //过滤已删除商品
            GoodsInfo good = goodsInfoService.getGoods(goods.getGoodsNum());
            if (null == good || good.getDeleted()) {
                continue;
            }
            //查询出订单商品列表
            List<OrderGoodsInfo> orderGoodsInfoList = orderGoodsDao.selectOrderGoodsByGoodsNum(good.getGoodsNum());
            if (CollectionUtils.isEmpty(orderGoodsInfoList)) {
                //如果该商品没有被购买过则过滤
                continue;
            }
            //过滤不满足的订单
            List<OrderGoodsInfo> orderGoodsList = orderGoodsInfoList.stream().filter(x -> {
                OrderInfo order = orderService.getOrderByOrderNum(x.getOrderNum());
                if (OrderStatusEnum.PENDING_PAYMENT.getCode().equals(order.getOrderStatus())
                        || OrderStatusEnum.REFUNDED.getCode().equals(order.getOrderStatus())
                        || OrderStatusEnum.CANCELLED.getCode().equals(order.getOrderStatus())) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());

            //按买家openId分组
            Map<String, List<OrderGoodsInfo>> orderGoodsMap = orderGoodsList.stream().collect(Collectors.groupingBy(OrderGoodsInfo::getOpenid));
            for (String key : orderGoodsMap.keySet()) {
                Integer repurchase = orderGoodsMap.get(key).size() - 1;
                if (repurchase > 0) {
                    DragonDetailRepurchaseVO repurchaseResponse = new DragonDetailRepurchaseVO();
                    repurchaseResponse.setGoodsName(good.getGoodsName());
                    repurchaseResponse.setGoodsNum(good.getGoodsNum());
                    WxUser buyer = wxUserDao.selectByOpenid(key);
                    repurchaseResponse.setUserName(buyer.getNickName());
                    repurchaseResponse.setHeadPicture(buyer.getHeadImgUrl());
                    repurchaseResponse.setRepurchaseCount(repurchase);
                    list.add(repurchaseResponse);
                }
            }
        }
        List<DragonDetailRepurchaseVO> finalGoodsList = list.stream().sorted(Comparator.comparing(DragonDetailRepurchaseVO::getRepurchaseCount).reversed()).collect(Collectors.toList());
        return finalGoodsList;
    }

    @Override
    public BaseResponse updownDragon(String dragonNum, String sessionId, Integer state) {
        if (StringUtils.isBlank(dragonNum) || StringUtils.isBlank(sessionId) || state == null) {
            throw new DataException("参数不能为空");
        }
        if (wxUserDao.getUserBySessionId(sessionId) == null) {
            throw new DataException("用户不存在");
        }
        if (state > 1 || state < 0) {
            throw new DataException("参数错误");
        }
        DragonInfo dragonInfo = dragonDao.selectDragon(dragonNum);
        if (dragonInfo == null) {
            throw new DataException("接龙不存在");
        }
        DragonInfo info = new DragonInfo();
        info.setDragonStatus(state);
        info.setDragonNum(dragonNum);
        dragonDao.updateDragon(info);
        return new BaseResponse();
    }

    @Override
    public List<String> selectLatestThreeShopOfDragon() {
        return dragonDao.selectLatestThreeShopOfDragon();
    }

//    @Override
//    public DragonInfo findByOpenIdAndGoodsNum(String openid, String goodsNum) {
//        return dragonDao.findByOpenIdAndGoodsNum(openid,goodsNum);;
//    }

    @Override
    public List<DragonAddr> getAllDragonAddr(DragonAddrRequest request) {

        PageHelper.startPage(request.getPage(), request.getSize());
        List<DragonAddr> dragonAddrs = dragonAddrDao.selectAllAddrByOpenid(request.getOpenid());
        List<DragonAddr> collect = dragonAddrs.stream().
                filter(dragonAddr -> !Optional.ofNullable(dragonAddr.getDeleted()).orElse(Boolean.FALSE))
                .collect(Collectors.toList());
        return collect;

    }

    /**
     * 获取周期性的发货最近一期发货或截单时间
     *
     * @param cycle
     * @param dragonDateTypeEnum
     */
    @Override
    public Date getDragonDeliveryCycleDate(String cycle, DragonDateTypeEnum dragonDateTypeEnum) {
        Date time = null;
        try {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(cycle)) {
                DeliveryCycle deliveryCycle = JSONObject.parseObject(cycle, DeliveryCycle.class);
                switch (deliveryCycle.getCycleType()) {
                    case 0:
                        // 不是周期性发货
                        break;
                    case 1:
                        // 设置每天的时间
                        Calendar instance = Calendar.getInstance();
                        // instance.setTime(new Date());
                        instance.set(Calendar.MINUTE, 0);
                        instance.set(Calendar.SECOND, 0);
                        if (DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME.getCode().equals(dragonDateTypeEnum.getCode())) {
                            // 截单类型时间
                            instance.set(Calendar.HOUR_OF_DAY, deliveryCycle.getCutHour());

                            if (deliveryCycle.getCutHour() < -25) {
                                instance.set(Calendar.HOUR_OF_DAY, deliveryCycle.getCutHour() + 48);
                            } else if (deliveryCycle.getCutHour() < 0 && deliveryCycle.getCutHour() >= -25) {
                                instance.set(Calendar.HOUR_OF_DAY, deliveryCycle.getCutHour() + 24);
                            }

                            if (new Date().after(instance.getTime())) {
                                if (deliveryCycle.getCutHour() < -25) {
                                    instance.add(Calendar.DATE, 3);
                                } else {
                                    instance.add(Calendar.DATE, 1);
                                }
                                // instance.add(Calendar.DATE, 1);
                            }

                        } else {
                            Calendar cutTime = Calendar.getInstance();
                            cutTime.set(Calendar.MINUTE, 0);
                            cutTime.set(Calendar.SECOND, 0);

                            // 截单类型时间
                            cutTime.set(Calendar.HOUR_OF_DAY, deliveryCycle.getCutHour());

                            if (deliveryCycle.getCutHour() < -25) {
                                cutTime.set(Calendar.HOUR_OF_DAY, deliveryCycle.getCutHour() + 48);
                            } else if (deliveryCycle.getCutHour() < 0 && deliveryCycle.getCutHour() >= -25) {
                                cutTime.set(Calendar.HOUR_OF_DAY, deliveryCycle.getCutHour() + 24);
                            }

                            if (new Date().after(cutTime.getTime())) {
                                if (deliveryCycle.getCutHour() < -25) {
                                    cutTime.add(Calendar.DATE, 3);
                                } else if (deliveryCycle.getCutHour() < 0 && deliveryCycle.getCutHour() >= -25) {
                                    cutTime.add(Calendar.DATE, 1);
                                } else {
                                    cutTime.add(Calendar.DATE, 2);
                                }
                            }

                            instance.setTime(cutTime.getTime());

                            if (deliveryCycle.getCutHour() < 0) {
                                int hour = deliveryCycle.getHour() - deliveryCycle.getCutHour();
                                instance.add(Calendar.HOUR, hour);
                            } else {
                                int hour = deliveryCycle.getCutHour() - deliveryCycle.getHour();
                                instance.add(Calendar.HOUR, hour);
                            }

                            // 发货类型时间
                            instance.set(Calendar.HOUR_OF_DAY, deliveryCycle.getHour());

                            /*if (new Date().after(instance.getTime())) {
                                instance.add(Calendar.DATE, 1);
                            }*/
                        }
                        /*if (instance.getTime().before(new Date()) && deliveryCycle.getCutHour() <= 0) {
                            // 如果获得的时间在当前时间之前则需要往后推一天
                            instance.add(Calendar.DAY_OF_MONTH, 2);
                        }

                        if (instance.getTime().before(new Date()) && deliveryCycle.getCutHour() > 0) {
                            // 如果获得的时间在当前时间之前则需要往后推一天
                            instance.add(Calendar.DAY_OF_MONTH, 1);
                        }*/

                        time = instance.getTime();
                        break;
                    case 2:
                        // 每周几
                        // 周期截单时间
                        Integer[] dayOfWeek = deliveryCycle.getDayOfWeek();
                        if (DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME.getCode().equals(dragonDateTypeEnum.getCode())) {
                            // 计算本周截单时间列表
                            List<Date> dateList = this.getDateList(deliveryCycle.getCutHour(), dayOfWeek, DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME.getCode());
                            // 获取最近一期截单时间
                            time = this.getDate(dateList);
                        } else {
                            // 计算本周截单时间列表
                            List<Date> cutDateList = this.getDateList(deliveryCycle.getCutHour(), dayOfWeek, DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME.getCode());
                            // 获取最近一期截单时间
                            Date cutTime = this.getDate(cutDateList);

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(cutTime);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            int hour = 0;
                            if (deliveryCycle.getCutHour() < 0) {
                                hour = deliveryCycle.getHour() - deliveryCycle.getCutHour();
                            } else {
                                hour = deliveryCycle.getHour();
                            }
                            calendar.add(Calendar.HOUR, hour);
                            calendar.set(Calendar.HOUR_OF_DAY, deliveryCycle.getHour());

                            time = calendar.getTime();

                            // 计算本周的发货时间列表
                           /* List<Date> sendDataList = this.getDateList(deliveryCycle.getHour(), dayOfWeek, DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME.getCode());
                            // 获取最近一期发货时间
                            time = this.getDate(sendDataList);
                            final Date sendTime = time;
                            Date date = new Date();
                            if (date.after(cutTime) && date.before(sendTime)) {
                                List<Date> collect = sendDataList.stream().filter(e -> e.after(sendTime)).collect(Collectors.toList());
                                if (CollectionUtils.isEmpty(collect)) {
                                    time = sendDataList.get(0);
                                } else {
                                    time = collect.get(0);
                                }

                            }*/

                        }
                        break;
                    case 3:
                        // 需求被砍掉
                        break;
                    default:
                        break;
                }
                return time;
            }
        } catch (Exception e) {
            log.error("周期发货信息解析异常{}", e.getMessage());
        }
        return null;
    }

    /**
     * @param hour      截单时间或发货时间(单位:小时)
     * @param dayOfWeek 每周几发货的时间数组
     * @return
     */
    @Override
    public List<Date> getDateList(Integer hour, Integer[] dayOfWeek, Integer type) {
        List<Date> dateList = new ArrayList<>();
        for (Integer i : dayOfWeek) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, i);
            calendar.set(Calendar.HOUR_OF_DAY, Optional.ofNullable(hour).orElse(0));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            if (hour != null && hour < 0) {
                /**
                 * 当截单时间早于发货时间越过当天0点时,
                 * 计算每周截单时间时传入的hour可能为负数
                 * 此时表示的是截单时间早于发货时间倒推越过当天的情况
                 * hour=-1 表示前一天23点,以此类推
                 */
                if (hour > -25) {
                    calendar.add(Calendar.DAY_OF_MONTH, -2);
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, hour + 48);
                // calendar.add(Calendar.DATE, 1);

                if (calendar.getTime().before(new Date()) && i == 1) {
                    calendar.add(Calendar.DAY_OF_WEEK, 7);
                }

                if (DragonDateTypeEnum.DRAGON_DATE_TYPE_CUT_OFF_TIME.getCode().equals(type)) {
                    calendar.add(Calendar.DATE, 1);
                }

            } else if (hour > 0 && i == 1) {
                calendar.add(Calendar.DAY_OF_WEEK, 7);
            }
            // calendar.add(Calendar.DATE, 1);
            if (calendar.getTime().before(new Date()) && i == 1 && DragonDateTypeEnum.DRAGON_DATE_TYPE_DELIVERY_TIME.getCode().equals(type)) {
                calendar.add(Calendar.DAY_OF_WEEK, 7);
            }


            dateList.add(calendar.getTime());
        }
        return dateList;
    }

    /**
     * 返回周期性发货每周的下期配送或者截单时间
     *
     * @param dateList 每周截单或发货时间列表
     * @return 截单或发货时间
     */
    @Override
    public Date getDate(List<Date> dateList) {
        if (dateList.isEmpty()) {
            return null;
        }
        Date date;
        // 筛选出在当前时间之前的截单时间点
        List<Date> collect = dateList.stream().filter(ele -> ele.before(new Date()))
                .sorted(Comparator.comparing(Date::getTime))
                .collect(Collectors.toList());
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(collect)) {
            // 上期截单(或者发货)时间,取最后一个
            Date cutOffDate = collect.get(collect.size() - 1);
            //log.info("上期时间{}", DateUtil.SDF.format(cutOffDate));
        }

        // 当前时间点之后的截单(或者发货)时间列表
        List<Date> dates = dateList.stream().filter(cutDate -> cutDate.after(new Date())).collect(Collectors.toList());
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(dates)) {
            // 取当前时间点后最近的一个截单(或者发货)时间
            //log.info("取当前时间点后最近的一个时间{}", DateUtil.SDF.format(dates.get(0).getTime()));
            date = dates.get(0);
        } else {
            // 取截单(或者发货)时间列表的第一个
            Date time = dateList.get(0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            // 下周截单(或者发货)时间
            calendar.add(Calendar.DAY_OF_WEEK, 7);
            //log.info("下周时间{}", DateUtil.SDF.format(calendar.getTime()));
            date = calendar.getTime();
        }
        return date;
    }

    @Override
    public String getDragonDeliveryTime(String cycle) {
        try {
            DeliveryCycle deliveryCycle = JsonUtils.toObj(cycle, DeliveryCycle.class);
            // 每周定时截单
            Integer[] dayOfWeek = deliveryCycle.getDayOfWeek();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("每");
            // 计算每周的截单时间,每周日至周六用数组[1]-[7]表示
            for (Integer i : dayOfWeek) {
                switch (i) {
                    case 1:
                        stringBuilder.append("周日");
                        break;
                    case 2:
                        stringBuilder.append("周一");
                        break;
                    case 3:
                        stringBuilder.append("周二");
                        break;
                    case 4:
                        stringBuilder.append("周三");
                        break;
                    case 5:
                        stringBuilder.append("周四");
                        break;
                    case 6:
                        stringBuilder.append("周五");
                        break;
                    case 7:
                        stringBuilder.append("周六");
                        break;
                    default:
                        break;
                }
                stringBuilder.append(deliveryCycle.getHour()).append("点发货");
                return stringBuilder.toString();
            }
        } catch (Exception e) {
            log.error("json解析异常{}", e.getMessage());
        }

        return "";
    }

    @Override
    public List<FindDragon> getPartDragonUser(String dragonNum, Date cutOffTime) {
        // 获取接龙所有订单
        List<OrderInfo> orderInfos = orderInfoDao.selectOrder(dragonNum);
        List<FindDragon> findDragonList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(orderInfos)) {
            log.warn("未查询到该接龙的交易订单!接龙编号{}", dragonNum);
            return findDragonList;
        }
        if (cutOffTime != null) {
            orderInfos = orderInfos.stream().filter(orderInfo -> orderInfo.getCreateTime().after(cutOffTime)
                    && StringUtils.isBlank(orderInfo.getParentOrderNum())
            ).collect(Collectors.toList());
        }
        for (OrderInfo orderInfo : orderInfos) {
            if (orderInfo.getOrderStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getCode())
                    || orderInfo.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())
                    || orderInfo.getOrderStatus().equals(OrderStatusEnum.REFUNDED.getCode())
                    || orderInfo.getParentOrderNum() != null) {
                continue;
            }

            //获取订单用户信息
            final WxUser user = wxUserService.getWxUser(orderInfo.getOpenid());
            if (null == user) {
                log.warn("未找到该订单的用户信息!用户openID{}", orderInfo.getOpenid());
                return findDragonList;
            }
            final List<Goods> goodsList = Lists.newArrayList();
            //查询订单包括的所有商品
            final List<OrderGoodsInfo> orderGoodsInfoList = orderGoodsService.getOrderGoods(orderInfo.getOrderNum());
            if (null == orderGoodsInfoList || orderGoodsInfoList.size() == 0) {
                log.warn("未查询到订单包括的所有商品!订单编号{}", orderInfo.getOrderNum());
                return findDragonList;
            }
            for (OrderGoodsInfo oginfo : orderGoodsInfoList) {
                if (oginfo.getBuyNumber() == 0) {
                    continue;
                }
                final Goods goods = new Goods();
                // 购买数量
                goods.setTotalNumber(oginfo.getBuyNumber());
                // 商品名称
                goods.setGoodsName(oginfo.getGoodsName());
                goods.setGoodsImgs(oginfo.getGoodsImg());
                goodsList.add(goods);
            }
            final FindDragon findDragon = new FindDragon();
            findDragon.setHeadImg(user.getHeadImgUrl());
            findDragon.setNickName(user.getNickName());
            findDragon.setList(goodsList);
            if (StringUtils.isBlank(orderInfo.getDragonAddr())) {
                findDragon.setDragonAddr("快递配送");
            } else {
                findDragon.setDragonAddr(orderInfo.getDragonAddr());
            }
            findDragonList.add(findDragon);
        }
        // List<FindDragon> findDragonsUserInfo = Lists.newArrayList();
        /*for (FindDragon findDragon : findDragonList) {
            if (!findDragonsUserInfo.contains(findDragon)) {
                findDragonsUserInfo.add(findDragon);
            }
        }*/
        return findDragonList;
    }

    @Override
    public List<DragonInfo> queryLatestDragon(DragonInfoQuery dragonInfoQuery) {
        return dragonDao.queryLatestDragon(dragonInfoQuery);
    }

    @Override
    public List<DragonInfo> selectDragonByStatusAndCycle() {
        return dragonDao.selectDragonByStatusAndCycle();
    }

    @Override
    public List<DragonOrderCount> queryDragonOrderCount(String dragonNum) {
        return dragonDao.selectDragonOrderCountInfo(dragonNum);
    }
}
