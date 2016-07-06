package com.zjlp.face.file.dto;

import java.io.Serializable;
/**
 * 请求保存或者修改一个业务包含的图片的参数类
 * @ClassName: ReqParamDto 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author phb
 * @date 2015年1月15日 下午2:13:06
 */
public class ReqParamDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 391458330251708096L;
	
	/**
	 * 必须的
	 * 图片数据：默认的为fileNo。
	 * 如果是新上传的图片，则为上传图片返回的path
	 * 如商品主图有多张的，按图片顺序以“,”连接，当前顺序认为是图片的排序
	 */
	private String imgData;
	
	/**
	 * 可为空
	 * 需要生成的缩略图尺寸，多个缩略图格式为"640_640,640_380"
	 */
	private String zoomSizes;
	
	/**
	 * 必须的
	 * 超管编号，用于当前图片属于谁的，占用图片空间将算到当前超管，
	 * 会员领卡生成图片将算到当前超管
	 */
	private Long userId;
	
	/**
	 * 可为空
	 * 上传图片的店铺，在超管上传图片时为NULL
	 */
	private String shopNo;
	
	/**
	 * 必须的
	 * 当前图片关联的业务表名
	 * 取图时用
	 */
	private String tableName;
	
	/**
	 * 必须的
	 * 当前图片关联的业务表编号
	 * 是属于哪条数据的图片
	 */
	private String tableId;
	
	/**
	 * 必须的
	 * 图片的CODE
	 * 1.标明图片出处类型，如商品图片goodFile
	 * 2.限制把控可上传的安全性
	 */
	private String code;

	public String getImgData() {
		return imgData;
	}

	public void setImgData(String imgData) {
		this.imgData = imgData;
	}

	public String getZoomSizes() {
		return zoomSizes;
	}

	public void setZoomSizes(String zoomSizes) {
		this.zoomSizes = zoomSizes;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getShopNo() {
		return shopNo;
	}

	public void setShopNo(String shopNo) {
		this.shopNo = shopNo;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
