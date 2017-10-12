package com.hc9.web.main.common.interceptor;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.common.annotation.AddPoints;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.util.Constant;

/**
 * 用户资源请求拦截器(测试)
 * 
 * @author frank
 * 
 */
public class UserResourcesRequestInterceptor extends HandlerInterceptorAdapter {

    /**
     * 构造方法
     */
    public UserResourcesRequestInterceptor() {
        LOG.info("--->用户资源请求拦截器已启动!");
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        Userbasicsinfo user = (Userbasicsinfo)request.getSession().getAttribute(Constant.SESSION_USER);
    	HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        AddPoints add=method.getAnnotation(AddPoints.class);
        LOG.info(add.value());
        //用户未登录
        if(user==null){
            request.setAttribute("user_error","请先登录后执行操作！");
            request.getRequestDispatcher(Constant.URL_LOGIN).forward(request, response);
            return false;
        }
        
        return request.getRequestURI().indexOf("/user/"+user.getId()+"/")!=-1;
        
    }

}
