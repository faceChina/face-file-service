package com.zjlp.face.file.domain;

import java.util.Date;
/**
 * 容量配比信息
 * @ClassName: FileCapacityInfo 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author phb
 * @date 2015年1月12日 下午1:39:39
 */
public class FileCapacityInfo {
	//主键
    private Long id;
    //文件容量状态
    private Long statusId;
    //容量类型(1.商户容量 2 产品容量)
    private Integer type;
    //产品类型(1.官网 2 商城 3 脸谱 )
    private Integer productType;
    //容量(KB)
    private Long capacity;
    //创建时间
    private Date createTime;
    //修改时间
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}