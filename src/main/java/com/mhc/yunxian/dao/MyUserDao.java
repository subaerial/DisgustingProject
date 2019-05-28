package com.mhc.yunxian.dao;

import com.mhc.yunxian.bean.GetMyUserRequest;
import com.mhc.yunxian.dao.model.MyUser;
import com.mhc.yunxian.vo.CountVO;

import java.util.List;
import java.util.Map;

public interface MyUserDao {

    List<MyUser> selectAllMyUser(GetMyUserRequest request);

    MyUser selectMyUser(String openid,String sellOpenid);

    int updateUser(MyUser user);

    int insertMyUser(MyUser user);

    int countMyUserNumber(Map param);

    int countBy();

    List<MyUser> getAll();
}