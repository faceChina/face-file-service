package com.zjlp.face.file.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zjlp.face.file.dto.QiniuUploadRet;

public class QiniuFileUtil {
	private static Logger _infoLog = Logger.getLogger("fileInfoLog");
	private static Logger _errorLog = Logger.getLogger("fileErrorLog");
	
	private static final String ACCESS_KEY = "qiniu.access.key";
	private static final String SECRET_KEY = "qiniu.secret.key";
	/**文件空间*/
	private static final String BUCKET = PropertiesUtil.getContexrtParam("qiniu.zone.name");
	
	private Auth auth = null;
	private BucketManager bucketManager = null;
	
	private Auth getAuth() throws Exception{
		if(null == this.auth){
			String accessKey = PropertiesUtil.getContexrtParam(ACCESS_KEY);
			if(StringUtil.isBlank(accessKey)){
				throw new Exception("未配置qiniu.access.key参数");
			}
			String secretKey = PropertiesUtil.getContexrtParam(SECRET_KEY);
			if(StringUtil.isBlank(secretKey)){
				throw new Exception("未配置qiniu.secret.key参数");
			}
			auth = Auth.create(accessKey, secretKey);
		}
		return auth;
	}
	
	private BucketManager getBucketManager() throws Exception{
		if(null == this.bucketManager){
			bucketManager = new BucketManager(getAuth());
		}
		return bucketManager;
	}
	/**
	 * 上传文件
	 * @Title: upload 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param file
	 * @param key
	 * @return
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	public QiniuUploadRet upload(File file,String key) throws Exception{
	    try {
	    	UploadManager uploadManager = new UploadManager();
	        Response res = uploadManager.put(file, key, getUpToken());
	        QiniuUploadRet ret = res.jsonToObject(QiniuUploadRet.class);
	        _infoLog.info(res.toString());
	        _infoLog.info(res.bodyString());
	        return ret;
	    } catch (QiniuException e) {
	        Response r = e.response;
	        // 请求失败时简单状态信息
	        _errorLog.error(r.toString());
	        throw new Exception(r.toString());
	    }
	}
	
	/**
	 * 拷贝图片
	 * @Title: copy 
	 * @Description: (用于如订单图片的备份数据文件) 
	 * @param key
	 * @param tagKey
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	public void copy(String key,String tagKey) throws Exception{
		getBucketManager().copy(BUCKET, key, BUCKET, tagKey);
	}
	
	/**
	 * 删除文件
	 * @Title: remove 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param key
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	public void remove(String key) throws Exception{
		getBucketManager().delete(BUCKET, key);
	}
	
	/**
	 * 查看文件属性
	 * @Title: getFileInfo 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param key
	 * @return
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	public FileInfo getFileInfo(String key) throws Exception{
		return getBucketManager().stat(BUCKET, key);
	}
	
	/**
	 * 获取上传凭证
	 * @Title: getUpToken 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @return
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	private String getUpToken() throws Exception{
	    return getAuth().uploadToken(BUCKET, null, 3600, new StringMap()
	            .putNotEmpty("returnBody", "{\"key\": $(key), \"hash\": $(etag), \"width\": $(imageInfo.width), \"height\": $(imageInfo.height)}"));
	}
	
	/***
	 * 获取KEY
	 * @Title: getKey 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param userId
	 * @param shopNo
	 * @param code
	 * @param fileName
	 * @return
	 * @author Hongbo Peng
	 */
	public String getKey(Long userId,String shopNo,String code,String fileName){
		StringBuffer sb = new StringBuffer("image/");
		sb.append(String.valueOf(userId));
		sb.append("/");
		if(StringUtils.isNotBlank(shopNo)){
			sb.append(shopNo).append("/");
		}
		sb.append(code).append("/").append(fileName);
		return sb.toString();
	}
}
