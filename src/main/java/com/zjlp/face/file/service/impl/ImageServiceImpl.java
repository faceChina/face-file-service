package com.zjlp.face.file.service.impl;

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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.zjlp.face.file.dao.AttachFileDao;
import com.zjlp.face.file.dao.AttachFileScalrDao;
import com.zjlp.face.file.dao.FileOperateFailedRecordDao;
import com.zjlp.face.file.domain.AttachFile;
import com.zjlp.face.file.domain.AttachFileScalr;
import com.zjlp.face.file.domain.FileOperateFailedRecord;
import com.zjlp.face.file.dto.CutDto;
import com.zjlp.face.file.dto.FileBizParamDto;
import com.zjlp.face.file.dto.PressParamDto;
import com.zjlp.face.file.file.FileClient;
import com.zjlp.face.file.file.FileStatus;
import com.zjlp.face.file.file.exception.FileException;
import com.zjlp.face.file.service.ImageService;
import com.zjlp.face.file.util.ImageUtils;
import com.zjlp.face.file.util.PropertiesUtil;
//@Service("imageService")
public class ImageServiceImpl implements ImageService{

	private Logger _infoLog = Logger.getLogger("fileInfoLog");
	
	private Logger _errorLog = Logger.getLogger("fileErrorLog");
	
	@Autowired
	private AttachFileDao attachFileDao;
	
	@Autowired
	private AttachFileScalrDao attachFileScalrDao;
	
	@Autowired
	private FileOperateFailedRecordDao fileOperateFailedRecordDao;
	
	@Autowired
	private FileClient fileClient;
	
