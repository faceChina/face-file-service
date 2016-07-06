package com.zjlp.face.file.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qiniu.storage.model.FileInfo;
import com.zjlp.face.file.dao.CloudstFileDao;
import com.zjlp.face.file.domain.AttachFile;
import com.zjlp.face.file.domain.CloudstFile;
import com.zjlp.face.file.dto.CutDto;
import com.zjlp.face.file.dto.FileBizParamDto;
import com.zjlp.face.file.dto.PressParamDto;
import com.zjlp.face.file.file.exception.FileException;
import com.zjlp.face.file.service.ImageService;
import com.zjlp.face.file.util.ImageUtils;
import com.zjlp.face.file.util.PropertiesUtil;
import com.zjlp.face.file.util.QiniuFileUtil;
/***
 * 云存储图片服务
 * @author Hongbo Peng
 *
 */
@Service("imageService")
public class CloudstImageServiceImpl implements ImageService {

	private Logger _infoLog = Logger.getLogger("fileInfoLog");
	
	private Logger _errorLog = Logger.getLogger("fileErrorLog");
	
	@Autowired
	private CloudstFileDao cloudstFileDao;
	
	@Override
	public String upload(byte[] img) {
		Map<String,String> flag = new HashMap<String,String>();
		if(null == img ){
			_infoLog.info("上传图片参数为空");
			flag.put("flag", "FAILED");
			return JSONObject.fromObject(flag).toString();
		}
		try {
			return ImageUtils.upload(img);
		} catch (Exception e) {
			e.printStackTrace();
			_errorLog.error("上传图片失败",e);
			flag.put("flag", "FAILED");
			return JSONObject.fromObject(flag).toString();
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String addOrEdit(List<FileBizParamDto> reqParamDtos)
			throws FileException {
		Map<String,Object> flag = new HashMap<String, Object>();
		/**本事物添加的图片,用于发生异常时回滚*/
		List<CloudstFile> addList = new ArrayList<CloudstFile>();
		/**本事物待删除的文件,用于操作完成时删除TFS上的文件*/
		List<CloudstFile> delList = new ArrayList<CloudstFile>();
		/**1.验证参数*/
		if( !_checkAddOrEdit( reqParamDtos,flag ) ){
			flag.put( "flag", "FAILED" );
			String str = JSONObject.fromObject( flag ).toString();
			_infoLog.info( str );
			return str;
		}
		try {
			for (FileBizParamDto reqParamDto : reqParamDtos) {
				/**2.查询已有的图片数据*/
				CloudstFile q_af = new CloudstFile();
				q_af.setTableId( reqParamDto.getTableId() );
				q_af.setTableName( reqParamDto.getTableName() );
				q_af.setCode( reqParamDto.getCode() );
				q_af.setFileLabel(reqParamDto.getFileLabel());
				List<CloudstFile> selList = cloudstFileDao.findByCodeAndTableId( q_af );
				delList.addAll( selList );
				
				if( "ubbFile".equals(reqParamDto.getCode())){
					if(StringUtils.isBlank(reqParamDto.getImgData())){
						continue;
					}
					/**处理UBB中上传图片的路径,以及文件*/
					Document doc = Jsoup.parse(reqParamDto.getImgData());
					Elements elements = doc.getElementsByTag("img");
					String host = PropertiesUtil.getContexrtParam("qiniu.host.url");
					if(StringUtils.isBlank(host)){
						throw new FileException("参数qiniu.host.url未配置");
					}
					for (int i = 0; i < elements.size(); i++) {
						String src = elements.get(i).attr("src");
						if( src.indexOf(ImageUtils.SHOW_LOCAL_IMAGE_PATH) == -1 ){
							_infoLog.info(new StringBuffer("当前UBB图片为原有：").append(src));
							/**如果是已经上传的图片,则剔除匹配上的图片，删除未匹配到的图片*/
							/**检查查询到的文件有多少被保留，将保留的文件从delList中剔除，剩余的将会被删除*/
							for (CloudstFile file : selList) {
								_checkDeleteParam(file,reqParamDto);
								_infoLog.info("---------UBB文件路径对比："+host+file.getFilePath() +"         "+src);
								if((host+file.getFilePath()).equals(src)){
									delList.remove(file);
								}
							}
						} else {
							//否者 ，就是需要添加的图片
							String[] path = src.split(ImageUtils.SPLIT_LOCAL_IMAGE_PATH);
							_infoLog.info(new StringBuffer("当前UBB图片为新增：").append(src).toString());
							CloudstFile attachFile = _saveCloudstFile(reqParamDto,path[1],i);
							
							elements.get(i).attr("src", new StringBuffer(host).append(attachFile.getFilePath()).toString());
							elements.get(i).attr("title",attachFile.getFilePath());
						}
					}
					reqParamDto.setImgData(doc.getElementsByTag("body").html());
				} else {
					String[] imgDatas = reqParamDto.getImgData().split(",");
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < imgDatas.length; i++) {
						if(StringUtils.isBlank(imgDatas[i]))continue;
						if(imgDatas[i].indexOf(ImageUtils.INIT_IMG_PATH) != -1)continue;
						
						boolean check = true;
						for (CloudstFile file : selList) {
							/**验证数据合法性,主要为了保证没有匹配上待删除的数据的安全性*/
							_checkDeleteParam(file,reqParamDto);
							_infoLog.info("---------文件路径对比："+imgDatas[i] +"         "+file.getFilePath());
							if( imgDatas[i].indexOf( file.getFilePath() ) != -1 ){
								/**如果当前图片还存在，则从待删除列表中踢出,剩余的将会被删除*/
								delList.remove( file );
								/**修改当前图片的顺序*/
								_editSort( file.getId(),i );
								check = false;
								sb.append(file.getFilePath());
								if(imgDatas.length - i > 1){
									sb.append(",");
								}
							}
						}
						if(check){
							/**新增的图片*/
							_infoLog.info(new StringBuffer("当前为新增图片：图片路径-").append(reqParamDto.getImgData())
									.append("业务表名-").append(reqParamDto.getTableName())
									.append("业务数据编号-").append(reqParamDto.getTableId())
									.toString());
							CloudstFile s_af = _saveCloudstFile( reqParamDto, imgDatas[i],i );
							
							addList.add( s_af );
							sb.append(s_af.getFilePath());
							if(imgDatas.length - i > 1){
								sb.append(",");
							}
						}
					}
					reqParamDto.setImgData( sb.toString() );
				}
			}
			/**3.删除TFS上待删除的图片*/
			for (CloudstFile attachFile : delList) {
				_deleteCloudstFile(attachFile);
			}
			flag.put("data", reqParamDtos);
			flag.put("flag", "SUCCESS");
			flag.put("info", "操作成功");
			return JSONObject.fromObject(flag).toString();
		} catch (Exception e) {
			e.printStackTrace();
			_errorLog.error("保存或修改发生异常");
			/**回滚本次添加的图片:添加的记录回滚，添加的图片删除*/
			try {
				for (CloudstFile file : addList) {
					_deleteCloudstFile(file);
				}
			} catch (Exception e1) {
				_errorLog.error("【严重】保存或修改发生异常执行回滚操作失败！！！！！",e);
			}
			throw new FileException("保存或修改发生异常",e);
		}
	}

	/**
	 * 删除图片
	 * @Title: _deleteCloudstFile 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param attachFile
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void _deleteCloudstFile(CloudstFile attachFile) throws Exception{
		cloudstFileDao.delete(attachFile.getId());
		try {
			QiniuFileUtil qiniuUtil = new QiniuFileUtil();
			String key = attachFile.getFilePath().substring(1);
			qiniuUtil.remove(key);
		} catch (Exception e) {
			_errorLog.error(e);
			_errorLog.error("删除云存储上图片失败："+attachFile.getFilePath());
		}
	}

	/**
	 * 保存图片
	 * @Title: _saveCloudstFile 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param reqParamDto
	 * @param path
	 * @param sort
	 * @return
	 * @throws Exception
	 * @author Hongbo Peng
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private CloudstFile _saveCloudstFile(FileBizParamDto reqParamDto,
			String path, int sort) throws Exception{
		String suffix = path.substring( path.lastIndexOf(".")+1 ).toUpperCase();
		String imageName = ImageUtils.randomImageName(suffix);
		QiniuFileUtil qiniuUtil = new QiniuFileUtil();
		String key = qiniuUtil.getKey(reqParamDto.getUserId(), reqParamDto.getShopNo(), reqParamDto.getCode(), imageName);
		
		Date date = new Date();
		CloudstFile cloudstFile = new CloudstFile();
		cloudstFile.setUserId(reqParamDto.getUserId());
		cloudstFile.setShop(reqParamDto.getShopNo());
		cloudstFile.setFileType(suffix);
		cloudstFile.setTableName(reqParamDto.getTableName());
		cloudstFile.setTableId(reqParamDto.getTableId());
		cloudstFile.setFilePath("/"+key);
		cloudstFile.setCode(reqParamDto.getCode());
		cloudstFile.setSort(sort);
		cloudstFile.setFileLabel(reqParamDto.getFileLabel());
		cloudstFile.setCreateTime(date);
		cloudstFile.setUpdateTime(date);
		
		FileInfo fileInfo = null;
		if(reqParamDto.getFileLabel().intValue() == 1){
			File file = new File(ImageUtils.getPhysicalPath(path));
			if(!file.isFile()){
				throw new FileException("上传目标图片"+path+"不存在");
			}
			qiniuUtil.upload(file, key);
			fileInfo = qiniuUtil.getFileInfo(key);
		} else if(reqParamDto.getFileLabel().intValue() == 2){
			String fileKey = path.substring(1);
			fileInfo = qiniuUtil.getFileInfo(fileKey);
			if(null == fileInfo){
				throw new FileException("备份目标图片"+path+"不存在");
			}
			qiniuUtil.copy(fileKey, key);
		} else {
			throw new FileException("FileLabel值异常");
		}
		cloudstFile.setFileSize(fileInfo.fsize);
		cloudstFileDao.add(cloudstFile);
		return cloudstFile;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	private void _editSort(Long id,int sort){
		CloudstFile e_af = new CloudstFile();
		e_af.setId(id);
		e_af.setSort( sort );
		e_af.setUpdateTime( new Date() );
		cloudstFileDao.edit(e_af);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String remove(List<FileBizParamDto> reqParamDtos)
			throws FileException {
		Map<String,String> flag = new HashMap<String, String>();
		/**1.验证参数*/
		if( !_checkRemove( reqParamDtos,flag ) ){
			flag.put( "flag", "FAILED" );
			String str = JSONObject.fromObject( flag ).toString();
			_infoLog.info( str );
			return str;
		}
		try {
			for (FileBizParamDto reqParamDto : reqParamDtos) {
				/**2.查询要删除的数据*/
				CloudstFile q_af = new CloudstFile();
				q_af.setTableName(reqParamDto.getTableName());
				q_af.setTableId(reqParamDto.getTableId());
				q_af.setCode(reqParamDto.getCode());
				q_af.setFileLabel(reqParamDto.getFileLabel());
				List<CloudstFile> selList = cloudstFileDao.findByCodeAndTableId(q_af);
				for (CloudstFile file : selList) {
					/**验证数据合法性,主要为了保证没有匹配上待删除的数据的安全性*/
					_checkDeleteParam(file,reqParamDto);
					/**删除*/
					_deleteCloudstFile(file);
				}
			}
			flag.put( "flag", "SUCCESS" );
			String str = JSONObject.fromObject( flag ).toString();
			_infoLog.info( str );
			return str;
		} catch (Exception e) {
			throw new FileException("删除图片发生异常",e);
		}
	}
	
