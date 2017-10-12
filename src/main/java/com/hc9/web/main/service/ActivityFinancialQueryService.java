package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.ActivityFinancial;


@Service
public class ActivityFinancialQueryService {
	
	@Resource
	private HibernateSupport dao;
	
	public ActivityFinancial getActivityFinancial(String userId ){
		String sql="select * from activity_financial where userId=?";
		return dao.findObjectBySql(sql, ActivityFinancial.class, userId);
	}
    
	public void addActivityFinancial(ActivityFinancial activityFinancial){
		dao.saveOrUpdate(activityFinancial);
	}
	
}
