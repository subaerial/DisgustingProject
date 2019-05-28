package com.mhc.yunxian.service.impl;

import com.google.common.collect.Lists;
import com.mhc.yunxian.bean.*;
import com.mhc.yunxian.bean.request.query.MsgRecordQuery;
import com.mhc.yunxian.commons.utils.DateUtil;
import com.mhc.yunxian.constants.RespStatus;
import com.mhc.yunxian.dao.MsgRecordMapper;
import com.mhc.yunxian.dao.MsgTemplateMapper;
import com.mhc.yunxian.dao.model.MsgRecord;
import com.mhc.yunxian.dao.model.MsgTemplate;
import com.mhc.yunxian.dao.model.WxUser;
import com.mhc.yunxian.enums.IsDeleteEnum;
import com.mhc.yunxian.enums.IsReadEnum;
import com.mhc.yunxian.enums.MsgTypeEnum;
import com.mhc.yunxian.service.MsgRecordService;
import com.mhc.yunxian.service.WxUserService;
import com.mhc.yunxian.utils.MessageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service.impl
 * @author: 昊天
 * @date: 2019/1/24 2:10 PM
 * @version: 0.0.1-vserion
 */
@Service
public class MsgRecordServiceImpl implements MsgRecordService {

    @Autowired
    private MsgRecordMapper msgRecordMapper;

    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    @Autowired
    private WxUserService wxUserService;


    @Override
    public String getNewContent(Integer msgType, ConvertArgs convertArgs) {
        List<MsgTemplate> msgTemplates = msgTemplateMapper.selectByMsgType(msgType);
        if(CollectionUtils.isEmpty(msgTemplates)){
            return "";
        }
        //取最新的模版
        String msg = msgTemplates.get(0).getContent();
        String newContent = "";
        switch (msgType){
            case 0:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName());
                break;
            case 1:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName(),convertArgs.getAddr());
                break;
            case 2:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName(),convertArgs.getPrice());
                break;
            case 3:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName(),convertArgs.getPrice());
                break;
            case 4:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName(),convertArgs.getPrice());
                break;
            case 5:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName(),convertArgs.getPrice());
                break;
            case 6:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName(),convertArgs.getSendTime());
                break;
            case 7:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getUserName(),convertArgs.getDragonName());
                break;
            case 8:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getUserName(),convertArgs.getDragonName());
                break;
            case 9:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getPrice());
                break;
            case 10:
                newContent = MessageUtil.replaceMsg(msg,convertArgs.getDragonName());
                break;
            default:
                break;
        }
        return newContent;
    }

    @Override
    public Map<String,Object> addMsgRecord(String openId, Integer msgType, Integer role, String url, ConvertArgs convertArgs) {
        Map<String,Object>  map = new HashMap<>();
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setOpenId(openId);
        msgRecord.setMsgType(msgType);
        msgRecord.setRole(role);
        msgRecord.setJumpUrl(url);
        msgRecord.setIsRead(IsReadEnum.UNREAD.getCode());
        msgRecord.setIsDeleted(IsDeleteEnum.IS_NOT_DELETE.getCode());
        msgRecord.setCreateTime(new Date());
        String newContent = getNewContent(msgType, convertArgs);
        msgRecord.setMsgContent(newContent);
        int i = msgRecordMapper.insertSelective(msgRecord);
        if(i == 1){
            map.put("title",MsgTypeEnum.getTitleByType(msgType));
            map.put("content",newContent);
            map.put("openId",openId);
        }
        return map;
    }

    @Override
    public boolean updateReadStatus(Integer id) {
        MsgRecord msgRecord = msgRecordMapper.selectByPrimaryKey(id);
        if(IsReadEnum.READ.getCode().equals(msgRecord.getIsRead())){
            //如果改记录已经是已读了
            return false;
        }
        msgRecord.setIsRead(IsReadEnum.READ.getCode());
        msgRecord.setReadTime(new Date());
        int i = msgRecordMapper.updateByPrimaryKeySelective(msgRecord);
        if(i == 1){
            return true;
        }
        return false;
    }


    @Override
    public MsgListResponse listMsg(MsgRecordRequest request) {
        MsgListResponse msgListResponse = new MsgListResponse();
        //入参校验
        WxUser wxUser = wxUserService.getUserBySessionId(request.getSessionId());
        if(Objects.isNull(wxUser)){
            return (MsgListResponse) msgListResponse.build(RespStatus.USER_NOT_EXIST);
        }
        if(null == request.getRole() || null == request.getIsRead()){
            return (MsgListResponse) msgListResponse.build(RespStatus.ILLEGAL_ARGUMENT);
        }
        //查询消息记录
        MsgRecordQuery query = new MsgRecordQuery();
        query.setOpenId(wxUser.getOpenid());
        query.setRole(request.getRole());
        query.setIsRead(request.getIsRead());
        query.setIsDelete(IsDeleteEnum.IS_NOT_DELETE.getCode());
        query.setPageNo(request.getPage());
        query.setPageSize(request.getSize());
        List<MsgRecord> msgRecords = msgRecordMapper.queryMsgRecord(query);
        List<MsgRecordVO> msgRecordVOs = Lists.newArrayList();
        //方案一，性能可能很差
        msgRecords.forEach(x->{
            MsgRecordVO msgRecordVO = new MsgRecordVO();
            //复制参数
            BeanUtils.copyProperties(x,msgRecordVO);
            List<MsgTemplate> msgTemplates = msgTemplateMapper.selectByMsgType(x.getMsgType());
            msgRecordVO.setTitle(msgTemplates.get(0).getTitle());
            msgRecordVO.setContent(x.getMsgContent());
            if(null != x.getReadTime()) {
                msgRecordVO.setReadTime(x.getReadTime().getTime());
            }
            msgRecordVO.setCreateTime(x.getCreateTime().getTime());
            msgRecordVOs.add(msgRecordVO);
        });
        //todo: 2019.01.25 分组查出模版标题
        msgListResponse.setCount(msgRecordVOs.size());
        msgListResponse.setRecordVOS(msgRecordVOs);
        return msgListResponse;
    }
}
