package com.mhc.yunxian.controller;

import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.service.MsgRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.controller
 * @author: 昊天
 * @date: 2019/1/24 2:18 PM
 * @since V1.1.0-SNAPSHOT
 */
@Api(value = "MessageController",description = "站内消息相关")
@RestController
@RequestMapping(value = "/yunxian")
public class MessageController {

    @Autowired
    private MsgRecordService msgRecordService;


    @PostMapping("/v21/message/listMsg")
    @ApiOperation(value = "listMsg",notes = "消息列表")
    public MsgListResponse listMsg(@RequestBody MsgRecordRequest request){
         return msgRecordService.listMsg(request);
    }

    @ApiOperation(value = "readMsg",notes = "消息阅读状态修改")
    @PostMapping("/v21/message/readMsg")
    public BaseResponse updateMsgReadStatus(@RequestBody ReadMsgRequest readMsgRequest){
        boolean IsSuccess = msgRecordService.updateReadStatus(readMsgRequest.getMsgId());
        BaseResponse baseResponse = new BaseResponse();
        Map<String,Object> map = new HashMap<>();
        if(IsSuccess){
            map.put("code","OK");
        }else {
            map.put("code","FAIL");
            map.put("msg","该消息已读了");
        }
        baseResponse.setMap(map);
        return baseResponse;
    }
}
