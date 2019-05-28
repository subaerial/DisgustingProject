package com.mhc.yunxian.controller;

import com.github.pagehelper.PageHelper;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.Goods;
import com.mhc.yunxian.bean.request.param.GoodsRequestParam;
import com.mhc.yunxian.bean.request.query.ShopInfoQuery;
import com.mhc.yunxian.bean.response.GoodsInfoResponse;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.DragonGoodsDao;
import com.mhc.yunxian.dao.GoodsInfoDao;
import com.mhc.yunxian.dao.model.DragonGoods;
import com.mhc.yunxian.dao.model.GoodsInfo;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.enums.DifPriceEnum;
import com.mhc.yunxian.enums.NumEnum;
import com.mhc.yunxian.service.DragonGoodsService;
import com.mhc.yunxian.service.GoodsInfoService;
import com.mhc.yunxian.service.ShopService;
import com.mhc.yunxian.service.WxUserService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/12/9.
 *
 * @author Alin
 */
@Controller
@Api
@Slf4j
@RequestMapping("/goods")
public class GoodsInfoController {

    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private GoodsInfoService goodsInfoService;

    @Autowired
    private GoodsInfoDao goodsInfoDao;

    @Autowired
    private DragonGoodsDao dragonGoodsDao;

    @Autowired
    private DragonGoodsService dragonGoodsService;

    private static final int LENGTH = 12;

