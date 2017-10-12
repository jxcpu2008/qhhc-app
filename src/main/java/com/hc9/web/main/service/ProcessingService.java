package com.hc9.web.main.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Recharge;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.entity.Withdraw;
import com.hc9.web.main.util.Constant;

/** 支付接口返回信息的业务处理 */
@Service
public class ProcessingService {
	@Resource
	private HibernateSupport dao;

	/** 宝付充值记录orderid */
	public Recharge findRechargeByOrderId(String orderid) {
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM recharge WHERE orderNum=?");
		return dao.findObjectBySql(sb.toString(), Recharge.class, orderid);
	}

	/** 宝付提现记录orderid  */
	public Withdraw findForchargebyorderid(String orderid) {
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM withdraw WHERE strNum=?");
		return dao.findObjectBySql(sb.toString(), Withdraw.class, orderid);
	}

	/** 修改标当前期的还款状态 */
	public synchronized int updateRayment(Repaymentrecord repaymentrecord) {
		try {
			dao.update(repaymentrecord);
			return Constant.STATUES_ZERO;
		} catch (Exception e) {
			return Constant.STATUES_ONE;
		}
	}

	/** 修改标的状态 */
	public synchronized int updateLoan(Loansign loan) {
		try {
			dao.update(loan);
			return Constant.STATUES_ZERO;
		} catch (Exception e) {
			return Constant.STATUES_ONE;
		}
	}

	/** 更新充值记录 */
	public Boolean updaterecharge(Recharge recharge) {
		dao.update(recharge);
		return false;
	}
}
