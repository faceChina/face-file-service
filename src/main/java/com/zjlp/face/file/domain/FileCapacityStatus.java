package com.zjlp.face.file.domain;

import java.util.Date;
/**
 * 文件容量状态表
 * @ClassName: FileCapacityStatus 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author Administrator
 * @date 2015年1月5日 下午8:19:13
 */
public class FileCapacityStatus {
	//主键
    private Long id;
    //所属用户
    private Long userId;
    //店铺
    private String shop;
    //总容量 KB
    private Long totalCapacity;
    //当前容量 KB
    private Long currentCapacity;
    //预警容量
    private Long warnCapacity;
    //可用容量
    private Long activeCapacity;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop == null ? null : shop.trim();
    }

    public Long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Long getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(Long currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public Long getWarnCapacity() {
        return warnCapacity;
    }

    public void setWarnCapacity(Long warnCapacity) {
        this.warnCapacity = warnCapacity;
    }

    public Long getActiveCapacity() {
        return activeCapacity;
    }

    public void setActiveCapacity(Long activeCapacity) {
        this.activeCapacity = activeCapacity;
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