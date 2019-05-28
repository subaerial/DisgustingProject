package com.mhc.yunxian.controller;

import com.mhc.yunxian.bean.BaseResponse;
import com.mhc.yunxian.service.CommonService;
import com.mhc.yunxian.service.DragonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @RequestMapping(value = "/count",method = RequestMethod.POST)
    public BaseResponse count(){
        return commonService.countBy();
    }
}
