/* Qimu Co.,Lmt. */
package com.mhc.yunxian.dao.model;

import java.io.Serializable;
import java.util.Date;

public class LogisticsCompany implements Serializable {
    /** 序列化ID */
    private static final long serialVersionUID = 1L;

    /** 物流公司ID  默认值：null */
    private Long logisticsCompanyId;

    /** 物流公司名称  默认值：null */
    private String logisticsCompany;

    /** 物流公司logo  默认值：null */
    private String logisticsLogo;

    /** 状态(启用/不启用)  默认值：null */
    private Integer status;

    /** 创建时间  默认值：null */
    private Date gmtCreate;

    /** 修改时间  默认值：null */
    private Date gmtModified;

    /** 
     * 获取 物流公司ID tbl_logistics_company.logistics_company_id
     * @return 物流公司ID
     */
    public Long getLogisticsCompanyId() {
        return logisticsCompanyId;
    }

    /** 
     * 设置 物流公司ID tbl_logistics_company.logistics_company_id
     * @param logisticsCompanyId 物流公司ID
     */
    public void setLogisticsCompanyId(Long logisticsCompanyId) {
        this.logisticsCompanyId = logisticsCompanyId;
    }

    /** 
     * 获取 物流公司名称 tbl_logistics_company.logistics_company
     * @return 物流公司名称
     */
    public String getLogisticsCompany() {
        return logisticsCompany;
    }

    /** 
     * 设置 物流公司名称 tbl_logistics_company.logistics_company
     * @param logisticsCompany 物流公司名称
     */
    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany == null ? null : logisticsCompany.trim();
    }

    /** 
     * 获取 物流公司logo tbl_logistics_company.logistics_logo
     * @return 物流公司logo
     */
    public String getLogisticsLogo() {
        return logisticsLogo;
    }

    /** 
     * 设置 物流公司logo tbl_logistics_company.logistics_logo
     * @param logisticsLogo 物流公司logo
     */
    public void setLogisticsLogo(String logisticsLogo) {
        this.logisticsLogo = logisticsLogo == null ? null : logisticsLogo.trim();
    }

    /** 
     * 获取 状态(启用/不启用) tbl_logistics_company.status
     * @return 状态(启用/不启用)
     */
    public Integer getStatus() {
        return status;
    }

    /** 
     * 设置 状态(启用/不启用) tbl_logistics_company.status
     * @param status 状态(启用/不启用)
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /** 
     * 获取 创建时间 tbl_logistics_company.gmt_create
     * @return 创建时间
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /** 
     * 设置 创建时间 tbl_logistics_company.gmt_create
     * @param gmtCreate 创建时间
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /** 
     * 获取 修改时间 tbl_logistics_company.gmt_modified
     * @return 修改时间
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /** 
     * 设置 修改时间 tbl_logistics_company.gmt_modified
     * @param gmtModified 修改时间
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logisticsCompanyId=").append(logisticsCompanyId);
        sb.append(", logisticsCompany=").append(logisticsCompany);
        sb.append(", logisticsLogo=").append(logisticsLogo);
        sb.append(", status=").append(status);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModified=").append(gmtModified);
        sb.append("]");
        return sb.toString();
    }
}