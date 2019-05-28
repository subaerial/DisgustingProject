package com.mhc.yunxian.dao.model;

public class City {
    private Integer id;

    private Integer code;

    private Integer type;

    private Integer parentCode;

    private String name;

    private String ab;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getParentCode() {
        return parentCode;
    }

    public void setParentCode(Integer parentCode) {
        this.parentCode = parentCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAb() {
        return ab;
    }

    public void setAb(String ab) {
        this.ab = ab == null ? null : ab.trim();
    }
}