	@Override
	public String upload(byte[] img){
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
	public byte[] getFileByte(String path) {
		try {
			Assert.hasLength(path);
			String suffix = path.substring( path.lastIndexOf(".")+1 ).toUpperCase();
			if(path.indexOf(ImageUtils.SHOW_IMAGE) != -1){
				String fileNo = path.substring( path.lastIndexOf("/")+1,path.lastIndexOf(".") );
				_infoLog.info(new StringBuffer("fileNo:").append(fileNo).append(" suffix:").append(suffix).toString());
				byte[] img = fileClient.getBytes(fileNo, suffix);
				return img;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			_errorLog.error("根据文件名获取图片二进制发生异常", e);
			return null;
		}
	}

	@Override
	public String cut(CutDto cutDto) {
		try {
			if( null == cutDto ){
				_errorLog.error("参数为空");
				return null;
			}
			return ImageUtils.cut(cutDto);
		} catch (Exception e) {
			e.printStackTrace();
			_errorLog.error(new StringBuffer("切图失败：").append(JSONObject.fromObject(cutDto).toString()).toString(),e);
			return null;
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	public String addOrEdit(List<FileBizParamDto> reqParamDtos) throws FileException{
		Map<String,Object> flag = new HashMap<String, Object>();
		/**本事物添加的图片,用于发生异常时回滚*/
		List<AttachFile> addList = new ArrayList<AttachFile>();
		/**本事物待删除的文件,用于操作完成时删除TFS上的文件*/
		List<AttachFile> delList = new ArrayList<AttachFile>();
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
				AttachFile q_af = new AttachFile();
				q_af.setTableId( reqParamDto.getTableId() );
				q_af.setTableName( reqParamDto.getTableName() );
				q_af.setCode( reqParamDto.getCode() );
				q_af.setFileLabel(reqParamDto.getFileLabel());
				List<AttachFile> selList = attachFileDao.findByCodeAndTableId( q_af );
				delList.addAll( selList );
				
				if( "ubbFile".equals(reqParamDto.getCode())){
					if(StringUtils.isBlank(reqParamDto.getImgData())){
						continue;
					}
					/**处理UBB中上传图片的路径,以及文件*/
					Document doc = Jsoup.parse(reqParamDto.getImgData());
					Elements elements = doc.getElementsByTag("img");
					for (int i = 0; i < elements.size(); i++) {
						String src = elements.get(i).attr("src");
						if( src.indexOf(ImageUtils.SHOW_LOCAL_IMAGE_PATH) == -1 ){
							_infoLog.info(new StringBuffer("当前UBB图片为原有：").append(src));
							/**如果是已经上传的图片,则剔除匹配上的图片，删除未匹配到的图片*/
							String param = src.substring(src.lastIndexOf("/")+1,src.lastIndexOf("."));
							String[] params = param.split("_q");
							/**检查查询到的文件有多少被保留，将保留的文件从delList中剔除，剩余的将会被删除*/
							for (AttachFile attachFile : selList) {
								_checkDeleteParam(attachFile,reqParamDto);
								if(attachFile.getFileNo().equals(params[0])){
									delList.remove(attachFile);
								}
							}
						} else {
							//否者 ，就是需要添加的图片
							String[] path = src.split(ImageUtils.SPLIT_LOCAL_IMAGE_PATH);
							_infoLog.info(new StringBuffer("当前UBB图片为新增：").append(src).toString());
							AttachFile attachFile = _saveAttachFile(reqParamDto,path[1],i);
							String url = new StringBuffer(path[0]).append(ImageUtils.SHOW_IMAGE).append(attachFile.getFileNo()).append(".").append(attachFile.getFileType()).toString();
							elements.get(i).attr("src", url);
							elements.get(i).attr("title",new StringBuffer(attachFile.getFileNo()).append(".").append(attachFile.getFileType()).toString());
						}
					}
					reqParamDto.setImgData(doc.getElementsByTag("body").html());
				} else {
					String[] imgDatas = reqParamDto.getImgData().split(",");
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < imgDatas.length; i++) {
						if(StringUtils.isBlank(imgDatas[i]))continue;
						if(imgDatas[i].indexOf(ImageUtils.INIT_IMG_PATH) != -1)continue;
						//处理备份
						if(reqParamDto.getFileLabel().intValue() == 2){
							_infoLog.info(new StringBuffer("当前为业务备份图片：图片路径-").append(reqParamDto.getImgData())
									.append("业务表名-").append(reqParamDto.getTableName())
									.append("业务数据编号-").append(reqParamDto.getTableId())
									.toString());
							AttachFile s_af = _saveBackAttachFile( reqParamDto, imgDatas[i],i );
							addList.add( s_af );
							sb.append(ImageUtils.SHOW_IMAGE).append(s_af.getFileNo()).append(".").append(s_af.getFileType());
							if(imgDatas.length - i > 1){
								sb.append(",");
							}
							continue;
						}
						
						boolean check = true;
						for (AttachFile attachFile : selList) {
							/**验证数据合法性,主要为了保证没有匹配上待删除的数据的安全性*/
							_checkDeleteParam(attachFile,reqParamDto);
							if( imgDatas[i].indexOf( attachFile.getFileNo() ) != -1){
								/**如果当前图片还存在，则从待删除列表中踢出,剩余的将会被删除*/
								delList.remove( attachFile );
								/**修改当前图片的顺序*/
								_editSort( attachFile.getFileNo(),i );
								check = false;
								sb.append(ImageUtils.SHOW_IMAGE).append(attachFile.getFileNo()).append(".").append(attachFile.getFileType());
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
							AttachFile s_af = _saveAttachFile( reqParamDto, imgDatas[i],i );
							
							addList.add( s_af );
							sb.append(ImageUtils.SHOW_IMAGE).append(s_af.getFileNo()).append(".").append(s_af.getFileType());
							if(imgDatas.length - i > 1){
								sb.append(",");
							}
						}
					}
					reqParamDto.setImgData( sb.toString() );
				}
			}
			/**3.删除TFS上待删除的图片*/
			for (AttachFile attachFile : delList) {
				_deleteAttachFile(attachFile);
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
				for (AttachFile attachFile : addList) {
					_deleteAttachFile(attachFile);
				}
			} catch (Exception e1) {
				_errorLog.error("【严重】保存或修改发生异常执行回滚操作失败！！！！！",e);
			}
			throw new FileException("保存或修改发生异常",e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	public String remove(List<FileBizParamDto> reqParamDtos) throws FileException{
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
				AttachFile q_af = new AttachFile();
				q_af.setTableName(reqParamDto.getTableName());
				q_af.setTableId(reqParamDto.getTableId());
				q_af.setCode(reqParamDto.getCode());
				q_af.setFileLabel(reqParamDto.getFileLabel());
				List<AttachFile> selList = attachFileDao.findByCodeAndTableId(q_af);
				for (AttachFile attachFile : selList) {
					/**验证数据合法性,主要为了保证没有匹配上待删除的数据的安全性*/
					_checkDeleteParam(attachFile,reqParamDto);
					/**删除*/
					_deleteAttachFile(attachFile);
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
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	public String pressText(PressParamDto pressParamDto) throws FileException{
		Map<String,String> flag = new HashMap<String, String>();
		try {
			/**1.2检查参数完整性*/
			if( !_checkPressText( pressParamDto,flag ) ){
				flag.put("flag", "FAILED");
				String str = JSONObject.fromObject(flag).toString();
				_infoLog.info(str);
				return str;
			}
			/**1.3验证code有效性*/
			String code = pressParamDto.getCode();
			if( !"memberFile".equals( code ) ){
				flag.put("flag", "FAILED");
				flag.put("info", "CODE参数错误");
				String str = JSONObject.fromObject(flag).toString();
				_infoLog.info(str);
				return str;
			}
			/**2.1查询原图记录*/
			String fileNo = pressParamDto.getFileNo();
			AttachFile attachFile = attachFileDao.getById(fileNo);
			if( null == attachFile ){
				flag.put("flag", "FAILED");
				flag.put("info", "没有找到原图记录");
				String str = JSONObject.fromObject(flag).toString();
				_infoLog.info(str);
				return str;
			}
			/**2.2拿取文件*/
			byte[] file = fileClient.getBytes(fileNo, attachFile.getFileType());
			if(null == file){
				flag.put("flag", "FAILED");
				flag.put("info", "原图文件已不存在");
				String str = JSONObject.fromObject(flag).toString();
				_infoLog.info(str);
				return str;
			}
			/**2.3生成水印图片*/
			String textPath = ImageUtils.pressText( file, attachFile.getFileType(), pressParamDto.getFontText(),
					pressParamDto.getFontName(), pressParamDto.getFontSize(), pressParamDto.getFontColor(), 
					pressParamDto.getFontX(), pressParamDto.getFontY(), pressParamDto.getAlpha() );
			/**2.4删除原有的水印图片*/
			AttachFile af = new AttachFile();
			af.setCode( code );
			af.setTableId( pressParamDto.getTableId() );
			af.setTableName( pressParamDto.getTableName() );
			af.setFileLabel(pressParamDto.getFileLabel());
			List<AttachFile> list = attachFileDao.findByCodeAndTableId( af );
			for (int i = 0; i < list.size(); i++) {
				_deleteAttachFile( list.get(i) );
			}
			/**2.5上传图片到TFS,并保存记录到数据库*/
			_saveAttachFile( pressParamDto,textPath,0 );
			flag.put("flag", "SUCCESS");
			flag.put("info", "操作成功");
			return JSONObject.fromObject(flag).toString();
		} catch (Exception e) {
			e.printStackTrace();
			flag.put("flag", "FAILED");
			flag.put("info", "操作失败");
			_errorLog.error("添加水印文字发生异常",e);
			return JSONObject.fromObject(flag).toString();
		}
	}
	
	@Override
	public List<AttachFile> findByTableIdAndCode(AttachFile attachFile){
		return attachFileDao.findByCodeAndTableId(attachFile);
	}

	/**
	 * 保存图片
	 * @Title: _saveAttachFile
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param reqParamDto
	 * @param path
	 * @param sort
	 * @return
	 * @throws FileException
	 * @return AttachFile
	 * @author phb
	 * @date 2015年4月1日 下午2:49:30
	 */
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	private AttachFile _saveAttachFile(FileBizParamDto reqParamDto,String path,int sort) throws FileException{
		try {
			String suffix = path.substring( path.lastIndexOf(".")+1 ).toUpperCase();
			String tfsFileName = null;
			String oldtfsFileName = null;
			//代理别人商品的比人家图片处理
			if(path.indexOf(ImageUtils.SHOW_IMAGE) != -1){
				oldtfsFileName = path.substring( path.lastIndexOf("/")+1,path.lastIndexOf(".") );
				byte[] img = fileClient.getBytes(oldtfsFileName, suffix);
				tfsFileName = fileClient.put( img, suffix );
			}else{
				String phyPath = ImageUtils.getPhysicalPath( path );
				/**将原图上传到TFS*/
				tfsFileName = fileClient.put( phyPath, suffix );
			}
			
			if(StringUtils.isBlank( tfsFileName )){
				throw new FileException("保存图片失败");
			}
			Date date = new Date();
			/**获取图片信息*/
			FileStatus fileStatus = fileClient.stat( tfsFileName );
			String filePath = new StringBuffer(ImageUtils.SHOW_IMAGE).append(tfsFileName).append(".").append(suffix).toString();
			/**持久化到表ATTACH_FILE*/
			AttachFile attachFile = new AttachFile();
			attachFile.setFileNo( tfsFileName );
			attachFile.setSort( sort );
			attachFile.setUserId( reqParamDto.getUserId() );
			attachFile.setShop( reqParamDto.getShopNo() );
			attachFile.setFileType( suffix );
			attachFile.setFileSize( fileStatus.getLength()/1024 );
			attachFile.setTableName( reqParamDto.getTableName() );
			attachFile.setTableId( reqParamDto.getTableId() );
			attachFile.setCode( reqParamDto.getCode() );
			attachFile.setFileLabel( reqParamDto.getFileLabel() );
			attachFile.setFilePath(filePath);
			attachFile.setUpdateTime( date );
			attachFile.setCreateTime( date );
			attachFileDao.add( attachFile );
			/**备份图片*/
			byte[] backByte = fileClient.getBytes(tfsFileName, suffix);
			try {
				ImageUtils.backup(backByte, attachFile.getUserId(), attachFile.getShop(), 
						attachFile.getCode(), tfsFileName, suffix, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String zoomSizes = reqParamDto.getZoomSizes();
			//如果参数不为空，则需要生成缩略图
			if(StringUtils.isNotBlank( zoomSizes )){
				for (String widht_height : zoomSizes.split( "," )) {
					String tfsFileSaclrName = null;
					String zoomPath = null;
					if(path.indexOf(ImageUtils.SHOW_IMAGE) != -1){
						byte[] img = fileClient.getBytes(oldtfsFileName, suffix);
						zoomPath = ImageUtils.zoomImg( img,suffix,widht_height );
					}else{
						String phyPath = ImageUtils.getPhysicalPath( path );
						//生成缩略图
						zoomPath = ImageUtils.zoomImg( phyPath,widht_height );
					}
					//上传缩略图
					tfsFileSaclrName = fileClient.put( zoomPath, suffix );
					
					if(StringUtils.isBlank( tfsFileSaclrName )){
						throw new FileException("保存缩略图失败");
					}
					//获取图片信息
					FileStatus fileSaclrStatus = fileClient.stat( tfsFileSaclrName );
					//缩略图持久化
					AttachFileScalr attachFileScalr = new AttachFileScalr();
					attachFileScalr.setFileScalrNo( tfsFileSaclrName );
					attachFileScalr.setAttachFileNo( tfsFileName );
					attachFileScalr.setUserId( reqParamDto.getUserId() );
					attachFileScalr.setShop( reqParamDto.getShopNo() );
					attachFileScalr.setScalrType( suffix );
					attachFileScalr.setScalrSize( fileSaclrStatus.getLength()/1024 );
					attachFileScalr.setTableName( reqParamDto.getTableName() );
					attachFileScalr.setTableId( reqParamDto.getTableId() );
					attachFileScalr.setCode( widht_height );
					attachFileScalr.setFileLabel( reqParamDto.getFileLabel() );
					attachFileScalr.setUpdateTime( date );
					attachFileScalr.setCreateTime( date );
					attachFileScalrDao.add( attachFileScalr );
					/**备份图片*/
					byte[] zoombackByte = fileClient.getBytes(tfsFileSaclrName, suffix);
					try {
						ImageUtils.backup(zoombackByte, attachFile.getUserId(), attachFile.getShop(), 
								attachFile.getCode(), tfsFileSaclrName, suffix, attachFileScalr.getCode());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return attachFile;
		} catch (Exception e) {
			_errorLog.error("保存图片失败",e);
			throw new FileException(e);
		}
	}
	
	/**
	 * 保存已有的备份图片
	 * @Title: _saveBackAttachFile
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param reqParamDto
	 * @param path
	 * @param sort
	 * @return
	 * @throws FileException
	 * @return AttachFile
	 * @author phb
	 * @date 2015年4月1日 下午2:49:38
	 */
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	private AttachFile _saveBackAttachFile(FileBizParamDto reqParamDto,String path,int sort) throws FileException{
		try {
			String suffix = path.substring( path.lastIndexOf(".")+1 ).toUpperCase();
			String oldtfsFileName = path.substring( path.lastIndexOf("/")+1,path.lastIndexOf(".") );
			byte[] img = fileClient.getBytes(oldtfsFileName, suffix);
			String tfsFileName = fileClient.put( img, suffix );
			if(StringUtils.isBlank( tfsFileName )){
				throw new FileException("保存图片失败");
			}
			Date date = new Date();
			/**获取图片信息*/
			FileStatus fileStatus = fileClient.stat( tfsFileName );
			String filePath = new StringBuffer(ImageUtils.SHOW_IMAGE).append(tfsFileName).append(".").append(suffix).toString();
			/**持久化到表ATTACH_FILE*/
			AttachFile attachFile = new AttachFile();
			attachFile.setFileNo( tfsFileName );
			attachFile.setSort( sort );
			attachFile.setUserId( reqParamDto.getUserId() );
			attachFile.setShop( reqParamDto.getShopNo() );
			attachFile.setFileType( suffix );
			attachFile.setFileSize( fileStatus.getLength()/1024 );
			attachFile.setTableName( reqParamDto.getTableName() );
			attachFile.setTableId( reqParamDto.getTableId() );
			attachFile.setCode( reqParamDto.getCode() );
			attachFile.setFileLabel( reqParamDto.getFileLabel() );
			attachFile.setFilePath(filePath);
			attachFile.setUpdateTime( date );
			attachFile.setCreateTime( date );
			attachFileDao.add( attachFile );
			/**备份图片*/
			byte[] backByte = fileClient.getBytes(tfsFileName, suffix);
			try {
				ImageUtils.backup(backByte, attachFile.getUserId(), attachFile.getShop(), 
						attachFile.getCode(), tfsFileName, suffix, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<AttachFileScalr> afss = attachFileScalrDao.findByAttachFileNo(oldtfsFileName);
			
			//将原图的缩略图备份
			for (AttachFileScalr afs : afss) {
				byte[] imgscalr = fileClient.getBytes(afs.getFileScalrNo(), afs.getScalrType());
				//上传缩略图
				String tfsFileSaclrName = fileClient.put( imgscalr, suffix );
				if(StringUtils.isBlank( tfsFileSaclrName )){
					throw new FileException("保存缩略图失败");
				}
				//获取图片信息
				FileStatus fileSaclrStatus = fileClient.stat( tfsFileSaclrName );
				//缩略图持久化
				AttachFileScalr attachFileScalr = new AttachFileScalr();
				attachFileScalr.setFileScalrNo( tfsFileSaclrName );
				attachFileScalr.setAttachFileNo( tfsFileName );
				attachFileScalr.setUserId( reqParamDto.getUserId() );
				attachFileScalr.setShop( reqParamDto.getShopNo() );
				attachFileScalr.setScalrType( suffix );
				attachFileScalr.setScalrSize( fileSaclrStatus.getLength()/1024 );
				attachFileScalr.setTableName( reqParamDto.getTableName() );
				attachFileScalr.setTableId( reqParamDto.getTableId() );
				attachFileScalr.setCode( afs.getCode() );
				attachFileScalr.setFileLabel( reqParamDto.getFileLabel() );
				attachFileScalr.setUpdateTime( date );
				attachFileScalr.setCreateTime( date );
				attachFileScalrDao.add( attachFileScalr );
				/**备份图片*/
				byte[] zoombackByte = fileClient.getBytes(tfsFileSaclrName, suffix);
				try {
					ImageUtils.backup(zoombackByte, attachFile.getUserId(), attachFile.getShop(), 
							attachFile.getCode(), tfsFileSaclrName, suffix, attachFileScalr.getCode());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return attachFile;
		} catch (Exception e) {
			_errorLog.error("保存图片失败",e);
			throw new FileException(e);
		}
	}
	
	/**
	 * 删除文件和数据
	 * @Title: _deleteAttachFile 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param attachFile
	 * @date 2015年1月15日 下午3:50:02  
	 * @author phb
	 */
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	private void _deleteAttachFile(AttachFile attachFile) throws FileException{
		List<AttachFileScalr> scalrs = null;
		//数据操作，失败回滚
		try {
			//查询当前文件的缩略图记录
			scalrs = attachFileScalrDao.findByAttachFileNo(attachFile.getFileNo());
			for (AttachFileScalr attachFileScalr : scalrs) {
				//删除缩略图数据
				attachFileScalrDao.delete(attachFileScalr.getFileScalrNo());
			}
			//删除原图数据
			attachFileDao.delete(attachFile.getFileNo());
		} catch (Exception e) {
			e.printStackTrace();
			_errorLog.error("删除图片数据失败",e);
			throw new FileException("删除文件数据发生异常",e);
		}
		//文件操作，失败不回滚，只记录
		try {
			for (AttachFileScalr attachFileScalr : scalrs) {
				//删除缩略图文件
				fileClient.delete(attachFileScalr.getFileScalrNo(), attachFileScalr.getScalrType());
			}
			//删除TFS上图片文件
			fileClient.delete(attachFile.getFileNo(), attachFile.getFileType());
		} catch (Exception e) {
			_errorLog.error("删除图片文件失败",e);
			e.printStackTrace();
		} finally {
			/** 在方法执行完成后检查文件的状态*/
			FileStatus fileStatus = fileClient.stat(attachFile.getFileNo(), attachFile.getFileType());
			if(null != fileStatus && 0 == fileStatus.getFlag() ){
				//记录当前没有被删除的图片
				_saveFileOperateFailedRecord(attachFile.getFileNo(), attachFile.getFileType());
			}
			if( null != scalrs ){
				for (AttachFileScalr attachFileScalr : scalrs) {
					FileStatus scalrsStatus = fileClient.stat(attachFileScalr.getFileScalrNo(), attachFileScalr.getScalrType());
					if(null != fileStatus && 0 == scalrsStatus.getFlag() ){
						//记录当前没有被删除的图片
						_saveFileOperateFailedRecord(attachFileScalr.getFileScalrNo(), attachFileScalr.getScalrType());
					}
				}
			}
		}
	}
	/***
	 * 保存删除文件失败记录
	 * @Title: _saveFileOperateFailedRecord 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param fileNo
	 * @param fileType
	 * @throws FileException
	 * @date 2015年1月15日 下午5:28:55  
	 * @author phb
	 */
	private void _saveFileOperateFailedRecord(String fileNo,String fileType) throws FileException{
		Date date = new Date();
		FileOperateFailedRecord record = new FileOperateFailedRecord();
		record.setFileNo(fileNo);
		record.setFileType(fileType);
		record.setOperate(3);
		record.setStatus(1);
		record.setCreateTime(date);
		record.setUpdateTime(date);
		fileOperateFailedRecordDao.add(record);
	}
	
	@Transactional(propagation = Propagation.REQUIRED,rollbackForClassName = { "FileException" })
	private void _editSort(String fileNo,int sort){
		AttachFile e_af = new AttachFile();
		e_af.setFileNo( fileNo );
		e_af.setSort( sort );
		e_af.setUpdateTime( new Date() );
		attachFileDao.edit(e_af);
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
	
	/**
	 * 验证code 的有效性
	 * @Title: _checkCode 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param code
	 * @return
	 * @date 2015年1月6日 下午4:27:54  
	 * @author phb
	 */
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
	
	private Boolean _checkPressText(PressParamDto pressParamDto,Map<String,String> flag){
		if( null == pressParamDto ){
			flag.put("info", "参数为空");
			return false;
		}
		if( null == pressParamDto.getUserId() ){
			flag.put("info", "参数userId为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getTableName()) ){
			flag.put("info", "参数tableName为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getTableId()) ){
			flag.put("info", "参数tableId为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getCode()) ){
			flag.put("info", "参数code为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getFileNo()) ){
			flag.put("info", "参数fileNo为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getFontText()) ){
			flag.put("info", "参数FontText为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getFontName()) ){
			flag.put("info", "参数FontName为空");
			return false;
		}
		if( null == pressParamDto.getFontSize() ){
			flag.put("info", "参数FontSize为空");
			return false;
		}
		if( StringUtils.isBlank(pressParamDto.getFontColor()) ){
			flag.put("info", "参数fontColor为空");
			return false;
		}
		if( null == pressParamDto.getFontX() ){
			flag.put("info", "参数FontX为空");
			return false;
		}
		if( null == pressParamDto.getFontY() ){
			flag.put("info", "参数FontY为空");
			return false;
		}
		if( null == pressParamDto.getAlpha() ){
			flag.put("info", "参数Alpha为空");
			return false;
		}
		return true;
	}
	
	private void _checkDeleteParam(AttachFile attachFile, FileBizParamDto reqParamDto) throws FileException{
		if( attachFile.getUserId().longValue() != reqParamDto.getUserId().longValue() ){
			_errorLog.info(new StringBuffer("当前用户不匹配，属于非法请求"));
			throw new FileException( "当前用户不匹配" );
		}
		if( null != attachFile.getShop() && !attachFile.getShop().equals(reqParamDto.getShopNo()) ){
			_errorLog.info(new StringBuffer("当前店铺不匹配，属于非法请求"));
			throw new FileException( "当前店铺不匹配" );
		}
	}
}
