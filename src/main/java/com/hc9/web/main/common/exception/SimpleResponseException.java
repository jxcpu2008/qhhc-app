package com.hc9.web.main.common.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.hc9.commons.normal.Validate;
import com.hc9.web.main.common.view.AjaxResponseView;
import com.hc9.web.main.common.view.UrlResponseView;

/**
 * 简单响应式异常
 * 
 * @author frank
 * 
 */
public class SimpleResponseException extends ResponseException {

    /**
     * 标示
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 地址
     */
    private String url;
    /**
     * json值
     */
    private String json;
    /**
     * 键
     */
    private String attr;
    /**
     * 值
     */
    private Object val;
    /**
     * ajax视图
     */
    private ModelAndView ajaxView;
    /**
     * url视图
     */
    private ModelAndView urlView;

    

    /**
     * 构造函数
     * 
     * @param url
     *            跳转
     * @param message
     *            消息
     */
    public SimpleResponseException(String url, String message) {
        super(message);
        this.url = url;
    }

    /**
     * json
     * @param json  json
     */
    public void setJson(String json) {
        if (Validate.emptyStringValidate(json)) {
            this.json = json;
        } else {
            this.json = JSONArray
                    .toJSONString(new Object[] { -1, getMessage() });
        }
    }

    /**
     * attr
     * @param attr  attr
     */
    public void setAttr(String attr) {
        this.attr = attr;
    }

    /**
     * val
     * @param val   val
     */
    public void setVal(Object val) {
        if (val != null) {
            this.val = val;
        } else {
            this.val = getMessage();
        }
    }

    /**
     * view
     * @param view  view
     */
    public void setAjaxView(AjaxResponseView view) {
        this.ajaxView = new ModelAndView(view).addObject(AjaxResponseView.JSON,
                json);
    }

    /**
     * view
     * @param view  view
     */
    public void setUrlView(UrlResponseView view) {
        this.urlView = new ModelAndView(view)
                .addObject(UrlResponseView.URL, this.url)
                .addObject(UrlResponseView.ATTR, this.attr)
                .addObject(UrlResponseView.VAL, this.val);
    }

    @Override
    protected ModelAndView executeAjax(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception) {
        return ajaxView;
    }

    @Override
    protected ModelAndView executeUrl(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception) {
        return urlView;
    }

}
