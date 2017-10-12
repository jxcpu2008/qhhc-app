package com.hc9.web.main.common.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.constant.HostAddress;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.util.Constant;


/**
 * 针对方法的登录检测AOP
 * 当在某些类里有针对某些方法的切面操作，会和类的的拦截器CheckLogin冲突
 * 给需要检测的方法用上这个工具。
 * 有需要积分的操作上加入注解@AddPoints
 * @author frank
 *
 */
public class CheckLoginOnMethodInterceptor implements MethodInterceptor {
	@Resource
	private MemberCenterService memberCenterService;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		HttpServletRequest request=null;
		HttpServletResponse response=null;
		Object[] objs=invocation.getArguments();
		for(Object obj:objs){
			
			if (obj instanceof HttpServletRequest) {
				request = (HttpServletRequest) obj;
				
			}
			if (obj instanceof HttpServletResponse) {
				response = (HttpServletResponse) obj;
				
			}
		}
		boolean bool = invocation.getMethod().isAnnotationPresent(CheckLoginOnMethod.class);
		if(bool){
			boolean isLogin=memberCenterService.webLogin(request, response);
			if(isLogin){
				return invocation.proceed();
			}else{
				request.setAttribute(Constant.SECURITY_VERIFIY,"您还没有登录，请先登录！"); 

				return "redirect:"+HostAddress.getHostAddress()	+ "/visitor/to-login";
			}
		}
		return invocation.proceed();
	}



}