	private Boolean _checkAddOrEdit(List<FileBizParamDto> fileBizParamDtos,Map<String,Object> flag){
		if( null == fileBizParamDtos || fileBizParamDtos.size() == 0 ){
			flag.put("info", "参数为空");
			return false;
		}
		for (FileBizParamDto reqParamDto : fileBizParamDtos) {
			if( null == reqParamDto.getUserId() ){
				flag.put("info", "参数UserId为空");
				return false;
			}
			if( StringUtils.isBlank(reqParamDto.getTableName()) ){
				flag.put("info", "参数TableName为空");
				return false;
			}
			if( StringUtils.isBlank(reqParamDto.getTableId()) ){
				flag.put("info", "参数TableId为空");
				return false;
			}
			if( StringUtils.isBlank(reqParamDto.getCode()) ){
				flag.put("info", "参数Code为空");
				return false;
			}
			if( !_checkCode(reqParamDto.getCode()) ){
				flag.put("info", "参数Code无效");
				return false;
			}
			if(!"ubbFile".equals(reqParamDto.getCode()) && StringUtils.isBlank(reqParamDto.getImgData()) ){
				flag.put("info", "参数ImgData为空");
				return false;
			}
			if( 1 != reqParamDto.getFileLabel() && 2 != reqParamDto.getFileLabel() ){
				flag.put("info", "参数fileLabel无效");
				return false;
			}
		}
		return true;
	}
	
