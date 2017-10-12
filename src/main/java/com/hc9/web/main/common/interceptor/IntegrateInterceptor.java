package com.hc9.web.main.common.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.hc9.web.main.common.annotation.AddPoints;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.IntegralSevice;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.util.Constant;

/**
 * 积分AOP
 * 有需要积分的操作上加入注解@AddPoints
 * @author frank
 *
 */
public class IntegrateInterceptor implements MethodInterceptor {
	@Resource
	private MemberCenterService memberCenterService;
	@Resource
	private IntegralSevice integralSevice;
	private String val=null;
	
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
		boolean bool = invocation.getMethod().isAnnotationPresent(AddPoints.class);
		if(bool){
			
				AddPoints addPoints=invocation.getMethod().getAnnotation(AddPoints.class);
				val=addPoints.value();
				try{
					return invocation.proceed();
				}finally{
					boolean isLogin=memberCenterService.webLogin(request, response);
					if(isLogin){
						//TODO 加分操作
						Userbasicsinfo user=(Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
						if(user!=null){
							integralSevice.AddIntegralForUser(user, Integer.parseInt(val));
						}
					}else{
						return "redirect:/visitor/to-login";
					}
				}
		}
		return invocation.proceed();
	}



}
