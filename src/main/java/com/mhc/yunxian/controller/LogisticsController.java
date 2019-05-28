package com.mhc.yunxian.controller;

import com.github.pagehelper.PageHelper;
import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.bean.request.param.LogisticsRequestParam;
import com.mhc.yunxian.bean.request.query.BaseQuery;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.service.LogisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2018/12/7.
 *
 * @author Alin
 */
@Controller
@RequestMapping(value = "/logistics")
@Api(value = "LogisticsController", description = "物流相关")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    /**
     * 订单发货选择快递公司
     */
    @ResponseBody
    @ApiOperation(value = "/queryLogisticsCompany", notes = "查询物流公司")
    @RequestMapping(value = "/queryLogisticsCompany", method = RequestMethod.POST)
    public BaseResponse queryLogisticsCompany(@RequestBody BaseQuery query) {

        PageHelper.startPage(query.getPageNo(), query.getPageSize());
        BaseResponse baseResponse = logisticsService.queryLogisticsCompany(query);

        return baseResponse;
    }

    /**
     * 新增物流信息
     * 填写快递单号(手动填写,扫描识别)
     */
    @ResponseBody
    @ApiOperation(value = "/addLogisticsInfo", notes = "新增物流信息")
    @RequestMapping(value = "/addLogisticsInfo", method = RequestMethod.POST)
    public BaseResponse addLogisticsInfo(@RequestBody LogisticsRequestParam param) {

        if (null == param.getLogisticsCompanyId()|| StringUtils.isBlank(param.getLogisticsCode())) {
            return new BaseResponse().build(RespStatus.ILLEGAL_ARGUMENT);
        }
        return logisticsService.addLogisticsInfo(param);
    }

}
