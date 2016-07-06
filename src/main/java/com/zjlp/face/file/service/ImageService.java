package com.zjlp.face.file.service;

import java.util.List;

import com.zjlp.face.file.domain.AttachFile;
import com.zjlp.face.file.dto.CutDto;
import com.zjlp.face.file.dto.FileBizParamDto;
import com.zjlp.face.file.dto.PressParamDto;
import com.zjlp.face.file.file.exception.FileException;


public interface ImageService {
	/**
	 * 上传图片
	 * @Title: upload 
	 * @Description: (上传到 图片服务 保存图片到图片服务，返回是否成功 SUCCESS FAILED) 
	 * @param img
	 * @return
	 * @date 2015年1月5日 下午3:32:06  
	 * @author phb
	 */
	String upload(byte[] img);
	
	/**
	 * 根据路径拿二进制文件
	 * @Title: getFileByte 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param path
	 * @return
	 * @date 2015年5月16日 下午4:47:48  
	 * @author lys
	 */
	byte[] getFileByte(String path);
	
	/**
	 * 图片截取
	 * @Title: cut 
	 * @Description: (接收截图参数，返回截图后的byte数组) 
	 * @param cutDto
	 * @return
	 * @date 2015年1月5日 下午3:09:38  
	 * @author phb
	 */
	String cut(CutDto cutDto);
	
	/**
	 * 保存一个事物下涉及到的图片
	 * @Title: addOrEdit 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param reqParamDtos
	 * @return
	 * @throws FileException
	 * @date 2015年1月15日 下午2:12:48  
	 * @author phb
	 */
	String addOrEdit(List<FileBizParamDto> fileBizParamDtos) throws FileException;

	/**
	 * 删除一个事物下涉及到的图片
	 * @Title: remove 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param reqParamDtos
	 * @return
	 * @throws FileException
	 * @date 2015年1月15日 下午4:25:04  
	 * @author phb
	 */
	String remove(List<FileBizParamDto> fileBizParamDtos) throws FileException;
	
	/**
	 * 添加水印文字，用于会员领卡
	 * @Title: pressText 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param pressParamDto
	 * @return
	 * @date 2015年1月16日 上午10:03:11  
	 * @author phb
	 */
	String pressText(PressParamDto pressParamDto);
	
	/***
	 * 根据tableId tableName code 获取图片集合
	 * @Title: findByTableIdAndCode
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param attachFile
	 * @return
	 * @return List<AttachFile>
	 * @author phb
	 * @date 2015年3月5日 下午4:57:32
	 */
	List<AttachFile> findByTableIdAndCode(AttachFile attachFile);
	
}
