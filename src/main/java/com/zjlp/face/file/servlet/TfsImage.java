package com.zjlp.face.file.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.zjlp.face.file.util.ImageUtils;
import com.zjlp.face.file.util.PropertiesUtil;

public class TfsImage extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7499367175929820709L;

//	private ImageLocalService imageLocalService;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
//		imageLocalService = (ImageLocalService) SpringUtil.getBean("imageLocalService");
//		OutputStream os = null;
//		try {
//			String url = req.getRequestURL().toString();
//			
//			String param = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
//			byte[] b = imageLocalService.read(param);
//			if(null != b){
//				os = resp.getOutputStream();
//				os.write(b);
//				os.flush();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if(null != os){
//				os.close();
//			}
//		}
		
		try {
			String url = req.getRequestURL().toString();
			if(StringUtils.isBlank(url)){
				return;
			}
			String[] param = url.split(ImageUtils.SHOW_IMAGE);
			if(param.length != 2){
				return;
			}
			String qiniuHost = PropertiesUtil.getContexrtParam("qiniu.host.url");
			if(StringUtils.isBlank(qiniuHost)){
				return;
			}
			StringBuffer sb = new StringBuffer(qiniuHost);
			sb.append(ImageUtils.SHOW_IMAGE).append(param[1]);
			System.out.println(sb.toString());
			resp.sendRedirect(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	
}
