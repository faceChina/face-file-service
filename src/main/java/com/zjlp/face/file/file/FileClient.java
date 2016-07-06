package com.zjlp.face.file.file;

import java.io.IOException;
import java.io.OutputStream;

import com.zjlp.face.file.file.exception.FileException;
/**
 * TFS客户端
 * @ClassName: FileClient 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author Administrator
 * @date 2015年1月4日 下午8:25:44
 */
public interface FileClient {
	
	/**
	 * 存入一个新的文件
	 * @Title: put 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param localfile 本地文件路径
	 * @param tfsSuffix	指定存入tfs的文件名后缀
	 * @return 存入tfs后的文件名,如果失败则返回null
	 * @date 2015年1月4日 下午7:57:16  
	 * @author dzq
	 */
	public String put(String localfile, String tfsSuffix) throws FileException;
	
	/**
	 * 存入一个新的文件
	 * @Title: put 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param localFile 本地文件名
	 * @param tfsFileName  tfs文件名，需要符合规范，如果非空则也可根据此名称访问tfs
	 * @param tfsSuffix 指定存入tfs的文件名后缀
	 * @return  存入tfs后的文件名,如果失败则返回null
	 * @throws FileException
	 * @date 2015年1月4日 下午8:30:27  
	 * @author Administrator
	 */
	public String put(String localFile, String tfsFileName, String tfsSuffix) throws FileException;
	
	/**
	 * 存入一个新的文件
	 * @Title: put 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param data 图片Byte数据
	 * @param tfsSuffix 指定存入tfs的文件名后缀
	 * @return 存入tfs后的文件名,如果失败则返回null
	 * @date 2015年1月4日 下午7:58:56  
	 * @author Administrator
	 */
	public String put(byte[] data, String tfsSuffix) throws FileException;
	
	/**
	 * 存入一个新的文件
	 * @Title: put 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param data 图片Byte数据
	 * @param tfsFileName tfs文件名，需要符合规范，如果非空则也可根据此名称访问tfs
	 * @param tfsSuffix 指定存入tfs的文件名后缀
	 * @return 存入tfs后的文件名,如果失败则返回null
	 * @date 2015年1月4日 下午8:31:25  
	 * @author Administrator
	 */
	public String put(byte[] data, String tfsFileName, String tfsSuffix);
	
	/**
	 * 获取tfs远程文件
	 * @Title: get 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param tfsFileName tfs远程文件名
	 * @param tfsSuffix tfs文件后缀名
	 * @param out  接收文件内容的OutputStream
	 * @return
	 * @date 2015年1月4日 下午8:02:11  
	 * @author Administrator
	 */
	public boolean get(String tfsFileName, String tfsSuffix, OutputStream out) throws FileException;
	
	/**
	 * 更新文件,如果不存在则直接存入
	 * @Title: update 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param localFile 本地文件名
	 * @param tfsFileName 需要更新的tfs文件名(指定需要更新tfs服务端的哪个文件).null则表示新增文件
	 * @param tfsSuffix 指定tfs的文件名后缀
	 * @return 更新后的文件名,如果失败则返回null
	 * @date 2015年1月4日 下午8:28:39  
	 * @author Administrator
	 */
	public String update(String localFile, String tfsFileName, String tfsSuffix) throws FileException;

	/**
	 * 更新文件,如果不存在则直接存入
	 * @Title: update 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param data 文件Byte数据
	 * @param tfsFileName 图片Byte数据 需要更新的tfs文件名(指定需要更新tfs服务端的哪个文件).null则表示新增文件
	 * @param tfsSuffix 指定tfs的文件名后缀
	 * @return 存入tfs后的文件名,如果失败则返回null
	 * @date 2015年1月4日 下午8:27:22  
	 * @author Administrator
	 */
	public String update(byte[] data, String tfsFileName, String tfsSuffix) throws FileException;
	
	/**
	 * 获取tfs远程文件
	 * @Title: get 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param tfsFileName tfs远程文件名
	 * @param tfsSuffix tfs文件后缀名
	 * @param localFile 指定保存到本地的文件名
	 * @return
	 * @date 2015年1月4日 下午8:03:36  
	 * @author Administrator
	 */
	public boolean get(String tfsFileName, String tfsSuffix, String localFile) throws FileException;
	
	/**
	 * 获取tfs远程文件,byte数组形式返回文件内容
	 * @Title: getBytes 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param tfsFileName  tfs远程文件名
	 * @param tfsSuffix tfs文件后缀名
	 * @return  以byte数组形式返回文件内容
	 * @date 2015年1月4日 下午8:15:41  
	 * @author Administrator
	 */
	public byte[] getBytes(String tfsFileName, String tfsSuffix) throws FileException,IOException;
	
	
	/**
	 * 删除tfs远程文件
	 * @Title: delete 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param tfsFileName tfs远程文件名
	 * @param tfsSuffix tfs文件后缀名
	 * @return 删除是否成功
	 * @date 2015年1月4日 下午8:16:32  
	 * @author Administrator
	 */
	public boolean delete(String tfsFileName, String tfsSuffix) throws FileException;
	
	/**
	 * 获取TFS远程文件状态
	 * @Title: stat 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param tfsFileName
	 * @return 成功返回FileStatus， 失败返回null
	 * @throws FileException
	 * @date 2015年1月5日 下午2:09:34  
	 * @author dzq
	 */
	public FileStatus stat(String tfsFileName) throws FileException;
	
	/**
	 * 获取TFS远程文件状态
	 * @Title: stat 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param tfsFileName
	 * @param tfsSuffix
	 * @return
	 * @throws FileException
	 * @date 2015年1月5日 下午2:10:16  
	 * @author dzq
	 */
	public FileStatus stat(String tfsFileName,String tfsSuffix) throws FileException;
}
