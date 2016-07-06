package com.zjlp.face.file.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ImageProcess {
	static BASE64Encoder encoder = new sun.misc.BASE64Encoder();
	static BASE64Decoder decoder = new sun.misc.BASE64Decoder();
    /**
     * 对图片进行缩放
     * 
     * @param srcImgFileName
     * @throws IOException
     */
    public static BufferedImage zoomImage(BufferedImage src,int w,int h) throws IOException {
    	
    	BufferedImage tag = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        // 绘制缩小后的图
        tag.getGraphics().drawImage(src, 0, 0, w, h, null);
        return tag;
    }

    /**
     * 
     * 对图片进行裁决
     * @param srcImageFile
     * @throws IOException
     */
    public static BufferedImage cut(BufferedImage src,int x,int y,int w,int h) throws IOException {
        Image img;
        ImageFilter cropFilter;
        // 四个参数分别为图像起点坐标和宽高
        cropFilter = new CropImageFilter(x, y, w, h);
        img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(src.getSource(), cropFilter));
        BufferedImage tag = new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(img, 0, 0, null); // 绘制小图
        g.dispose();
        return tag;
    }
    /** *//**
     * 旋转图片为指定角度
     * 
     * @param bufferedimage
     *            目标图像
     * @param degree
     *            旋转角度
     * @return
     */
    public static BufferedImage rotateImage(BufferedImage bufferedimage,int degree){
        int w= bufferedimage.getWidth();// 得到图片宽度。
        int h= bufferedimage.getHeight();// 得到图片高度。
        int type= bufferedimage.getColorModel().getTransparency();// 得到图片透明度。
        BufferedImage img;// 空的图片。
        Graphics2D graphics2d;// 空的画笔。
        (graphics2d= (img= new BufferedImage(w, h, type)).createGraphics()).setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);// 旋转，degree是整型，度数，比如垂直90度。
        graphics2d.drawImage(bufferedimage, 0, 0, null);// 从bufferedimagecopy图片至img，0,0是img的坐标。
        graphics2d.dispose();
        return img;// 返回复制好的图片，原图片依然没有变，没有旋转，下次还可以使用。
    }
    
    /*********
     * 确定旋转后的图象的高度和宽度 
     * @param image 图片
     * @param degree 角度
     * @param bgcolor 背景色
     * @return
     * @throws IOException
     */
    public static BufferedImage rotateImg(BufferedImage image, int degree, Color bgcolor) throws IOException {  
  	  
        int iw = image.getWidth();//原始图象的宽度   
        int ih = image.getHeight();//原始图象的高度  
        int w = 0;  
        int h = 0;  
        int x = 0;  
        int y = 0;  
        degree = degree % 360;  
        if (degree < 0)  
            degree = 360 + degree;//将角度转换到0-360度之间  
        double ang = Math.toRadians(degree);//将角度转为弧度  
  
        /** 
         *确定旋转后的图象的高度和宽度 
         */  
  
        if (degree == 180 || degree == 0 || degree == 360) {  
            w = iw;  
            h = ih;  
        } else if (degree == 90 || degree == 270) {  
            w = ih;  
            h = iw;  
        } else {  
            int d = iw + ih;  
            w = (int) (d * Math.abs(Math.cos(ang)));  
            h = (int) (d * Math.abs(Math.sin(ang)));  
        }  
  
        x = (w / 2) - (iw / 2);//确定原点坐标  
        y = (h / 2) - (ih / 2);  
        BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());  
        Graphics2D gs = (Graphics2D)rotatedImage.getGraphics();  
        if(bgcolor==null){  
            rotatedImage  = gs.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);  
        }else{  
            gs.setColor(bgcolor);  
            gs.fillRect(0, 0, w, h);//以给定颜色绘制旋转后图片的背景  
        }  
          
        AffineTransform at = new AffineTransform();  
        at.rotate(ang, w / 2, h / 2);//旋转图象  
        at.translate(x, y);  
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);  
        op.filter(image, rotatedImage);  
        image = rotatedImage;      
        return image;
    }  
      
    /*****
     * 图片转base64图片信息
     * @param f
     * @return
     */  
    public static String getImageBinary(File f ) {
		BufferedImage bi;
		try {
			bi = ImageIO.read(f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "jpg", baos);
			byte[] bytes = baos.toByteArray();
			System.out.println(bytes.length);
			return encoder.encodeBuffer(bytes).trim();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
    /*******
     * 转成图片信息
     * @param base64String
     * @param file d:/a.jpg 
     * @param imgType png,jpg
     */
	public static void base64StringToImage(String base64String,File file,String imgType) {
		try {
			byte[] bytes1 = decoder.decodeBuffer(base64String);
			System.out.println(bytes1.length);
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
			BufferedImage bi1 = ImageIO.read(bais);
			ImageIO.write(bi1, imgType, file);//不管输出什么格式图片，此处不需改动   
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

