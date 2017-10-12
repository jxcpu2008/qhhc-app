package com.hc9.web.main.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.util.LocalUtil;

/** 获得本地服务 */
public class LocalService {

    /** 百度key */
    private String key;

    public LocalService() {
    }

    /** 构造函数 */
    public LocalService(String key) {
        this.key = key;
        LOG.info("--->初始化百度KEY成功！");
    }

    /** 得到请求者的IP */
    public String getRequesterIP(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null) {
            String[] ip1 = ip.split(",");
            if (ip1.length > 1) {
                return ip1[0];
            } else {
                return ip;
            }
        } else {
            return ip;
        }

    }

    /** 得到请求者的地址 */
    public String getRequesterAddressByIP(String ip) {
        return LocalUtil.getAddress(ip, key);
    }

    /** 得到请求者的地址 */
    public String getRequesterAddressByRequeste(HttpServletRequest request) {
        return LocalUtil.getAddress(getRequesterIP(request), key);
    }
    /**
     * 获取地址的经纬度
     * @param address 比如：广东省深圳市福田区深南大道4001号或者广东省深圳市福田区国际科技大厦
     * @return Map<String,String> key:longitude经度，key:latitude纬度
     */
    public Map<String,String> getAddressCoordinate(String address){
    	return LocalUtil.getGeocoderLatitude(address, key);
    }
}