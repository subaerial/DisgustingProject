package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.bean.CompleteBankInfoRequest;
import com.mhc.yunxian.bean.UserBankInfoVO;
import com.mhc.yunxian.dao.BankDao;
import com.mhc.yunxian.dao.CityDao;
import com.mhc.yunxian.dao.DrawMoneyDao;
import com.mhc.yunxian.dao.UserBankInfoDao;
import com.mhc.yunxian.dao.model.Bank;
import com.mhc.yunxian.dao.model.City;
import com.mhc.yunxian.dao.model.DrawMoney;
import com.mhc.yunxian.dao.model.UserBankInfo;
import com.mhc.yunxian.service.DrawMoneyService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrawMoneyServiceImpl implements DrawMoneyService {



    @Autowired
    DrawMoneyDao drawMoneyDao;

    @Autowired
    UserBankInfoDao userBankInfoDao;

    @Autowired
    BankDao bankDao;

    @Autowired
    CityDao cityDao;


    @Override
    public List<DrawMoney> selectByStatus() {
        return drawMoneyDao.selectByStatus();
    }

    @Override
    public List<DrawMoney> getDrawMoneyDetail(String openid) {
        return drawMoneyDao.selectByOpenid(openid);
    }

    @Override
    public int insertDrawMoney(DrawMoney record) {
        return drawMoneyDao.insert(record);
    }

    @Override
    public boolean updateDrawMoney(DrawMoney drawMoney) {
        return drawMoneyDao.updateDrawMoney(drawMoney) > 0;
    }

    @Override
    public DrawMoney selectByOpenIdAndFormId(String openId, String formId){
        return drawMoneyDao.selectByOpenIdAndFormId(openId,formId);
    }

    @Override
    public UserBankInfoVO getBankInfoVO(String openid) {
        return userBankInfoDao.selectUserBankInfoVOByOpenid(openid);
    }

    @Override
    public UserBankInfo getBankInfo(String openid) {
        return userBankInfoDao.getBankInfo(openid);
    }

    @Override
    public int updateUserBankInfo(UserBankInfo userBankInfo) {
        return userBankInfoDao.updateByPrimaryKeySelective(userBankInfo);
    }

    @Override
    public List<Bank> getAllBank() {
        return bankDao.selectAll();
    }

    @Override
    public List<City> getAllProvince() {
        return cityDao.selectAllProvince();
    }

    @Override
    public List<City> selectByProvince(Integer parentCode) {
        return cityDao.selectByProvince(parentCode);
    }

    @Override
    public void completeBankInfo(CompleteBankInfoRequest request) throws Exception {

        UserBankInfo userBankInfo = new UserBankInfo();

        userBankInfo.setAccountName(request.getAccountName());
        userBankInfo.setAccountNo(request.getAccountNo());
        userBankInfo.setBankId(request.getBankId());
        userBankInfo.setCityId(request.getCityCode());
        userBankInfo.setOpenid(request.getOpenid());

        if(userBankInfoDao.insertSelective(userBankInfo) < 1){
           throw new Exception("用户银行信息表添加失败");
        }
    }

    @Override
    public DrawMoney getDrawMoneyById(Integer id) {
        return drawMoneyDao.selectByPrimaryKey(id);
    }

    @Override
    public int drawMoney() {
        return 0;
    }


}
