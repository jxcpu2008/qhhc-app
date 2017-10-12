package com.hc9.web.main.common.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import com.hc9.commons.log.LOG;


/**
 * ajax响应视图
 * @author frank
 *
 */
@Component
public class AjaxResponseView implements View {

    /**
     * 常量
     */
    public static final String JSON = "json";
    
    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request,HttpServletResponse response) throws Exception {
        
        PrintWriter writer = null;
        
        try {
            writer = response.getWriter();
            writer.print(model.get(JSON));
        } catch (IOException e) {
            LOG.info("获取输出流失败！",e);
        }finally{
            writer.flush();
            writer.close();
        }
        
    }

}
