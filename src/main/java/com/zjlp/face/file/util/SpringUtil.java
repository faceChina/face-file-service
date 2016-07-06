package com.zjlp.face.file.util;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
/**
 * @ClassName: com.jzwgj.util.SpringUtil
 * @Description: SpringUtil工具类,方便获取ApplicationContext对象
 * @date: 2014年7月30日 下午2:30:37
 * @author: zyl
 */
public class SpringUtil {
	
	/**
	 * @Description: 获取ApplicationContext对象
	 * @return ApplicationContext
	 */
	public static ApplicationContext getApplicationContext(){
		return ContextLoader.getCurrentWebApplicationContext();
	}
	
	/**
	 * @Description: 获取bean对象
	 * @param name
	 * @return Object
	 */
	public static Object getBean(String name){
		return ContextLoader.getCurrentWebApplicationContext().getBean(name);
	}
}
