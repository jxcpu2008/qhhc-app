package com.hc9.web.main.service;

import java.util.List;

import net.sf.json.JSONObject;

import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.RepaymentRecordDetail;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.vo.RepaymentRequest;

/**
 * 还款服务接口
 * @author Administrator
 *
 */
public interface IRepaymentService {
	
	/**
	 * 天标提前还款
	 * @param repaymentRequest
	 * @param repaymentRecordDetails
	 */
	public void applyInAdvanceRepay(RepaymentRequest repaymentRequest, List<RepaymentRecordDetail> repaymentRecordDetails) throws Exception;
	
	/**
	 * 多月标提前还款
	 * @param repaymentRequest
	 * @param repayRecords
	 * @param investRecords
	 * @throws Exception
	 */
	public void applyInAdvanceRepay(RepaymentRequest repaymentRequest, List<Repaymentrecord> repayRecords, List<Loanrecord> investRecords) throws Exception;
	
	/**
	 * 还款
	 * @param repaymentRequest
	 * @param repaymentRecordDetails
	 */
	public JSONObject repay(RepaymentRequest repaymentRequest, List<RepaymentRecordDetail> repaymentRecordDetails) throws Exception;
	
	/**
	 * 逾期还款
	 * @param repaymentRequest
	 * @param repaymentRecordDetails
	 */
	public JSONObject overdueRepay(int overdueDays, RepaymentRequest repaymentRequest, List<RepaymentRecordDetail> repaymentRecordDetails) throws Exception;
}