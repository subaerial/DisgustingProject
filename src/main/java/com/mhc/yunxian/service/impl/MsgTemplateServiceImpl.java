package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.MsgTemplateMapper;
import com.mhc.yunxian.dao.model.MsgTemplate;
import com.mhc.yunxian.service.MsgTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * All rights Reserved, Designed By 863044052@qq.com
 *
 * @Package com.mhc.yunxian.service.impl
 * @author: 昊天
 * @date: 2019/1/24 2:05 PM
 * @version: 0.0.1-vserion
 */
@Service
public class MsgTemplateServiceImpl implements MsgTemplateService {

    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    @Override
    public List<MsgTemplate> getByMsgType(Integer msgType) {
        return msgTemplateMapper.selectByMsgType(msgType);
    }
}
