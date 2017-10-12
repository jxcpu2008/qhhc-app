package com.hc9.web.main.service;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import com.hc9.web.main.constant.HostAddress;

/**
 * 初始化service
 * 
 * @author frank
 * 
 */
@Service
public class InitService implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
//		ResourceBundle bundle = ResourceBundle.getBundle("/config/user/host");
		ServletContext application = ((WebApplicationContext) applicationContext)
				.getServletContext();

		application.setAttribute(
				WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				applicationContext);
		application.setAttribute("oss", HostAddress.getResourceAddress());
		application.setAttribute("https", HostAddress.getHostAddress());
	}


}
