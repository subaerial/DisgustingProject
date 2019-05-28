package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.GetMyUserRequest;
import com.mhc.yunxian.dao.model.MyUser;

import java.util.List;
import java.util.Map;

public interface MyUserService {


    List<MyUser> getMyAllUser(GetMyUserRequest request);

    MyUser getUser(String openid,String sellOpenid);

    boolean updateMyUser(MyUser user);

    boolean insertMyUser(MyUser user);

    int countMyUserNumber(Map param);

    /**
     * <p> 同步用户管理下用户复购缓存使用 </p>
     * @param 
     * @return List<MyUser>
     * @author 昊天 
     * @date  
     * @since V1.1.0-SNAPSHOT
     *
     */
    List<MyUser> getAll();

}
