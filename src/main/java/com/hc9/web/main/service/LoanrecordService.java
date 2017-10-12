package com.hc9.web.main.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.HibernateSupportTemplate;
import com.hc9.web.main.entity.Loanrecord;

/** 认购记录service */
@Service
public class LoanrecordService {
	/** dao */
	@Resource
	private HibernateSupportTemplate dao;

	public List<Loanrecord> findLoanRecordList(Long loanid) {
		StringBuffer sb = new StringBuffer(
				"SELECT * from loanrecord l where l.isSucceed=1 AND l.loanSign_id=")
				.append(loanid);
		return dao.findBySql(sb.toString(), Loanrecord.class);
	}

	/** 根据订单号查询购买记录信息 */
	public Loanrecord getLoanRecordOrderNum(String orderNum) {
		String sql = "select * from loanrecord where order_id=?";
		Loanrecord loanrecord = dao.findObjectBySql(sql, Loanrecord.class,
				orderNum);
		return loanrecord;
	}
}