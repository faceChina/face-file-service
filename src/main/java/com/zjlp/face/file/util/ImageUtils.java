package com.zjlp.face.file.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.zjlp.face.file.dto.CutDto;
import com.zjlp.face.file.file.exception.FileException;
import com.zjlp.face.file.util.enums.FileType;

public class ImageUtils {
	
	private static Logger _infoLog = Logger.getLogger("fileInfoLog");
	
	private static Logger _errorLog = Logger.getLogger("fileErrorLog");

	private static final String CLASS_PATH = "/WEB-INF/classes/";

	/**上传临时目录 用于保存图片*/
	private static final String TEMP_PATH = new StringBuffer(File.separator)
			.append("resources").append(File.separator).append("upload")
			.append(File.separator).append("temp").toString();
	/**上传临时目录 用于访问图片*/
	private static final String TEMP_PATH_SHOW = "/resources/upload/temp";
	/**截图临时目录 保存*/
	private static final String CUT_PATH = new StringBuffer(File.separator)
			.append("resources").append(File.separator).append("upload")
			.append(File.separator).append("cut").toString();
	/**截图临时目录 访问*/
	private static final String CUT_PATH_SHOW = "/resources/upload/cut";
	/**缩放图片目录*/
	private static final String ZOOM_PATH = new StringBuffer(File.separator)
			.append("resources").append(File.separator).append("upload")
			.append(File.separator).append("zoom").toString();
	/**水印文字图片*/
	private static final String PRESS_PATH =  new StringBuffer(File.separator)
			.append("resources").append(File.separator).append("upload")
			.append(File.separator).append("press").toString();
	/**本地临时图片访问路径拼接*/
	public static final String SHOW_LOCAL_IMAGE_PATH = "/showLocalImg.do?path=";

	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyyMMdd");
	//用于处理UBB图片路径截取
	public static final String SPLIT_LOCAL_IMAGE_PATH = "showLocalImg.do\\?path=";
	//用于拼接显示图片路径
	public static final String SHOW_IMAGE = "/image/";
	/**项目本地图片*/
	public static final String INIT_IMG_PATH = "/resource/base/img/";
	
