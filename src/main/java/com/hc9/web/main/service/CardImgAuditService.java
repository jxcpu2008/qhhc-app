package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.CardImgAudit;

@Service
public class CardImgAuditService {
	
	@Resource
	private HibernateSupport dao;
	
    /** 身份证审核,保存 */
    public void save(CardImgAudit cardImgAudit) {
        dao.save(cardImgAudit);
    }

    /** 查询身份证审核记录 */
    public CardImgAudit getCardImgAudit(Long userId) {
    	String sql = "select * from cardimgaudit where userId=? limit 0,1";
		return dao.findObjectBySql(sql,CardImgAudit.class, userId);
    }
    
    /** 查询身份证审核记录 */
    public CardImgAudit getCardImgAuditByState(Long userId,String state) {
    	String sql = "select * from cardimgaudit where userId=? and cardImgState=? limit 0,1";
		return dao.findObjectBySql(sql,CardImgAudit.class, userId , state);
    }
    
    public void updateCardImgAudit(CardImgAudit card) {
    	dao.update(card);;
    }
}
