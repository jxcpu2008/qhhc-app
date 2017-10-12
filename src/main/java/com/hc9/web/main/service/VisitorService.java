package com.hc9.web.main.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Banner;
import com.hc9.web.main.redis.sys.vo.BannerVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;

/** 前台首页service */
@Service
@SuppressWarnings(value = { "visitorservice" })
public class VisitorService {

	/** 注入HibernateSupport */
	@Resource
	HibernateSupport commondao;

	/**
	 * 验证用户的登陆名是否重复 
	 * @param userName 用户名
	 * @return boolean
	 */
	public boolean checkUserName(String userName) {
		String hql = "from Userbasicsinfo where userName = '" + userName + "'";
		int size = commondao.find(hql).size();
		if (size > 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 验证用户注册邮箱是否重复 
	 * @param email  邮箱
	 * @return boolean
	 */
	public boolean checkUserEmail(String email) {
		String hql = "from Userrelationinfo where email = '" + email + "'";
		int size = commondao.find(hql).size();
		if (size > 0) {
			return false;
		} else {
			return true;
		}
	}

	/** 服务协议 */
	public Object getAgreeMent() {
		String sql = "select pageHTML from deputysection where name='前海红筹网站服务协议'";
		Object obj = commondao.findObjectBySql(sql.toString());
		return obj;
	}

	public Object getPayProtocol() {
		String sql = "select pageHTML from deputysection where name='前海红筹网站支付协议'";
		Object obj = commondao.findObjectBySql(sql.toString());
		return obj;
	}

	public Object getContant() {
		String sql = "select pageHTML from deputysection where name='众持项目之借款及服务协议'";
		Object obj = commondao.findObjectBySql(sql.toString());
		return obj;
	}
	
	/** 图片信息 */
    public List<Banner> query() {
        String hql = "from Banner where type=1 order by number asc";
        List<Banner> banners = commondao.find(hql);
        if(banners != null && banners.size() > 0) {
        	List<BannerVo> bannerVoList = new ArrayList<BannerVo>();
        	for(Banner banner : banners) {
        		BannerVo vo = new BannerVo();
        		vo.setImgurl(banner.getImgurl());
        		vo.setUrl(banner.getUrl());
        		bannerVoList.add(vo);
        	}
        	WebCacheManagerUtil.setWebBannerListToRedis(bannerVoList);
        }
        return banners;
    }
}