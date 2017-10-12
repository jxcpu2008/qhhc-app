package com.hc9.web.main.common.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

/**
 * url响应异常
 * @author frank
 *
 */
@Component
public class UrlResponseView implements View {

    /**
     * 常量
     */
    public static final String ATTR = "attr";
    /**
     * 常量
     */
    public static final String VAL = "val";
    /**
     * 常量
     */
    public static final String URL = "url";
    
    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request,HttpServletResponse response) throws Exception {
        
        request.getSession().setAttribute((String)model.get(ATTR),model.get(VAL));
        
        response.sendRedirect((String)model.get(URL));
        
    }

}
