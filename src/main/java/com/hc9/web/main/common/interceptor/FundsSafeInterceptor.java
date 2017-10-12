package com.hc9.web.main.common.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.hc9.web.main.common.annotation.CheckFundsSafe;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.util.Constant;

/**
 * 增加资金安全拦截器，应用：在点击左侧菜单进入模块前判断用户身份证、手机、邮箱、安全问题是否都已填写，</br>
 * 如果某一项未填写，系统将跳转到安全中心的相应TAB页中</br>
 * 需要在进入模块的方法上加入@CheckFundsSafe注解，同时被拦截的方法需要request,response两个对象参数
 * 
 * @author frank
 * @version 2015-1-11
 */
public class FundsSafeInterceptor implements MethodInterceptor {
	


	/**
	 * 会员基本信息接口
	 * 
	 */
	@Resource
	private MemberCenterService memberCenterService;

	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		HttpServletRequest request=null;
		HttpServletResponse response=null;
		Object[] ars = mi.getArguments();
		for (Object o : ars) {
			if (o instanceof HttpServletRequest) {
				request = (HttpServletRequest) o;
			}
			if (o instanceof HttpServletResponse) {
				response = (HttpServletResponse) o;
			}
		}
		// 判断该方法是否加了@CheckFundsSafe注解
		boolean bool = mi.getMethod().isAnnotationPresent(CheckFundsSafe.class);
		if (bool) {
			
			Userbasicsinfo user = (Userbasicsinfo) request.getSession()
					.getAttribute(Constant.SESSION_USER);
			//获取注解内容
			CheckFundsSafe cks=mi.getMethod().getAnnotation(CheckFundsSafe.class);
			
			//注入类型
			String type=cks.value();
			int msg=100;
			// 如果未登录跳转登录页面
			if (user == null) {
				request.setAttribute(Constant.SECURITY_VERIFIY,"您还没有登录，请先登录！"); 
				return "redirect:/visitor/to-login";
			}
		}
		return mi.proceed();
	}


}
