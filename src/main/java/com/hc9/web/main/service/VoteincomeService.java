package com.hc9.web.main.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.VoteIncome;

@Service
public class VoteincomeService {
    /** 注入数据库底层操作层*/
    @Resource
    private HibernateSupport dao;
    
	/** 获取加息券列表 */
	public List<VoteIncome> getVoteIncomeList(String loanId){
		String sql="select * from voteIncome where status in(0,-1)  and   loanId=?";
		List<VoteIncome> voteIncomeList=dao.findBySql(sql, VoteIncome.class, loanId);
		return voteIncomeList;
	}
	
	public void updateVoteIncome(VoteIncome voteIncome){
		dao.update(voteIncome);
	}
}
