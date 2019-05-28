/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao.model;

import java.io.Serializable;
import java.util.Date;

public class Logistics implements Serializable {
    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 物流ID  默认值：null
     */
    private Long logisticsId;

    /**
     * 用户openID  默认值：null
     */
    private String userOpenId;

    /**
     * 订单编号  默认值：null
     */
    private String orderNum;

    /**
     * 接龙编号  默认值：null
     */
    private String dragonNum;

    /**
     * 物流编号  默认值：
     */
    private Long logisticsCompanyId;
    /**
     * 物流编号
     */
    private String logisticsCode;
    /**
     * 物流公司  默认值：null
     */
    private String logisticsCompany;

    /**
     * 物流内容信息  默认值：null
     */
    private String logisticsContent;

    /**
     * 外部物流状态  默认值：null
     */
    private Integer outLogisticsStatus;

    /**
     * 外部物流信息  默认值：null
     */
    private String outLogisticsContent;

    /**
     * 物流状态  默认值：null
     */
    private Integer logisticsStatus;

    /**
     * 逻辑删除  默认值：null
     */
    private Boolean isDeleted;

    /**
     * 创建时间  默认值：null
     */
    private Date gmtCreate;

    /**
     * 修改时间  默认值：null
     */
    private Date gmtModified;

    /**
     * 扩展属性  默认值：null
     */
    private String jsonAttribute;

    /**
     * 获取 物流ID tbl_logistics.logistics_id
     *
     * @return 物流ID
     */
    public Long getLogisticsId() {
        return logisticsId;
    }

    /**
     * 设置 物流ID tbl_logistics.logistics_id
     *
     * @param logisticsId 物流ID
     */
    public void setLogisticsId(Long logisticsId) {
        this.logisticsId = logisticsId;
    }

    /**
     * 获取 用户openID tbl_logistics.user_open_id
     *
     * @return 用户openID
     */
    public String getUserOpenId() {
        return userOpenId;
    }

    /**
     * 设置 用户openID tbl_logistics.user_open_id
     *
     * @param userOpenId 用户openID
     */
    public void setUserOpenId(String userOpenId) {
        this.userOpenId = userOpenId == null ? null : userOpenId.trim();
    }

    /**
     * 获取 订单编号 tbl_logistics.order_num
     *
     * @return 订单编号
     */
    public String getOrderNum() {
        return orderNum;
    }

