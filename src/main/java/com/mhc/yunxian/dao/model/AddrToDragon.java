package com.mhc.yunxian.dao.model;

public class AddrToDragon {
    private Integer id;

    private Integer addrId;

    private String dragonNum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAddrId() {
        return addrId;
    }

    public void setAddrId(Integer addrId) {
        this.addrId = addrId;
    }

    public String getDragonNum() {
        return dragonNum;
    }

    public void setDragonNum(String dragonNum) {
        this.dragonNum = dragonNum == null ? null : dragonNum.trim();
    }
}