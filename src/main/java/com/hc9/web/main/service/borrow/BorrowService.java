package com.hc9.web.main.service.borrow;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Borrowersbase;
import com.hc9.web.main.entity.Userbasicsinfo;

@Service
public class BorrowService {
	@Resource
	private HibernateSupport dao;

	public Borrowersbase getBorrowService(Userbasicsinfo user) {
		return (Borrowersbase) dao.findObject(
				"FROM Borrowersbase b WHERE b.userbasicsinfo.id=?",
				user.getId());
	}

	public Double getBorrowCredit(Long uid) {
		String sql = "SELECT credit from borrowersbase WHERE userbasicinfo_id=?";
		Object credit = dao.findObjectBySql(sql, uid);
		return credit != null ? Double.valueOf(credit.toString()) : 0D;
	}
}