	private Boolean _checkCode(String code){
		String pcode = PropertiesUtil.getContexrtParam("code");
		_infoLog.info("code:"+code+"  pcode:"+pcode);
		if(StringUtils.isBlank(pcode)){
			return false;
		}
		String[] codes = pcode.split(",");
		for (String string : codes) {
			if(string.equals(code)){
				return true;
			}
		}
		return false;
	}
	
	private void _checkDeleteParam(CloudstFile file, FileBizParamDto reqParamDto) throws FileException{
		if( file.getUserId().longValue() != reqParamDto.getUserId().longValue() ){
			_errorLog.info(new StringBuffer("当前用户不匹配，属于非法请求"));
			throw new FileException( "当前用户不匹配" );
		}
		if( null != file.getShop() && !file.getShop().equals(reqParamDto.getShopNo()) ){
			_errorLog.info(new StringBuffer("当前店铺不匹配，属于非法请求"));
			throw new FileException( "当前店铺不匹配" );
		}
	}

	private Boolean _checkRemove(List<FileBizParamDto> fileBizParamDtos,Map<String,String> flag){
		if( null == fileBizParamDtos || fileBizParamDtos.size() == 0 ){
			flag.put("info", "参数为空");
			return false;
		}
		for (FileBizParamDto reqParamDto : fileBizParamDtos) {
			if( null == reqParamDto.getUserId() ){
				flag.put("info", "参数UserId为空");
				return false;
			}
			if( StringUtils.isBlank(reqParamDto.getTableName()) ){
				flag.put("info", "参数TableName为空");
				return false;
			}
			if( StringUtils.isBlank(reqParamDto.getTableId()) ){
				flag.put("info", "参数TableId为空");
				return false;
			}
			if( StringUtils.isBlank(reqParamDto.getCode()) ){
				flag.put("info", "参数Code为空");
				return false;
			}
			if( !_checkCode(reqParamDto.getCode()) ){
				flag.put("info", "参数Code无效");
				return false;
			}
		}
		return true;
	}
	
	
	
	/************************************************/
	@Override
	public byte[] getFileByte(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String cut(CutDto cutDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String pressText(PressParamDto pressParamDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AttachFile> findByTableIdAndCode(AttachFile attachFile) {
		// TODO Auto-generated method stub
		return null;
	}


}
