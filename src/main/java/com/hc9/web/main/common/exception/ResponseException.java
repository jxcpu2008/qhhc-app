package com.hc9.web.main.common.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * 响应式异常
 * @author frank
 *
 */
public abstract class ResponseException extends Throwable {

    /**
     * 标识
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造函数
     * @param message   消息
     */
    public ResponseException(String message) {
        super(message);
    }

    

    /**
     * 执行
     * @param request   请求
     * @param response  响应
     * @param handler   参数
     * @param exception 异常
     * @return          视图
     */
    public ModelAndView execute(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception) {

        // 判断是否为ajax请求
        if ("XMLHttpRequest".equalsIgnoreCase(request
                .getHeader("X-Requested-With"))) {

            return executeAjax(request, response, handler, exception);

        } else {

            return executeUrl(request, response, handler, exception);

        }

    }

    /**
     * 响应ajax请求
     * @param request   请求
     * @param response  响应
     * @param handler   参数
     * @param exception 异常
     * @return          视图
     */
    protected abstract ModelAndView executeAjax(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception);

    /**
     * 响应链接请求
     * @param request   请求
     * @param response  响应
     * @param handler   参数
     * @param exception 异常
     * @return          视图
     */
    protected abstract ModelAndView executeUrl(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception);

}
