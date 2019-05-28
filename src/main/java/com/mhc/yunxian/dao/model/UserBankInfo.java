package com.mhc.yunxian.dao.model;

public class UserBankInfo {
    private Integer id;

    private String openid;

    private String accountName;

    private String accountNo;

    private Integer bankId;

    private Integer cityId;

    private Integer limit;

    private Integer usedLimit;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName == null ? null : accountName.trim();
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo == null ? null : accountNo.trim();
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getUsedLimit() {
        return usedLimit;
    }

    public void setUsedLimit(Integer usedLimit) {
        this.usedLimit = usedLimit;
    }
}