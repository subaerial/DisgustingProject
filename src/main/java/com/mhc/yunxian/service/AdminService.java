package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.AdminChangePwdRequest;
import com.mhc.yunxian.bean.admin.ManagerLoginRequest;
import com.mhc.yunxian.dao.model.Admin;

public interface AdminService {

    boolean login(ManagerLoginRequest managerLoginRequest);

    boolean changePwd(AdminChangePwdRequest request);

}
