package com.zjlp.face.file.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zjlp.face.file.service.ImageLocalService;
import com.zjlp.face.file.util.SpringUtil;

/**
 * 显示截图的图片
 * @ClassName: CutImage 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author phb
 * @date 2015年1月8日 下午3:25:15
 */
public class LocalImage extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ImageLocalService imageLocalService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		imageLocalService = (ImageLocalService) SpringUtil.getBean("imageLocalService");
		OutputStream os = null;
		try {
			String path = req.getParameter("path");
			byte[] b = imageLocalService.showLocalImage(path);
			if(null == b){
				return;
			}
			os = resp.getOutputStream();
			os.write(b);
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null != os){
				os.close();
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	

}
