package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.admin.ManagerLoginRequest;
import com.mhc.yunxian.dao.model.Admin;

import java.util.Map;

public interface AdminDao {

    Admin selectAdmin(ManagerLoginRequest managerLoginRequest);

    int updateByUserName(Map map);

    Admin selectAdminInfo(Map map);

}
