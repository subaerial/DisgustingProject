package com.mhc.yunxian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.dao.DragonInfoDao;
import com.mhc.yunxian.dao.GoodsInfoDao;
import com.mhc.yunxian.dao.MyUserDao;
import com.mhc.yunxian.dao.OrderInfoDao;
import com.mhc.yunxian.enums.DragonStatusEnum;
import com.mhc.yunxian.service.CommonService;
import com.mhc.yunxian.vo.CountGoodsVO;
import com.mhc.yunxian.vo.CountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private DragonInfoDao dragonInfoDao;

    @Autowired
    private OrderInfoDao orderInfoDao;

    @Autowired
    private GoodsInfoDao goodsInfoDao;

    @Autowired
    private MyUserDao myUserDao;

    @Override
    public BaseResponse countBy() {
        BaseResponse response = new BaseResponse();
        Map<String,Object> map = new HashMap<>();
        response.setMap(map);
        //统计接龙
        List<CountVO> list = dragonInfoDao.countBy();
        list.forEach(s -> {
            if (s != null && s.getState() != null) {
                if (s.getState().equals(DragonStatusEnum.IN_PROGRESS.getStatus())) {
                    map.put("dragon_now", s.getTotal());
                } else if (s.getState().equals(DragonStatusEnum.ALREADY_OVER.getStatus())) {
                    map.put("dragon_end", s.getTotal());
                }
            }
        });
        //统计复购口碑总数
        map.put("fg_total",myUserDao.countBy());

        //统计订单订单
        List<CountVO> orderList = orderInfoDao.countBy();
        if(!CollectionUtils.isEmpty(orderList)) {
            orderList.forEach(m -> {
                switch (m.getState()) {
                    case 1:
                        map.put("order_delivering", m.getTotal());
                        break;
                    case 2:
                        map.put("order_delivered", m.getTotal());
                        break;
                    case 3:
                        map.put("order_complete", m.getTotal());
                        break;
                    case 4:
                        map.put("order_refunding", m.getTotal());
                        break;
                    case 5:
                        map.put("order_refunded", m.getTotal());
                        break;
                    case 6:
                        map.put("order_cancel", m.getTotal());
                        break;
                    default:
                        log.warn("订单状态：{}", m.getState());
                }
            });
        }
        //统计订单总数
        map.put("order_total",orderList.stream().mapToInt(CountVO::getTotal).sum());
        //商品总数
        CountGoodsVO goodsVO =  goodsInfoDao.countBy();
        map.put("goods_total",goodsVO.getGoodsCount());
        //销售总数
        map.put("sell_total",goodsVO.getTotal());
        return response;
    }
}