    /**
     * 设置 订单编号 tbl_logistics.order_num
     *
     * @param orderNum 订单编号
     */
    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum == null ? null : orderNum.trim();
    }

    /**
     * 获取 接龙编号 tbl_logistics.dragon_num
     *
     * @return 接龙编号
     */
    public String getDragonNum() {
        return dragonNum;
    }

    /**
     * 设置 接龙编号 tbl_logistics.dragon_num
     *
     * @param dragonNum 接龙编号
     */
    public void setDragonNum(String dragonNum) {
        this.dragonNum = dragonNum == null ? null : dragonNum.trim();
    }

    /**
     * 获取 物流编号 tbl_logistics.logistics_code
     *
     * @return 物流编号
     */
    public Long getLogisticsCompanyId() {
        return logisticsCompanyId;
    }

    public void setLogisticsCompanyId(Long logisticsCompanyId) {
        this.logisticsCompanyId = logisticsCompanyId;
    }

    public String getLogisticsCode() {
        return logisticsCode;
    }

    public void setLogisticsCode(String logisticsCode) {
        this.logisticsCode = logisticsCode;
    }

    /**
     * 获取 物流公司 tbl_logistics.logistics_company
     *
     * @return 物流公司
     */
    public String getLogisticsCompany() {
        return logisticsCompany;
    }

    /**
     * 设置 物流公司 tbl_logistics.logistics_company
     *
     * @param logisticsCompany 物流公司
     */
    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany == null ? null : logisticsCompany.trim();
    }

    /**
     * 获取 物流内容信息 tbl_logistics.logistics_content
     *
     * @return 物流内容信息
     */
    public String getLogisticsContent() {
        return logisticsContent;
    }

    /**
     * 设置 物流内容信息 tbl_logistics.logistics_content
     *
     * @param logisticsContent 物流内容信息
     */
    public void setLogisticsContent(String logisticsContent) {
        this.logisticsContent = logisticsContent == null ? null : logisticsContent.trim();
    }

    /**
     * 获取 外部物流状态 tbl_logistics.out_logistics_status
     *
     * @return 外部物流状态
     */
    public Integer getOutLogisticsStatus() {
        return outLogisticsStatus;
    }

    /**
     * 设置 外部物流状态 tbl_logistics.out_logistics_status
     *
     * @param outLogisticsStatus 外部物流状态
     */
    public void setOutLogisticsStatus(Integer outLogisticsStatus) {
        this.outLogisticsStatus = outLogisticsStatus;
    }

    /**
     * 获取 外部物流信息 tbl_logistics.out_logistics_content
     *
     * @return 外部物流信息
     */
    public String getOutLogisticsContent() {
        return outLogisticsContent;
    }

    /**
     * 设置 外部物流信息 tbl_logistics.out_logistics_content
     *
     * @param outLogisticsContent 外部物流信息
     */
    public void setOutLogisticsContent(String outLogisticsContent) {
        this.outLogisticsContent = outLogisticsContent == null ? null : outLogisticsContent.trim();
    }

    /**
     * 获取 物流状态 tbl_logistics.logistics_status
     *
     * @return 物流状态
     */
    public Integer getLogisticsStatus() {
        return logisticsStatus;
    }

    /**
     * 设置 物流状态 tbl_logistics.logistics_status
     *
     * @param logisticsStatus 物流状态
     */
    public void setLogisticsStatus(Integer logisticsStatus) {
        this.logisticsStatus = logisticsStatus;
    }

    /**
     * 获取 逻辑删除 tbl_logistics.is_deleted
     *
     * @return 逻辑删除
     */
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置 逻辑删除 tbl_logistics.is_deleted
     *
     * @param isDeleted 逻辑删除
     */
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * 获取 创建时间 tbl_logistics.gmt_create
     *
     * @return 创建时间
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 设置 创建时间 tbl_logistics.gmt_create
     *
     * @param gmtCreate 创建时间
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 获取 修改时间 tbl_logistics.gmt_modified
     *
     * @return 修改时间
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * 设置 修改时间 tbl_logistics.gmt_modified
     *
     * @param gmtModified 修改时间
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * 获取 扩展属性 tbl_logistics.json_attribute
     *
     * @return 扩展属性
     */
    public String getJsonAttribute() {
        return jsonAttribute;
    }

    /**
     * 设置 扩展属性 tbl_logistics.json_attribute
     *
     * @param jsonAttribute 扩展属性
     */
    public void setJsonAttribute(String jsonAttribute) {
        this.jsonAttribute = jsonAttribute == null ? null : jsonAttribute.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logisticsId=").append(logisticsId);
        sb.append(", userOpenId=").append(userOpenId);
        sb.append(", orderNum=").append(orderNum);
        sb.append(", dragonNum=").append(dragonNum);
        sb.append(", logisticsCompanyId=").append(logisticsCompanyId);
        sb.append(", logisticsCompany=").append(logisticsCompany);
        sb.append(", logisticsContent=").append(logisticsContent);
        sb.append(", outLogisticsStatus=").append(outLogisticsStatus);
        sb.append(", outLogisticsContent=").append(outLogisticsContent);
        sb.append(", logisticsStatus=").append(logisticsStatus);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModified=").append(gmtModified);
        sb.append(", jsonAttribute=").append(jsonAttribute);
        sb.append("]");
        return sb.toString();
    }
}