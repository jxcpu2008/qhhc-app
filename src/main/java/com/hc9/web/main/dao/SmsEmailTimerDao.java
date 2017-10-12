package com.hc9.web.main.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.service.LoanManageService;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.StatisticsUtil;
import com.hc9.web.main.vo.EmaiLoansignInfo;

/** 短信邮件定时任务数据访问层 */

@Service
public class SmsEmailTimerDao {

	/** 注入数据库操作层 */
	@Resource
	private HibernateSupport dao;
	
	@Resource
	private LoanManageService loanManageService;
	
	/** 查询最新的三个新标 */
	public List<EmaiLoansignInfo> queryNewestThreeLoansign() {
		List<EmaiLoansignInfo> resultList = new ArrayList<EmaiLoansignInfo>();
		String sql = "select id,name,prio_rate,prio_aword_rate from " + 
				" (select * from loansign where status=1 and rest_money>0 order by id desc) t limit 0,3";
		List list = dao.findBySql(sql);
		if(list != null && list.size() > 0) {
			for(Object obj : list) {
				Object[] arr = (Object[])obj;
				EmaiLoansignInfo loan = new EmaiLoansignInfo();
				loan.setLoansignId(StatisticsUtil.getLongFromBigInteger(arr[0]));
				loan.setLoansignName(StatisticsUtil.getStringFromObject(arr[1]));
				loan.setPrioRate(StatisticsUtil.getDoubleFromBigdecimal((BigDecimal)arr[2]));
				loan.setPrioAwordRate(StatisticsUtil.getDoubleFromBigdecimal((BigDecimal)arr[3]));
				double totalRate = Arith.round(
						Arith.add(loan.getPrioRate(),loan.getPrioAwordRate()),
						2);
				loan.setTotalRate(Arith.mul(totalRate, 100));
				resultList.add(loan);
			}
		}
		return resultList;
	}
}