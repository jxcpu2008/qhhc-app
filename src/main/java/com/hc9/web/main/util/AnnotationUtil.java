package com.hc9.web.main.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.hc9.web.main.common.annotation.FieldConfig;

/**
 * 注解工具
 * 
 * @author frank
 * 
 */
@SuppressWarnings("all")
public class AnnotationUtil {

    /**
     * 得到对象的主键属性
     * 
     * @param obj
     *            指定对象
     * @param clazz
     *            注解类
     * @param name
     *            属性名称
     * @return 属性值
     * @throws NoSuchFieldException
     *             异常
     * @throws NoSuchMethodException
     *             异常
     * @throws InvocationTargetException
     *             异常
     * @throws IllegalAccessException
     *             异常
     */
    public static Object getAttribute(Object obj, Class clazz, String name) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Annotation entity = null;

        if (obj.getClass().isEnum()) {// 该对象为枚举

            entity = obj.getClass().getField(obj.toString())
                    .getAnnotation(clazz);

        } else {
            entity = obj.getClass().getAnnotation(clazz);
        }

        Method method = clazz.getMethod(name);// 在获取想要的方法

        Object result = method.invoke(entity);// 反射调用方法获取相关注解值

        return result;
    }

    /**
     * 获取FieldConfig注解的值
     * 
     * @param enumm 枚举
     * @return      中文
     */
    public static String getFieldConfigValue(Enum enumm) {
        
        try {
            return (String) getAttribute(enumm, FieldConfig.class, "value");
        } catch (NoSuchFieldException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e) {
            LOG.error("获取FieldConfig注解值失败！", e);
        }
        
        
        return null;
    }

}
