package com.hc9.web.main.common.interceptor;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.annotation.SmsCheck;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.BlackIP;

public class SmsInterceptor implements MethodInterceptor {
	 @Resource
	 private HibernateSupport dao;
    /**
     * 构造方法
     */
    public SmsInterceptor() {
        LOG.info("--->启动恶意请求拦截器成功！");
    }
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
	    String sql="SELECT * FROM black_ip";
	    List<BlackIP> list=dao.findBySql(sql, BlackIP.class);
	    String ip=request.getRemoteAddr();
		boolean bool = invocation.getMethod().isAnnotationPresent(SmsCheck.class);
		if(bool){
		    for(BlackIP item:list){
		    	if(ip.equals(item.getIp()) && (item.getCount()>=2||item.getTimesDay()>=5)){
		    		return null;
		    	}
		    }
		}
		return invocation.proceed();
	}
}
