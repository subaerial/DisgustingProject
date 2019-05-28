package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.GetMyUserRequest;
import com.mhc.yunxian.dao.MyUserDao;
import com.mhc.yunxian.dao.model.MyUser;
import com.mhc.yunxian.service.MyUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MyUserServiceImpl implements MyUserService {


    @Autowired
    MyUserDao myUserDao;


    @Override
    public List<MyUser> getMyAllUser(GetMyUserRequest request) {
        return myUserDao.selectAllMyUser(request);
    }

    @Override
    public MyUser getUser(String openid, String sellOpenid) {
        return myUserDao.selectMyUser(openid,sellOpenid);
    }

    @Override
    public boolean updateMyUser(MyUser user) {
        return myUserDao.updateUser(user) > 0;
    }

    @Override
    public boolean insertMyUser(MyUser user) {
        return myUserDao.insertMyUser(user) > 0;
    }

    @Override
    public int countMyUserNumber(Map param){
        return myUserDao.countMyUserNumber(param);
    }

    @Override
    public List<MyUser> getAll() {
        return myUserDao.getAll();
    }
}
