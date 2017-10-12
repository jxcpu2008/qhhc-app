package com.hc9.web.main.service.article;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Helper;

@Service
public class HelperService {
	@Resource
	private HibernateSupport dao;

	public List<Helper> queryHelp(Integer id) {
		String hql = " from Helper where level_Id=" + id;
		List<Helper> list = dao.find(hql.toString());
		return list;
	}
}
