package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.param.LogisticsRequestParam;
import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.LogisticsCompanyMapper;
import com.mhc.yunxian.dao.LogisticsMapper;
import com.mhc.yunxian.dao.model.Logistics;
import com.mhc.yunxian.dao.model.LogisticsCompany;
import com.mhc.yunxian.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/12/5.
 *
 * @author Alin
 */
@Service
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    private LogisticsMapper logisticsMapper;
    @Autowired
    private LogisticsCompanyMapper logisticsCompanyMapper;

    @Override
    public BaseResponse queryLogisticsCompany(BaseQuery query) {
        BaseResponse baseResponse = new BaseResponse();
        Map<String, Object> hashMap = new HashMap<>();
        List<LogisticsCompany> logisticsCompanies = logisticsCompanyMapper.queryLogisticsCompany(query);
        hashMap.put("logisticsCompanies", logisticsCompanies);
        baseResponse.setMap(hashMap);
        return baseResponse;
    }

    @Override
    public Logistics getLogisticsInfoByOrderNum(String orderNum) {

        return logisticsMapper.getLogisticsInfoByOrderNum(orderNum);
    }

    @Override
    public BaseResponse addLogisticsInfo(LogisticsRequestParam param) {
        BaseResponse baseResponse = new BaseResponse();
        Logistics logisticsInfo = logisticsMapper.getLogisticsInfoByOrderNum(param.getOrderNum());
        LogisticsCompany logisticsCompany = logisticsCompanyMapper.selectByPrimaryKey(param.getLogisticsCompanyId());
        int effect = 0;
        if (logisticsInfo != null) {
            // 已存在物流信息,则更新
            this.fillLogisticsInfo(param, logisticsInfo, logisticsCompany);
            effect = logisticsMapper.updateByPrimaryKeySelective(logisticsInfo);
        } else {
            Logistics logistics = new Logistics();
            this.fillLogisticsInfo(param, logistics, logisticsCompany);
            effect = logisticsMapper.insertSelective(logistics);
        }
        if (effect > 0) {
            return baseResponse.build(RespStatus.SUCCESS);
        }
        return baseResponse.build(RespStatus.DB_INSERT_FAILED);
    }

    /**
     * 填充物流信息
     *
     * @param param
     * @param logisticsInfo
     * @param logisticsCompany
     */
    private void fillLogisticsInfo(LogisticsRequestParam param, Logistics logisticsInfo, LogisticsCompany logisticsCompany) {
        logisticsInfo.setDragonNum(param.getDragonNum());
        logisticsInfo.setOrderNum(param.getOrderNum());
        logisticsInfo.setUserOpenId(param.getUserOpenId());
        logisticsInfo.setGmtCreate(new Date());
        logisticsInfo.setLogisticsCode(param.getLogisticsCode());
        logisticsInfo.setLogisticsContent(param.getLogisticsContent());
        logisticsInfo.setLogisticsCompany(logisticsCompany.getLogisticsCompany());
        logisticsInfo.setLogisticsCompanyId(param.getLogisticsCompanyId());
    }
}
