package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.AdminChangePwdRequest;
import com.mhc.yunxian.bean.admin.ManagerLoginRequest;
import com.mhc.yunxian.dao.AdminDao;
import com.mhc.yunxian.dao.model.Admin;
import com.mhc.yunxian.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    AdminDao adminDao;

    @Override
    public boolean login(ManagerLoginRequest managerLoginRequest) {

        managerLoginRequest.setPassword(DigestUtils.sha1Hex(managerLoginRequest.getPassword()));

        Admin admin = adminDao.selectAdmin(managerLoginRequest);


        if (admin != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean changePwd(AdminChangePwdRequest request) {

        Map param = new HashMap();
        param.put("username",request.getUsername());
        param.put("password",DigestUtils.sha1Hex(request.getOldPwd()));

        Admin admin = adminDao.selectAdminInfo(param);

        if(admin != null){
            param.put("password",DigestUtils.sha1Hex((request.getNewPwd())));

            if(adminDao.updateByUserName(param) < 1){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
}
