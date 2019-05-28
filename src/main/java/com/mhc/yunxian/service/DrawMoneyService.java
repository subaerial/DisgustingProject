package com.mhc.yunxian.service;

import com.mhc.yunxian.bean.CompleteBankInfoRequest;
import com.mhc.yunxian.bean.UserBankInfoVO;
import com.mhc.yunxian.dao.model.Bank;
import com.mhc.yunxian.dao.model.City;
import com.mhc.yunxian.dao.model.DrawMoney;
import com.mhc.yunxian.dao.model.UserBankInfo;

import java.util.List;

public interface DrawMoneyService {


    List<DrawMoney> selectByStatus();

    List<DrawMoney> getDrawMoneyDetail(String openid);

    int insertDrawMoney(DrawMoney record);


    boolean updateDrawMoney(DrawMoney drawMoney);

    DrawMoney selectByOpenIdAndFormId(String openId, String formId);

    UserBankInfoVO getBankInfoVO(String openid);

    UserBankInfo getBankInfo(String openid);

    int updateUserBankInfo(UserBankInfo userBankInfo);

    List<Bank> getAllBank();

    List<City> getAllProvince();

    List<City> selectByProvince(Integer parentCode);

    void completeBankInfo(CompleteBankInfoRequest request) throws Exception;

    DrawMoney getDrawMoneyById(Integer id);

    /*微信自动打款*/
    int drawMoney();

}
