package com.zjlp.face.file.dto;
/**
 * 水印文字业务参数类
 * @ClassName: PressParamDto 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author phb
 * @date 2015年1月16日 上午9:58:24
 */
public class PressParamDto extends FileBizParamDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4411945976303953344L;
	//原图片文件名
	private String fileNo;
	//水印文字
	private String fontText;
	//字体名称
	private String fontName;
	//字体大小
	private Integer fontSize;
	//字体颜色
	private String fontColor;
	//X轴
	private Integer fontX;
	//Y轴
	private Integer fontY;
	//透明度
	private Float alpha;
	
	public String getFileNo() {
		return fileNo;
	}
	public void setFileNo(String fileNo) {
		this.fileNo = fileNo;
	}
	public String getFontText() {
		return fontText;
	}
	public void setFontText(String fontText) {
		this.fontText = fontText;
	}
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public Integer getFontSize() {
		return fontSize;
	}
	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	public Integer getFontX() {
		return fontX;
	}
	public void setFontX(Integer fontX) {
		this.fontX = fontX;
	}
	public Integer getFontY() {
		return fontY;
	}
	public void setFontY(Integer fontY) {
		this.fontY = fontY;
	}
	public Float getAlpha() {
		return alpha;
	}
	public void setAlpha(Float alpha) {
		this.alpha = alpha;
	}
	
	
}
