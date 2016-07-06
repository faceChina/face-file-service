package com.zjlp.face.file.domain;

import java.io.Serializable;
import java.util.Date;
/**
 * 文件表
 * @ClassName: AttachFile 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author Administrator
 * @date 2015年1月5日 下午8:18:58
 */
public class AttachFile implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2762419690016910308L;
	//文件编号
    private String fileNo;
    //所属用户
    private Long userId;
    //所属店铺
    private String shop;
    //文件类型
    private String fileType;
    //文件大小
    private Long fileSize;
    //业务表名称
    private String tableName;
    //业务表ID
    private String tableId;
    //文件路径
    private String filePath;
    //文件代号
    private String code;
    //排序
    private Integer sort;
    //文件标识 1.用户上传 2.记录备份
    private Integer fileLabel;
	//创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;

    public String getFileNo() {
        return fileNo;
    }

    public void setFileNo(String fileNo) {
        this.fileNo = fileNo == null ? null : fileNo.trim();
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? null : fileType.trim();
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
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