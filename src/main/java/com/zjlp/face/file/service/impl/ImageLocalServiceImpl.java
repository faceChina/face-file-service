package com.zjlp.face.file.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zjlp.face.file.dao.AttachFileDao;
import com.zjlp.face.file.dao.AttachFileScalrDao;
import com.zjlp.face.file.domain.AttachFile;
import com.zjlp.face.file.domain.AttachFileScalr;
import com.zjlp.face.file.file.FileClient;
import com.zjlp.face.file.service.ImageLocalService;
import com.zjlp.face.file.util.ImageUtils;

@Service("imageLocalService")
public class ImageLocalServiceImpl implements ImageLocalService {

	private Logger _infoLog = Logger.getLogger("fileInfoLog");
	
	@Autowired
	private AttachFileDao attachFileDao;
	
	@Autowired
	private AttachFileScalrDao attachFileScalrDao;
	
	@Autowired
	private FileClient fileClient;
	
	@Override
	public byte[] showLocalImage(String path) {
		if(StringUtils.isBlank(path)){
			return null;
		}
		return ImageUtils.getPhysicalFile(path);
	}
	
	@Override
	public byte[] read(String param) {
		if(StringUtils.isBlank(param)){
			return null;
		}
		try {
			String[] params = param.split("_q");
			byte[] img = null;
			//获取原图
			AttachFile af = attachFileDao.getById(params[0]);
			if(null == af){
				_infoLog.info(new StringBuffer("没有查询到原图数据，参数fileNo=").append(params[0]).toString());
				return null;
			}
			if(params.length > 1){
				//获取缩略图
				List<AttachFileScalr> list = attachFileScalrDao.findByAttachFileNo(params[0]);
				if(null == list){
					_infoLog.info(new StringBuffer("没有查询到缩略图数据，参数fileNo=").append(params[0]).toString());
					return null;
				}
				AttachFileScalr afs = null;
				for (AttachFileScalr attachFileScalr : list) {
					if(attachFileScalr.getCode().equals(params[1])){
						afs = attachFileScalr;
					}
				}
				if ( null == afs ) {
					_infoLog.info(new StringBuffer("没有查询到对应尺寸的缩略图数据，参数fileNo=").append(params[0]).append("zoom=").append(params[1]).toString());
					return null;
				} else {
					img = fileClient.getBytes(afs.getFileScalrNo(), afs.getScalrType());
					//没有在TFS上读取到图片，尝试从备份图片中恢复
					if(null == img){
						_infoLog.info(new StringBuffer("TFS图片丢失，fileNo=").append(afs.getFileScalrNo()).toString());
						String backup = ImageUtils.getBuckUpImage(af.getUserId(), af.getShop(), af.getCode(), afs.getFileScalrNo(), afs.getScalrType(), afs.getCode());
						if(StringUtils.isNotBlank(backup)){
							_infoLog.info(new StringBuffer("找到备份图片，尝试恢复到TFS，fileNo=").append(afs.getFileScalrNo()).toString());
							fileClient.put(backup, afs.getFileScalrNo(), afs.getScalrType());
							img = fileClient.getBytes(afs.getFileScalrNo(), afs.getScalrType());
							_infoLog.info(new StringBuffer("恢复到TFS成功，fileNo=").append(afs.getFileScalrNo()).toString());
						} else {
							_infoLog.info(new StringBuffer("未找到备份图片，无法恢复，fileNo=").append(afs.getFileScalrNo()).toString());
						}
					}
				}
			} else {
				//直接根据图片名读取TFS上的图片
				img = fileClient.getBytes(af.getFileNo(), af.getFileType());
				//没有在TFS上读取到图片，尝试从备份图片中恢复
				if(null == img){
					_infoLog.info(new StringBuffer("TFS图片丢失，fileNo=").append(af.getFileNo()).toString());
					String backup = ImageUtils.getBuckUpImage(af.getUserId(), af.getShop(), af.getCode(),af.getFileNo(),af.getFileType(),null);
					if(StringUtils.isNotBlank(backup)){
						_infoLog.info(new StringBuffer("找到备份图片，尝试恢复到TFS，fileNo=").append(af.getFileNo()).toString());
						fileClient.put(backup,af.getFileNo(), af.getFileType());
						img = fileClient.getBytes(af.getFileNo(), af.getFileType());
						_infoLog.info(new StringBuffer("恢复到TFS成功，fileNo=").append(af.getFileNo()).toString());
					} else {
						_infoLog.info(new StringBuffer("未找到备份图片，无法恢复，fileNo=").append(af.getFileNo()).toString());
					}
				}
			}
			return img;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
