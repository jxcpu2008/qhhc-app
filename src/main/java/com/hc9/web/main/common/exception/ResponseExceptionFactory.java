package com.hc9.web.main.common.exception;

import java.util.Dictionary;
import java.util.Hashtable;

import com.alibaba.fastjson.JSONArray;
import com.hc9.web.main.common.view.AjaxResponseView;
import com.hc9.web.main.common.view.UrlResponseView;
import com.hc9.web.main.util.AnnotationUtil;

/**
 * 响应式异常工厂
 * 
 * @author frank
 * 
 */
public final class ResponseExceptionFactory {

    /**
     * 返回地址
     */
    private String url;
    /**
     * 参数key
     */
    private String attr;
    /**
     * 参数值
     */
    private Object val;
    /**
     * ajax视图
     */
    private AjaxResponseView ajaxView;
    /**
     * 链接视图
     */
    private UrlResponseView urlView;

    /**
     * 异常池
     */
    private final Dictionary<String, SimpleResponseException> responseExceptions = new Hashtable<>();

    /**
     * 产生
     * @param key   key
     * @param url   返回地址
     * @param msg   消息
     * @param attr  参数
     * @param val   值
     * @param json  值
     * @return      异常
     */
    public ResponseException born(String key, String url, String msg,
            String attr, Object val, String json) {

        SimpleResponseException ex = responseExceptions.get(key);

        // 该异常不存在
        if (ex == null) {

            if (url == null) {
                url = this.url;
            }
            if (attr == null) {
                attr = this.attr;
            }
            if (val == null && msg == null) {
                val = this.val;
            }

            ex = new SimpleResponseException(url, msg);

            ex.setAttr(attr);
            ex.setVal(val);
            ex.setJson(json);
            ex.setAjaxView(ajaxView);
            ex.setUrlView(urlView);
            responseExceptions.put(key, ex);
        }

        return ex;
    }

    /**
     * 产生ajax
     * @param key   key
     * @param json  json
     * @return  异常
     */
    public ResponseException bornAjax(String key, String json) {
        return born(key, null, null, null, null, json);
    }

    /**
     * 产生ajax
     * @param key   异常枚举
     * @return  异常
     */
    public ResponseException bornAjax(Enum<?> key) {
        return born(
                key.toString(),
                null,
                null,
                null,
                null,
                JSONArray.toJSONString(new Object[] { -key.ordinal(),
                        AnnotationUtil.getFieldConfigValue(key) }));
    }

    /**
     * 产生链接
     * @param key   key
     * @param url   地址
     * @param attr  键
     * @param val   值
     * @return  异常
     */
    public ResponseException bornUrl(String key, String url, String attr,
            Object val) {
        return born(key, url, null, attr, val, null);
    }

    /**
     * 产生链接
     * @param key   异常枚举
     * @param url   地址
     * @param attr  键
     * @return  异常
     */
    public ResponseException bornUrl(Enum<?> key, String url, String attr) {
        return born(key.toString(), url, null, attr,
                AnnotationUtil.getFieldConfigValue(key), null);
    }

    /**
     * url
     * @param url   url
     */
    public void setUrl(String url) {
        this.url = url;
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
        this.val = val;
    }

    /**
     * ajaxView
     * @param ajaxView  ajaxView
     */
    public void setAjaxView(AjaxResponseView ajaxView) {
        this.ajaxView = ajaxView;
    }

    /**
     * urlView
     * @param urlView   urlView
     */
    public void setUrlView(UrlResponseView urlView) {
        this.urlView = urlView;
    }

}