    /**
     * 判断字符串是否含有表情包emoji
     *
     * @param content
     * @return
     */
    public boolean hasEmoji(String content) {
        Pattern pattern = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return true;
        }
        return false;
    }


    /**
     * 商品管理-新增商品信息
     */
    @ResponseBody
    @ApiOperation(value = "/addGoodsInfo", notes = "商品管理-新增商品信息")
    @RequestMapping(value = "/addGoodsInfo", method = RequestMethod.POST)
    public BaseResponse addGoodsInfo(@RequestBody GoodsRequestParam param) {
        WxUser wxUser = wxUserService.getUserBySessionId(param.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        for (Goods goods : param.getGoods()) {
            if (null == goods.getPrice() || null == goods.getTotalNumber() || null == goods.getGoodsName() || null == goods.getSpecification()) {
                return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
            }
            if (StringUtils.isBlank(goods.getGoodsImgs())) {
                return new BaseResponse().build("图片不能为空");
            }
            //价格校验
            if (goods.getPrice() < DifPriceEnum.DIF_PRICE_MIN.getCode() || goods.getPrice() > DifPriceEnum.DIF_PRICE_MAX.getCode()) {
                return new BaseResponse().build(RespStatus.GOODS_PRICE_LIMIT);
            }
            //库存校验
            if (goods.getTotalNumber() < NumEnum.NUM_MIN.getCode() || goods.getTotalNumber() > NumEnum.NUM_MAX.getCode()) {
                return new BaseResponse().build(RespStatus.GOODS_NUM_LIMIT);
            }
            //商品规格
            if (LENGTH < goods.getSpecification().length()) {
                return new BaseResponse().build(RespStatus.GOODS_SPECIFICATION_LIMIT);
            }
            //商品名称
            if (hasEmoji(goods.getGoodsName())) {
                return new BaseResponse().build(RespStatus.GOODS_NAME_EMOJI);
            }
            if (LENGTH < goods.getGoodsName().length()) {
                return new BaseResponse().build(RespStatus.GOODS_NAME_LIMIT);
            }
        }
        param.getGoods().stream().forEach(goods -> goods.setCreatorOpenId(wxUser.getOpenid()));
        return goodsInfoService.addGoods(param);
    }

    /**
     * 商品管理-更新商品信息
     */
    @ResponseBody
    @ApiOperation(value = "/updateGoodsInfo", notes = "更新商品信息")
    @RequestMapping(value = "/updateGoodsInfo", method = RequestMethod.POST)
    public BaseResponse updateGoodsInfo(@RequestBody GoodsRequestParam param) {
        WxUser wxUser = wxUserService.getUserBySessionId(param.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        BaseResponse baseResponse = new BaseResponse();
        List<Goods> list = param.getGoods();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Goods goods : list) {

                GoodsInfo goodsInfo = goodsInfoDao.selectById(goods.getGoodsId());
                //DragonGoods dragonGoods = new DragonGoods();
                if (goodsInfo == null) {
                    goodsInfo = goodsInfoDao.selectGoods(goods.getGoodsNum());
                    if (goodsInfo == null) {
                        return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
                    }
                }
                //禁止前端输入
                if (StringUtils.isNotBlank(goods.getGoodsName()) && !goodsInfo.getGoodsName().equals(goods.getGoodsName())) {
                    return new BaseResponse().build(RespStatus.GOODS_NAME_CANT_UPDATE);
                }
                if (goods.isDeleted() == false) {
                    if (goods.getGoodsId() != null) {
                        goodsInfo.setId(goods.getGoodsId());
                    }

                    goodsInfo.setCreatorOpenId(wxUser.getOpenid());
                    if (goods.getGoodsName().equals(goodsInfo.getGoodsImg())) {
                        goodsInfo.setGoodsImg(null);
                    } else {
                        goodsInfo.setGoodsImg(goods.getGoodsImgs());
                    }
                    goodsInfo.setGoodsName(goods.getGoodsName());
                    goodsInfo.setGoodsNum(goods.getGoodsNum());
                    goodsInfo.setPrice(goods.getPrice());

                    /**
                     * 接龙商品价格同步更新
                     */
                    List<DragonGoods> dragonGoodsList = dragonGoodsService.getByGoodsNum(goods.getGoodsNum());
                    dragonGoodsList.forEach(x -> {
                        x.setCurrentNumber(goods.getTotalNumber());
                        x.setCurrentPrice(goods.getPrice());
                        x.setGoodsImg(goods.getGoodsImgs());
                        x.setSpecification(goods.getSpecification());
                        x.setUpdateTime(new Date());
                        dragonGoodsService.updateDragonGoods(x);
                    });
                    goodsInfo.setSpecification(goods.getSpecification());
                    goodsInfo.setTotalNumber(goods.getTotalNumber());
                    goodsInfo.setUpdateTime(new Date());
                    goodsInfo.setDeleted(goods.isDeleted());
                    goodsInfo.setLimitBuyNum(goods.getLimitBuyNum());
                } else {
                    goodsInfo.setDeleted(true);
                    goodsInfo.setUpdateTime(new Date());
                }
                int effect = goodsInfoDao.updateByGoodsId(goodsInfo);
                if (effect < 1) {
                    log.error("商品更新失败!{}", goods.toString());
                }
            }
        }
        // 填充店铺其他信息
        return baseResponse;
    }

    /**
     * 商品管理-查询商品信息详情
     */
    @ResponseBody
    @ApiOperation(value = "/queryGoodsDetail", notes = "商品管理-查询商品信息详情")
    @RequestMapping(value = "/queryGoodsDetail", method = RequestMethod.POST)
    public BaseResponse queryGoodsDetail(@RequestBody GoodsRequestParam param) {
        BaseResponse baseResponse = new BaseResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(param.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SYSTEM_ERROR);
        }
        GoodsInfo goods = goodsInfoService.getGoods(param.getGoodsNum());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("goodsInfo", goods);
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    /**
     * 商品管理-查询商品信息列表
     */
    @ResponseBody
    @ApiOperation(value = "/queryGoodsList", notes = "商品管理-查询商品信息")
    @RequestMapping(value = "/queryGoodsList", method = RequestMethod.POST)
    public BaseResponse queryGoodsList(@RequestBody ShopInfoQuery query) {
        BaseResponse baseResponse = new BaseResponse();
        Map<String, Object> hashMap = new HashMap();
        WxUser wxUser = wxUserService.getUserBySessionId(query.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SESSION_ID_EXPIRE);
        }
        // 使用子查询方式获取卖家历史接龙的商品
        List<GoodsInfo> goodsInfos = goodsInfoService.getGoodsListByUser(wxUser.getOpenid());
        // 根据卖家openId获取商品列表(新增创建人openId字段)
        PageHelper.startPage(query.getPageNo(), query.getPageSize());
        List<GoodsInfo> goodsInfoList = goodsInfoService.queryGoodsListByCreatorOpenId(wxUser.getOpenid());
        goodsInfoList.addAll(goodsInfos);
        List<GoodsInfo> list = goodsInfoService.filterAreadyDeletedGoods(goodsInfoList);
        List<GoodsInfo> infos = new ArrayList<>();
        for (GoodsInfo goodsInfo : list) {
            if (!infos.contains(goodsInfo)) {
                if (goodsInfo.getGoodsImg().indexOf(",") != -1) {
                    goodsInfo.setGoodsImg(goodsInfo.getGoodsImg().split(",")[0]);
                }
                infos.add(goodsInfo);
            }
        }
        // 根据创建时间的降序排列
        List<GoodsInfo> finalGoodsList = infos.stream().sorted(Comparator.comparing(GoodsInfo::getCreateTime).reversed()).collect(Collectors.toList());

        hashMap.put("goodsInfoList", finalGoodsList);
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    /**
     * 查询复购商品信息
     */
    @ResponseBody
    @ApiOperation(value = "/queryRepurchaseGoodsList", notes = "我的店铺-复购口碑商品")
    @RequestMapping(value = "/queryRepurchaseGoodsList", method = RequestMethod.POST)
    public BaseResponse queryRepurchaseGoodsList(@RequestBody ShopInfoQuery query) {
        WxUser wxUser = wxUserService.getUserBySessionId(query.getSessionId());
        if (wxUser == null) {
            return new BaseResponse().build(RespStatus.SYSTEM_ERROR);
        }
        //Shop shop = shopService.queryByShopId(query.getShopId());
        query.setShopkeeperOpenId(wxUser.getOpenid());
        query.setShopkeeperUserId(Long.valueOf(wxUser.getId()));
        BaseResponse baseResponse = new BaseResponse();
        Map<String, Object> hashMap = new HashMap<>();
        PageHelper.startPage(query.getPageNo(), query.getPageSize());
        List<GoodsInfoResponse> goodsInfoResponses = goodsInfoService.countGoodsRepurchase(query);
        hashMap.put("goodsInfoResponses", goodsInfoResponses);
        baseResponse.setMap(hashMap);
        // 填充店铺其他信息
        return baseResponse;
    }
}
