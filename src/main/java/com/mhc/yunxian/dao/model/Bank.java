package com.mhc.yunxian.dao.model;

public class Bank {
    private Integer id;

    private String bankName;

    private String bankAb;

    private String bankLogo;

    private Integer isDisplay;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName == null ? null : bankName.trim();
    }

    public String getBankAb() {
        return bankAb;
    }

    public void setBankAb(String bankAb) {
        this.bankAb = bankAb == null ? null : bankAb.trim();
    }

    public String getBankLogo() {
        return bankLogo;
    }

    public void setBankLogo(String bankLogo) {
        this.bankLogo = bankLogo == null ? null : bankLogo.trim();
    }

    public Integer getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(Integer isDisplay) {
        this.isDisplay = isDisplay;
    }
}