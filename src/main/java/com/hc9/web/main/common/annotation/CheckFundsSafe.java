package com.hc9.web.main.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>对资金操作前的安全检查，</br>
 * 用户信息如邮箱未激活等都不能对资金操作</p>
 * <p>发布项目要先做融资人资格检查，没有申请或者申请了但没通过的不能发布项目</p>
 * @author frank
 * @version 2015-1-11
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckFundsSafe {
	
	/**基础检测</br>实名，手机，宝付注册检测*/
	public static final String BASIC_AUTH="Basic";
	
	/**众持融资人资格*/
	public static final String LOANEE="Loanee";
	
	/**店铺融资人资格*/
	public static final String SHOP="Shop";
    /**
     * 默认基础检测
     * 
     */
    String value() default BASIC_AUTH;
}
