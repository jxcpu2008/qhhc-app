package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.HibernateSupportTemplate;
import com.hc9.web.main.entity.Generalize;
import com.hc9.web.main.entity.RegBonus;
/** 活动奖励service  */
@Service
public class BonusService {
	
    @Resource
    private HibernateSupportTemplate dao;
    
    /** 查询注册奖励表，返回记录 */
    public RegBonus queryRegBonus(Long id){
    	String sql="SELECT * from reg_bonus rb WHERE rb.user_id=?";
    	RegBonus bonus=dao.findObjectBySql(sql, RegBonus.class, id);
    	return bonus;
    }
    
    /** 查找推荐记录 */
    public Generalize queryGeneralize(Long id){
    	String sql="SELECT * from generalize g WHERE g.uid=?";
    	Generalize generalize=dao.findObjectBySql(sql, Generalize.class, id);
    	return generalize;
    }
}