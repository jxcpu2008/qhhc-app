package com.hc9.web.main.common.aspect;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;

import com.hc9.web.main.common.exception.SimpleResponseException;

/** 日志记录 */
public class CheckLoginAspect {

    /** session的key */
    private String key;
    
    /** 类型 */
    private Class<?> cls;

    /** 异常 */
    private SimpleResponseException ex;

    /**
     * 构造函数
     * 
     * @param key session的key
     * @param cls 类型
     * @param ex 异常
     * @throws ClassNotFoundException 类不能找到
     */
    public CheckLoginAspect(String key, String cls, SimpleResponseException ex)
            throws ClassNotFoundException {
        super();
        this.key = key;
        this.cls = Class.forName(cls);
        this.ex = ex;
    }

    /**
     * 执行之前
     * @param jp 切入点
     * @throws SimpleResponseException  没有登录的异常
     */
    public void executeBefore(JoinPoint jp) throws SimpleResponseException {
        Object[] objs = jp.getArgs();
        for (Object obj : objs) {
            if (obj instanceof HttpServletRequest) {
                Object o = ((HttpServletRequest) obj).getSession()
                        .getAttribute(key);
                if (o == null || !o.getClass().equals(cls)) {
                    throw ex;
                } else {
                    break;
                }
            }
        }
    }
}