	/**
	 * 备份图片
	 * @Title: backup
	 * @Description: (图片上传TFS时，备份一个到fileservice本地)
	 * @param img
	 * @param userId
	 * @param shopNo
	 * @param code
	 * @param fileName
	 * @return
	 * @return Boolean
	 * @author phb
	 * @date 2015年3月4日 上午10:54:08
	 */
	public static Boolean backup(byte[] img,Long userId,String shopNo,String code,String fileName,String fileType,String zoom) throws FileException{
		//获取备份图片路径
		String backupPath = PropertiesUtil.getContexrtParam("image.backup.url");
		if(StringUtils.isBlank(backupPath)){
			_errorLog.error("未配置备份文件路径");
			return false;
		}
		//拼接配分图片路径
		StringBuffer pathSB = new StringBuffer(backupPath).append(File.separator).append(userId).append(File.separator);
		if(StringUtils.isNotBlank(shopNo)){
			pathSB.append(shopNo).append(File.separator);
		}
		pathSB.append(code).append(File.separator);
		if(StringUtils.isNotBlank(zoom)){
			pathSB.append(zoom).append(File.separator);
		}
		File dir = new File(pathSB.toString());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		pathSB.append(fileName).append(".").append(fileType);
		_infoLog.info("备份图片路径:"+pathSB.toString());
		ByteArrayInputStream input = null;
		try {
			input = new ByteArrayInputStream(img);
			BufferedImage image = ImageIO.read(input);
			ImageIO.write(image, fileType, new File(pathSB.toString()));
		} catch (Exception e) {
			throw new FileException("备份图片发生异常",e);
		} finally {
			if(null != input){
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取备份图片 二进制
	 * @Title: getBuckUpImage
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param userId
	 * @param shopNo
	 * @param code
	 * @param fileName
	 * @param fileType
	 * @param zoom
	 * @throws FileException
	 * @return String
	 * @author phb
	 * @date 2015年3月4日 上午11:18:31
	 */
	public static String getBuckUpImage(Long userId,String shopNo,String code,String fileName,String fileType,String zoom)throws FileException{
		//获取备份图片路径
		String backupPath = PropertiesUtil.getContexrtParam("image.backup.url");
		if(StringUtils.isBlank(backupPath)){
			_errorLog.error("未配置允许上传的文件格式");
			return null;
		}
		//拼接配分图片路径
		StringBuffer pathSB = new StringBuffer(backupPath).append(File.separator).append(userId).append(File.separator);
		if(StringUtils.isNotBlank(shopNo)){
			pathSB.append(shopNo).append(File.separator);
		}
		pathSB.append(code).append(File.separator);
		if(StringUtils.isNotBlank(zoom)){
			pathSB.append(zoom).append(File.separator);
		}
		pathSB.append(fileName).append(".").append(fileType);
		try {
			File file = new File(pathSB.toString());
			if(!file.exists()){
				return null;
			}
			return pathSB.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 将上传图片保存到临时文件夹下 返回当前图片的宽高
	 * 
	 * @Title: upload
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param img
	 * @param fileName
	 * @return
	 * @throws FileException
	 * @date 2015年1月6日 下午2:14:28
	 * @author phb
	 */
	public static String upload(byte[] img)
			throws FileException {
		Map<String, Object> map = new HashMap<String, Object>();
		ByteArrayInputStream typeInput = null;

		BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;
		try {
			//验证文件类型
			typeInput = new ByteArrayInputStream(img);
			FileType fileType = FileTypeJudge.getType(typeInput);
			if( !_checkFileType(fileType.name()) ){
				_infoLog.info(new StringBuffer("上传的文件类型不被允许：").append(fileType.name()).toString());
				map.put("flag", "FAILED");
				map.put("info", "上传的文件类型不被允许");
				return JSONObject.fromObject(map).toString();
			}
			_infoLog.info("上传的文件类型验证通过："+fileType.name());
			
			//限制文件大小
			String maxSize = PropertiesUtil.getContexrtParam("image.maxsize");
			if( StringUtils.isBlank( maxSize ) ){
				_errorLog.error("未配置允许上传的文件大小");
				throw new FileException("未配置允许上传的文件大小");
			}
			if( Long.valueOf(maxSize).longValue() < img.length ){
				String info = new StringBuffer("上传的文件超出最大限制").append(Long.valueOf(maxSize)*1.0/(1024*1024)).append("M").toString();
				_infoLog.info(info);
				map.put("flag", "FAILED");
				map.put("info", info);
				return JSONObject.fromObject(map).toString();
			}
			_infoLog.info("上传的文件大小验证通过："+fileType.name());
			
//			input = new ByteArrayInputStream(img);
//			BufferedImage image = ImageIO.read(input);
			String imgName = ImageUtils.randomImageName(fileType.name());
			StringBuffer sb = new StringBuffer(getPhysicalPath(TEMP_PATH))
					.append(File.separator).append(SDF.format(new Date()));
			File dir = new File(sb.toString());
			if (!dir.exists()) {
				dir.mkdirs();
			}
			sb.append(File.separator).append(imgName);
//			ImageIO.write(image, fileType.name(), new File(sb.toString()));
			
            file = new File(sb.toString());  
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);
            bos.write(img);
			
			StringBuffer path = new StringBuffer(TEMP_PATH_SHOW).append("/").append(SDF.format(new Date())).append("/").append(imgName);
			map.put("flag", "SUCCESS");
			map.put("path", path.toString());
			map.put("type", fileType.name());
			map.put("source", new StringBuffer(SHOW_LOCAL_IMAGE_PATH).append(path.toString()).toString());
			return JSONObject.fromObject(map).toString();
		} catch (Exception e) {
			_errorLog.error("上传图片失败"+e);
			throw new FileException(e);
		} finally {
			try {
				if ( null != bos ) {
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if ( null != fos ) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if ( null != typeInput ) {
					typeInput.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 根据切图参数，生成切图
	 * 
	 * @Title: cut
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param cutDto
	 * @date 2015年1月6日 下午2:13:57
	 * @author phb
	 */
	public static String cut(CutDto cutDto) throws FileException{
		try {
			// 源图片路径
			String subPath = getPhysicalPath(cutDto.getImagePath());
			// 文件名
			String imgName = subPath.substring(subPath
					.lastIndexOf(File.separator) + 1);
			// 目标图片路径
			StringBuffer tagPath = new StringBuffer(getPhysicalPath(CUT_PATH))
					.append(File.separator).append(SDF.format(new Date()));
			File tagDir = new File(tagPath.toString());
			if (!tagDir.exists()) {
				tagDir.mkdirs();
			}
			tagPath.append(File.separator).append(imgName);
			// 坐标
			int x = ((int) Math
					.round(Double.parseDouble(cutDto.getSelectorX())) - (int) Math
					.round(Double.parseDouble(cutDto.getImageX())));
			int y = ((int) Math
					.round(Double.parseDouble(cutDto.getSelectorY())) - (int) Math
					.round(Double.parseDouble(cutDto.getImageY())));
			
			File subFile = new File(subPath);
			File tagFile = new File(tagPath.toString());
			FileOutputStream out = new FileOutputStream(tagFile);
			ImageIcon ii = new ImageIcon(subFile.getCanonicalPath());
			Image image = ii.getImage();
			//缩放图片
			BufferedImage tagImage = imageZoomOut(image, cutDto.getImageW(), cutDto.getImageH(), true);
			
			int rotateNum_i = Integer.parseInt(cutDto.getImageRotate()); // 旋转参数
			// 旋转图片
			if (rotateNum_i != 0) {
				tagImage = ImageProcess.rotateImage(tagImage, rotateNum_i);// 接收旋转后图片
			}
			
			// 选择框的宽高
			int s_w = Integer.parseInt(cutDto.getSelectorW());
			int s_h = Integer.parseInt(cutDto.getSelectorH());
			// 剪切图片
			tagImage = ImageProcess.cut(tagImage, x, y, s_w, s_h);
			
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(tagImage);
			param.setQuality(1f, true);
			encoder.setJPEGEncodeParam(param);
			encoder.encode(tagImage);
			
			StringBuffer path = new StringBuffer(CUT_PATH_SHOW).append("/").append(SDF.format(new Date())).append("/").append(imgName);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("flag", "SUCCESS");
			map.put("path", path.toString());
			map.put("source", "/showLocalImg.do?path="+path.toString());
			return JSONObject.fromObject(map).toString();
		} catch (Exception e) {
			_errorLog.error("剪切图片失败"+e);
			e.printStackTrace();
			throw new FileException(e);
		}
	}

	public static String zoomImg(String path, String width_height) {
		try {
			String[] wh = width_height.split("_");
			if (wh.length != 2) {
				throw new FileException("缩放尺寸参数异常");
			}
			String imgName = path
					.substring(path.lastIndexOf(File.separator) + 1);
			File file = new File(path);
			StringBuffer sb = new StringBuffer(getPhysicalPath(ZOOM_PATH))
					.append(File.separator).append(SDF.format(new Date())).append(File.separator).append(width_height);
			File dir = new File(sb.toString());
			if (!dir.exists()) {
				dir.mkdirs();
			}
			sb.append(File.separator).append(imgName);
			File zoomFile = new File(sb.toString());
			FileOutputStream out = new FileOutputStream(zoomFile);
			ImageIcon ii = new ImageIcon(file.getCanonicalPath());
			Image image = ii.getImage();
			// 按比例输出
			BufferedImage image2 = imageZoomOut(image, wh[0], wh[1], true);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image2);
			param.setQuality(1f, true);
			encoder.setJPEGEncodeParam(param);
			encoder.encode(image2);
			return sb.toString();
		} catch (Exception e) {
			throw new FileException("缩放图片发生异常");
		}
	}
	
	public static String zoomImg(byte[] fileByte,String suffix, String width_height) {
		try {
			String[] wh = width_height.split("_");
			if (wh.length != 2) {
				throw new FileException("缩放尺寸参数异常");
			}
			
			String tempImgName = ImageUtils.randomImageName(suffix);
			StringBuffer tempSb = new StringBuffer(getPhysicalPath(TEMP_PATH))
					.append(File.separator).append(SDF.format(new Date()));
			File tempDir = new File(tempSb.toString());
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			tempSb.append(File.separator).append(tempImgName);
			String path = tempSb.toString();
			byte2File(fileByte,path);
			
			String imgName = path
					.substring(path.lastIndexOf(File.separator) + 1);
			File file = new File(path);
			StringBuffer sb = new StringBuffer(getPhysicalPath(ZOOM_PATH))
					.append(File.separator).append(SDF.format(new Date())).append(File.separator).append(width_height);
			File dir = new File(sb.toString());
			if (!dir.exists()) {
				dir.mkdirs();
			}
			sb.append(File.separator).append(imgName);
			File zoomFile = new File(sb.toString());
			FileOutputStream out = new FileOutputStream(zoomFile);
			ImageIcon ii = new ImageIcon(file.getCanonicalPath());
			Image image = ii.getImage();
			// 按比例输出
			BufferedImage image2 = imageZoomOut(image, wh[0], wh[1], true);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image2);
			param.setQuality(1f, true);
			encoder.setJPEGEncodeParam(param);
			encoder.encode(image2);
			return sb.toString();
		} catch (Exception e) {
			throw new FileException("缩放图片发生异常");
		}
	}
	
	/**
	 * 添加水印文字
	 * @Title: pressText 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param file
	 * @param pressText
	 * @param fontName
	 * @param fontSize
	 * @param color
	 * @param x
	 * @param y
	 * @param alpha
	 * @date 2015年1月14日 下午4:20:27  
	 * @author phb
	 */
	public static String pressText(byte[] file,String suffix, String pressText, String fontName, int fontSize, String htmlcolor, int x, int y, float alpha){
		//TODO
		try {
			// 目标图片路径
			StringBuffer tagPath = new StringBuffer(getPhysicalPath(PRESS_PATH))
					.append(File.separator).append(SDF.format(new Date()));
			String imgName = ImageUtils.randomImageName(suffix);
			File tagDir = new File(tagPath.toString());
			if (!tagDir.exists()) {
				tagDir.mkdirs();
			}
			tagPath.append(File.separator).append(imgName);
			Color color = _parseToColor(htmlcolor);
			_pressText(file,tagPath.toString(),pressText,fontName,Font.PLAIN,fontSize,color,x,y,alpha);
			return new StringBuffer(PRESS_PATH).append(File.separator).append(SDF.format(new Date())).append(File.separator).append(imgName).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FileException("添加水印文字发生异常",e);
		}
	}
	
	/**
     * 添加文字水印
     * @param targetImg 目标图片路径，如：C://myPictrue//1.jpg
     * @param pressText 水印文字， 如：中国证券网
     * @param fontName 字体名称，    如：宋体
     * @param fontStyle 字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC)
     * @param fontSize 字体大小，单位为像素
     * @param color 字体颜色
     * @param x 水印文字距离目标图片左侧的偏移量，如果x<0, 则在正中间
     * @param y 水印文字距离目标图片上侧的偏移量，如果y<0, 则在正中间
     * @param alpha 透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
     */
    private static void _pressText(byte[] file,String tagImg, String pressText, String fontName, int fontStyle, int fontSize, Color color, int x, int y, float alpha) {
        try {
            File tagfile = new File(tagImg);
            Image image = ImageIO.read(new ByteArrayInputStream(file));
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bufferedImage.createGraphics();
            /* -------------处理透明背景变黑问题---------------*/
            bufferedImage = g.getDeviceConfiguration().createCompatibleImage(width,height,  
                    Transparency.TRANSLUCENT);
            g.dispose();
            g = bufferedImage.createGraphics();
            /* -------------处理透明背景变黑问题end---------------*/
            g.drawImage(image, 0, 0, width, height, null);
            g.setColor(color);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            
            /* --------对要显示的文字进行处理-------------- */
            AttributedString ats = new AttributedString(pressText);
            Font font = new Font(fontName, fontStyle, fontSize);
            g.setFont(font);
           /* 消除java.awt.Font字体的锯齿 */
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
            /* 消除java.awt.Font字体的锯齿 */
            // font = g.getFont().deriveFont(30.0f);
            ats.addAttribute(TextAttribute.FONT, font, 0, pressText.length());
            AttributedCharacterIterator iter = ats.getIterator();
            
            int width_1 = fontSize * getLength(pressText);
            int height_1 = fontSize;
            int widthDiff = width - width_1;
            int heightDiff = height - height_1;
            if(x < 0){
                x = widthDiff / 2;
            }else if(x > widthDiff){
                x = widthDiff;
            }
            if(y < 0){
                y = heightDiff / 2;
            }else if(y > heightDiff){
                y = heightDiff;
            }
            
            g.drawString(iter, x, y + height_1);
            g.dispose();
            ImageIO.write(bufferedImage, "PNG", tagfile);
        } catch (Exception e) {
        	_errorLog.error(new StringBuffer("ImageUtils._pressText添加水印文字异常").append(e.getMessage()).toString());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取字符长度，一个汉字作为 1 个字符, 一个英文字母作为 0.5 个字符
     * @param text
     * @return 字符长度，如：text="中国",返回 2；text="test",返回 2；text="中国ABC",返回 4.
     */
    private static int getLength(String text) {
        int textLength = text.length();
        int length = textLength;
        for (int i = 0; i < textLength; i++) {
            if (String.valueOf(text.charAt(i)).getBytes().length > 1) {
                length++;
            }
        }
        return (length % 2 == 0) ? length / 2 : length / 2 + 1;
    }
	
    /**
	 * HTML颜色转java.awt.Color
	 * @Title: parseToColor 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param htmlColor
	 * @return
	 * @date 2014年7月8日 下午6:33:28  
	 * @author Administrator
	 */
	public static Color _parseToColor( String htmlColor) {
	    Color convertedColor = Color.BLACK;
	    if (htmlColor.indexOf("#") != -1) {
	    	htmlColor= htmlColor.substring(1);
		}
	    convertedColor = new Color(Integer.parseInt(htmlColor, 16));
	    return convertedColor;
	}
	
	/**
	 * java.awt.Color转HTML颜色
	 * @Title: parseToHtml 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param color
	 * @return
	 * @date 2014年7月8日 下午6:34:00  
	 * @author Administrator
	 */
	public static String _parseToHtml(Color color) {
		  String R = Integer.toHexString(color.getRed());
		  R = R.length()<2?('0'+R):R;
		  String B = Integer.toHexString(color.getBlue());
		  B = B.length()<2?('0'+B):B;
		  String G = Integer.toHexString(color.getGreen());
		  G = G.length()<2?('0'+G):G;
		  return '#'+R+B+G;
	}
    
	/**
	 * 根据文件路径 获取文件二进制
	 * @Title: getPhysicalFile 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param path
	 * @return
	 * @date 2015年1月8日 下午3:46:58  
	 * @author phb
	 */
	public static byte[] getPhysicalFile(String path){
		String phyPath = getPhysicalPath(path);
		File file = new File(phyPath);
		if(!file.exists()){
			return null;
		}
		return ImageUtils.getBytes(file);
	}

	/**
	 * 随机图片名，用于存储图片在本地
	 * 
	 * @Title: randomImageName
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param suffix
	 * @return
	 * @date 2015年1月5日 下午7:52:48
	 * @author phb
	 */
	public static String randomImageName(String suffix) {
		long now = System.currentTimeMillis();// 时间戳
		Random random = new Random();
		return new StringBuffer(random.nextInt(10000)).append(now).append(".")
				.append(suffix).toString();// 时间戳命名
	}

	/**
	 * 获取当前工程下面某个相对路径的物理路径
	 * 
	 * @Title: getPhysicalPath
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param path
	 * @return
	 * @date 2015年1月6日 下午2:13:17
	 * @author phb
	 */
	public static String getPhysicalPath(String path) {
		String classPath = ImageUtils.class.getClassLoader().getResource("/")
				.getPath();
		String filePath = classPath.replace(CLASS_PATH, path);
		return replaceFile(filePath);
	}

	/**
	 * 过滤文件分隔符
	 * 
	 * @Title: replaceFile
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param path
	 * @return
	 * @date 2015年1月6日 下午2:13:13
	 * @author phb
	 */
	private static String replaceFile(String path) {
		return path.replace("/", File.separator).replace("\\", File.separator);
	}

	/**
	 * 文件转换为byte[]
	 * 
	 * @Title: getBytes
	 * @Description: (这里用一句话描述这个方法的作用)
	 * @param file
	 * @return
	 * @date 2015年1月6日 下午2:46:01
	 * @author phb
	 */
	public static byte[] getBytes(File file) {
		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(
					(int) file.length());
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
		return buffer;
	}
	
	/***
	 * 二进制转换为文件
	 * @Title: byte2File 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param buf
	 * @param filePath
	 * @param fileName
	 * @date 2015年1月15日 上午10:01:07  
	 * @author phb
	 */
	public static void byte2File(byte[] buf, String filePath)  
    {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try  
        {  
            File dir = new File(filePath);  
            if (!dir.exists() && dir.isDirectory())  
            {  
                dir.mkdirs();  
            }  
            file = new File(filePath);  
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(buf);  
        }  
        catch (Exception e)  
        {  
            e.printStackTrace();  
        }  
        finally  
        {  
            if (bos != null)  
            {  
                try  
                {  
                    bos.close();  
                }  
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }  
            }  
            if (fos != null)  
            {  
                try  
                {  
                    fos.close();  
                }  
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  

	/**
	 * @param BufferedImage
	 *            ：缩小的图片
	 * @param toWidth
	 *            ：缩小的宽度
	 * @param toHight
	 *            ：缩小的高度
	 * @param lockScale
	 *            :True:等比例缩放; flase:强制按输入值缩放
	 * @return BufferedImage：缩小后的图片
	 * @author liujia
	 * */
	public static BufferedImage imageZoomOut(Image srcBufferImage,
			String toWidth, String toHight, boolean isLockScale) {
		// 原图宽，高
		int width = srcBufferImage.getWidth(null);
		int height = srcBufferImage.getHeight(null);

		Image resizedImage = null;

		// 目标宽，高
//		int w = Integer.parseInt(toWidth);
//		int h = Integer.parseInt(toHight);
		int w = (int) Math.round(Double.parseDouble(toWidth));// 宽度==
		int h = (int) Math.round(Double.parseDouble(toHight));// 高度===

		int[] imgList = getWdithAndHeight(width, height, w, h);

		resizedImage = srcBufferImage.getScaledInstance(imgList[0], imgList[1],
				Image.SCALE_SMOOTH);

		// This code ensures that all the pixels in the image are loaded.
		Image temp = new ImageIcon(resizedImage).getImage();

		// Create the buffered image.
		BufferedImage bufferedImage = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);

		// Copy image to buffered image.
		Graphics g = bufferedImage.createGraphics();

		// Clear background and paint the image.
		g.setColor(Color.white);

		if (imgList[0] == w && imgList[1] == h) {
			g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
			g.drawImage(temp, 0, 0, null);
		} else if (imgList[1] == h) {
			int x = (w - imgList[0]) / 2;
			g.fillRect(0, 0, w, h);
			g.drawImage(temp, x, 0, null);

		} else if (imgList[0] == w) {
			int y = (h - imgList[1]) / 2;
			g.fillRect(0, 0, w, h);
			g.drawImage(temp, 0, y, null);
		}

		g.dispose();

		// Soften.
		float softenFactor = 0.05f;
		float[] softenArray = { 0, softenFactor, 0, softenFactor,
				1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0 };
		Kernel kernel = new Kernel(3, 3, softenArray);
		ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		bufferedImage = cOp.filter(bufferedImage, null);

		return bufferedImage;
	}

	/**
	 * @Title: getWdithAndHeight 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param yWidth原图尺寸
	 * @param yHeight原图尺寸
	 * @param mWidth目标图尺寸
	 * @param mHeight目标图尺寸
	 * @return
	 * @date 2015年1月8日 下午1:53:59  
	 * @author phb
	 */
	private static int[] getWdithAndHeight(int yWidth, int yHeight, int mWidth,
			int mHeight) {
		// 初始化缩略图尺寸
		int newWidth = 0;
		int newHeight = 0;

		// 原图宽大于高，优先计算高，宽固定mWidth
		if (yWidth > yHeight) {
			newWidth = mWidth;
			newHeight = (newWidth * yHeight) / yWidth;
			// 目标图尺寸大于缩略图尺寸
			if (mWidth >= newWidth && mHeight >= newHeight) {
				int[] imgList = { newWidth, newHeight };
				return imgList;
			} else {
				newHeight = mHeight;
				newWidth = (newHeight * yWidth) / yHeight;
				int[] imgList = { newWidth, newHeight };
				return imgList;
			}
		} else {
			// 高固定，宽计算
			// mHeight
			newHeight = mHeight;
			newWidth = (newHeight * yWidth) / yHeight;
			if (mWidth >= newWidth && mHeight >= newHeight) {
				int[] imgList = { newWidth, newHeight };
				return imgList;
			} else {
				newWidth = mWidth;
				newHeight = (newWidth * yHeight) / yWidth;
				int[] imgList = { newWidth, newHeight };
				return imgList;
			}
		}
	}
	
	/**
	 * 验证文件类型
	 * @Title: _checkFileType 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param suffix
	 * @return
	 * @date 2015年1月12日 下午2:57:16  
	 * @author phb
	 */
	private static boolean _checkFileType(String suffix){
		String fileType = PropertiesUtil.getContexrtParam("image.suffix");
		if(StringUtils.isBlank(fileType)){
			_errorLog.error("未配置允许上传的文件格式");
			return false;
		}
		String[] fileTypes = fileType.split(",");
		for (String string : fileTypes) {
			if(string.equals(suffix)){
				return true;
			}
		}
		return false;
	}
}
