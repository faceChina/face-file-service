package com.zjlp.face.file.domain;

import java.util.Date;
/**
 * 压缩文件表
 * @ClassName: AttachFileScalr 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author phb
 * @date 2015年1月12日 下午1:38:09
 */
public class AttachFileScalr {
	//压缩文件编号
    private String fileScalrNo;
	//原文件编号
    private String attachFileNo;
    //所属用户
    private Long userId;
    //所属店铺
    private String shop;
	//压缩文件类型
    private String scalrType;
	//压缩文件大小
    private Long scalrSize;
	//业务表名称
    private String tableName;
	//业务表ID
    private String tableId;
    //压缩文件路径
    private String scalrPath;
    //压缩文件编码
    private String code;
    //文件标识 1.用户上传 2.记录备份
    private Integer fileLabel;
    //创建时间
    private Date createTime;
    //修改时间
    private Date updateTime;

    public String getFileScalrNo() {
        return fileScalrNo;
    }

    public void setFileScalrNo(String fileScalrNo) {
        this.fileScalrNo = fileScalrNo == null ? null : fileScalrNo.trim();
    }

    public String getAttachFileNo() {
        return attachFileNo;
    }

    public void setAttachFileNo(String attachFileNo) {
        this.attachFileNo = attachFileNo == null ? null : attachFileNo.trim();
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

    public String getScalrType() {
        return scalrType;
    }

    public void setScalrType(String scalrType) {
        this.scalrType = scalrType == null ? null : scalrType.trim();
    }

    public Long getScalrSize() {
        return scalrSize;
    }

    public void setScalrSize(Long scalrSize) {
        this.scalrSize = scalrSize;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId == null ? null : tableId.trim();
    }

    public String getScalrPath() {
        return scalrPath;
    }

    public void setScalrPath(String scalrPath) {
        this.scalrPath = scalrPath == null ? null : scalrPath.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }
    
    public Integer getFileLabel() {
		return fileLabel;
	}

	public void setFileLabel(Integer fileLabel) {
		this.fileLabel = fileLabel;
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