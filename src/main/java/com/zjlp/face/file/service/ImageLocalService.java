package com.zjlp.face.file.service;

public interface ImageLocalService {

	/**
	 * 显示剪切图片
	 * @Title: showCutImage 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param path
	 * @return
	 * @date 2015年1月8日 下午3:34:42  
	 * @author phb
	 */
	byte[] showLocalImage(String path);
	
	/**
	 * 读取图片
	 * @Title: read 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param param 由 fileNo+"_q"+zoom+"."+suffix eg:T1yaETBydT1R4xEV6K_q200_200.JPG
	 * @return
	 * @date 2015年1月5日 下午3:32:21  
	 * @author phb
	 */
	byte[] read(String param);
}
