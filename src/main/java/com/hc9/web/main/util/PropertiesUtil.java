package com.hc9.web.main.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置文件
 * 
 * @author frank
 * 
 */
public class PropertiesUtil {

    /**
     * 读取
     * 
     * @param name
     *            name
     * @return Properties
     */
    public static Properties getReadAbleProperties(String name) {

        InputStream ins = PropertiesUtil.class.getResourceAsStream(name);

        // 生成properties对象
        Properties p = new Properties();

        try {
            p.load(ins);
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p;

    }

}
