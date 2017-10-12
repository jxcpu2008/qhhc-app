package com.hc9.web.main.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 增加积分切入
 * 在需要操作的方法上@AddPoints
 * @author frank
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddPoints {
	
	/**登录 */
	public static final String LOGIN="1";
	
	/**评论*/
	public static final String COMMENT="2";
	
	/**约谈*/
	public static final String INTERVIEW="3";
	
	/**投资*/
	public static final String INVEST="4";
	
	/**推荐*/
	public static final String RECOMMEND="5";
	
	
	/**实名认证*/
	public static final String NAME_CERTIFICATE="6";
	
	/**电话认证*/
	public static final String PHONE_CERTIFICATE="7";
	
	/**邮箱认证*/
	public static final String MAIL_CERTIFICATE="8";
	
	/**图片上传*/
	public static final String UPLOAD_IMAGE="9";
	
	/**其他认证*/
	public static final String OTHER_CERTIFICATE="10";
	
	
	/**默认登录*/
	String value() default LOGIN;
}
