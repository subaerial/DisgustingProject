package com.mhc.yunxian.controller;


import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.model.SendAddr;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.service.SendAddrService;
import com.mhc.yunxian.service.WxUserService;
import com.mhc.yunxian.utils.KeyTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 *
 * 地址的增删改查
 *
 */

@Slf4j
@RestController
@RequestMapping("/")
public class AddrController {


    @Autowired
    SendAddrService sendAddrService;
    @Autowired
    WxUserService wxUserService;

    @RequestMapping(value = "/yunxian/getAddrNum",method = RequestMethod.POST)
    public GetAddrNumResponse getAddrNum(@RequestBody GetAddrNumRequest request){
        final GetAddrNumResponse response = new GetAddrNumResponse();

        //生成地址编号
        String addrNum = "A" + KeyTool.createOrderNo();

        response.setAddrNum(addrNum);

        return response;
    }


    @RequestMapping(value = "/yunxian/addAddr",method = RequestMethod.POST)
    public AddAddrResponse addAddr(@RequestBody AddAddrRequest request){
        final AddAddrResponse response = new AddAddrResponse();
        WxUser userBySessionId = wxUserService.getUserBySessionId(request.getSessionId());
        request.setOpenid(userBySessionId.getOpenid());
        final SendAddr sendAddr = new SendAddr();
        sendAddr.setUserName(request.getUserName());
        sendAddr.setAddrNum(request.getAddrNum());
        sendAddr.setPhone(request.getPhone());
        sendAddr.setMyAddr(request.getAddr());
        sendAddr.setCreateTime(new Date());
        sendAddr.setOpenid(request.getOpenid());

        //查询地址是否存在，有责修改无则添加
        if(request.getAddrNum() == null){
            if (!sendAddrService.addAddr(sendAddr)){

                return (AddAddrResponse) response.build(400,"添加地址失败");
            }
        } else {
            if (!sendAddrService.updateAddr(sendAddr)){
                return (AddAddrResponse) response.build(400,"更新地址失败");
            }
        }
        return response;
    }


    @RequestMapping(value = "/yunxian/delAddr",method = RequestMethod.POST)
    public DelAddrResponse delAddr(@RequestBody DelAddrRequest request){
        final DelAddrResponse response = new DelAddrResponse();

        if (!sendAddrService.delAddr(request.getAddrNum())){
            return (DelAddrResponse) response.build(RespStatus.SYSTEM_ERROR);
        }

        return response;
    }

    /**
     * 获取我的地址列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/yunxian/getAllMyAddr",method = RequestMethod.POST)
    public GetAllMyAddrResponse getAllMyAddr(@RequestBody GetAllMyAddrRequest request){
        final GetAllMyAddrResponse response = new GetAllMyAddrResponse();
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        request.setOpenid(wxUser.getOpenid());
        final List<SendAddr> sendAddrs = sendAddrService.getAddr(request.getOpenid());
        if (sendAddrs == null){
            return (GetAllMyAddrResponse) response.build(RespStatus.SYSTEM_ERROR);
        }
        response.setData(sendAddrs);

        return response;
    }




}
