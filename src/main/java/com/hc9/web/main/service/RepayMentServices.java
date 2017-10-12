package com.hc9.web.main.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.constant.PayURL;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Repaymentrecordparticulars;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.ParseXML;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.PageModel;
import com.hc9.web.main.vo.pay.P2pQuery;

/** 还款业务处理 */
@Service
public class RepayMentServices {

	@Resource
	private HibernateSupport dao;

	@Resource
	private PayLogService payLogService;
	
	/** 批量修改投资人收款记录表 **/
	public void updateRepayDetailList(List<Repaymentrecordparticulars> toUpdateRcpList) {
		LOG.error("待更新的还款明细记录： " + toUpdateRcpList.size() + " 条！");
		dao.saveOrUpdateAll(toUpdateRcpList);
		LOG.error("成功更新 " + toUpdateRcpList.size() + " 条投资人收益明细记录！");
	}
	
	/** 分红状态查询 */
	public String shareBonusState(String orderid ,Loansign loansig,Userbasicsinfo userinfo) {
		String resultStr = "";
		P2pQuery p2pQuery = new P2pQuery(orderid, 4);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		try {
			String shopFullxml = ParseXML.p2pQueryXml(p2pQuery);
			nvps.add(new BasicNameValuePair("requestParams", shopFullxml));
			nvps.add(new BasicNameValuePair("sign", CommonUtil.MD5(shopFullxml + "~|~" + ParameterIps.getMerchantKey())));
			payLogService.savePayLog(shopFullxml, userinfo.getId(), loansig.getId(), 13, orderid, 0.0, 0.0, 0.00);

			String result = CommonUtil.excuteRequest(PayURL.P2PQUERYTESTURL, nvps);
			Document doc = DocumentHelper.parseText(result);
			Element rootElt = doc.getRootElement(); // 获取根节点
			// 拿到crs节点下的子节点code值
			String code = rootElt.elementTextTrim("code");
			String msg = rootElt.elementTextTrim("msg");
			String sign = rootElt.elementTextTrim("sign");
			// 获取子节点crs下的子节点result
			Iterator iteratorResult = rootElt.elementIterator("result");
			boolean Judge = false; // 判断是否有值
			String state = "0"; // 0-失败 1-成功
			while (iteratorResult.hasNext()) {
				Element itemEle = (Element) iteratorResult.next();
				Iterator iteratorOrder = itemEle.elementIterator("order"); // 获取子节点result下的子节点order
				while (iteratorOrder.hasNext()) {
					Element elementOrder = (Element) iteratorOrder.next();
					state = elementOrder.elementTextTrim("state");
					String Md5sign = CommonUtil.MD5(code + "~|~" + msg + "~|~" + ParameterIps.getDes_algorithm());
					if (sign.equals(Md5sign)) {
						if (code.equals("CSD000")) {
							Judge = true;
							if (state.equals("1")) {
								return "CSD000";
							}
						} else if (code.equals("CSD333")) {
							return "CSD333";
						} else {
							return "CSD222";
						}
					} else {
						return "CSD222";
					}
				}
			}
			if (!Judge && state.equals("0")) {
				return "CSD333";
			}
			return "CSD222";
		} catch (Exception e) {
			LOG.error("订单号 " + orderid + " 查询宝付失败!", e);
			return "CSD222";
		}
	}
	
	/** 根据投资记录查询还款记录 */
	public List getRepaymentShareRecord(String loanrecordId) {
		StringBuffer sql = new StringBuffer(
				"select r.id,case loanType when 1 then money when 2 then middleMoney else afterMoney end,"
				+ "case loanType when 1 then preRepayMoney when 2 then middlePreRepayMoney else afterPreRepayMoney end,"
				+ "(select periods from repaymentrecord where id=r.repaymentrecordId),(select repayState from repaymentrecord where id=r.repaymentrecordId),(select preRepayDate from repaymentrecord where id=r.repaymentrecordId),"
				+ "case loanType when 1 then '优先' when 2 then '夹层' else '劣后' end from repaymentrecordparticulars r where r.loanrecordId=" + loanrecordId);
		List list = dao.findBySql(sql.toString());
		return list;
	}
	
	/** 还款清单 */
	public PageModel repaymentList(HttpServletRequest request,
			PageModel page,String loanName,String begin,String end,Integer search) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		StringBuffer sql = new StringBuffer("select rr.id,ls.name,rr.preRepayDate,rr.periods, ");
		sql.append("IFNULL((select sum(money)+sum(middleMoney)+sum(afterMoney)+sum(preRepayMoney)+sum(middlePreRepayMoney)+sum(afterPreRepayMoney) from repaymentrecord where id=rr.id)+IF(ls.loansignType_id=5,IFNULL(rr.companyPreFee,0),0),0),rr.repayState,");
		sql.append("rr.repayTime,(select count(1) from repaymentrecord where DATE_FORMAT(now(),'%Y-%m-%d') <= DATE_FORMAT(preRepayDate,'%Y-%m-%d') and repayState = 1 and id=rr.id),IFNULL(ls.id,null) ");
		sql.append("from repaymentrecord rr join loansign ls on rr.loanSign_id=ls.id where ls.status>=6 and ls.userbasicinfo_id="+user.getId());
		StringBuffer sqlCount = new StringBuffer("select count(1) from repaymentrecord rr join loansign ls on rr.loanSign_id=ls.id where ls.status>=6 and ls.userbasicinfo_id="+user.getId());

		if (StringUtil.isNotBlank(loanName)) { // 项目名称
			sql.append(" and ls.name like '%" + loanName + "%'");
			sqlCount.append(" and ls.name like '%" + loanName + "%'");
		}
		if (StringUtil.isNotBlank(begin)) { // 开始时间
			sql.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') >= '"
					+ begin + "'");
			sqlCount.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') >= '"
					+ begin + "'");
		}
		if (StringUtil.isNotBlank(end)) { // 结束时间
			sql.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') <= '"
					+ end + "'");
			sqlCount.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') <= '"
					+ end + "'");
		}
		if (search != null && !"".equals(search)) {
			sql.append(" and rr.preRepayDate<=now() and rr.preRepayDate>=DATE_SUB(now(),INTERVAL "+search+" MONTH) ");
			sqlCount.append(" and rr.preRepayDate<=now() and rr.preRepayDate>=DATE_SUB(now(),INTERVAL "+search+" MONTH) ");
		}
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());// 设置总条数量

		sql.append(" order by ls.`name` desc,rr.repayState,rr.preRepayDate desc LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* page.getNumPerPage()).append(",")
				.append(page.getNumPerPage());

		List list = dao.findBySql(sql.toString());
		page.setList(list);// project集合
		return page;
	}
	
	/**
	 * 我的融资-还款清单
	 * @param request
	 * @param page
	 * @param loanName
	 * @param begin
	 * @param end
	 * @param search
	 * @return
	 */
	public PageModel myRepaymentList(HttpServletRequest request, PageModel page, String loanName, String begin, String end, Integer search) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer(" select rr.id as repayRecordId, "
				+ " ls.name loanName, "
				+ " ls.remonth as loanPeriods, "
				+ " ls.issueLoan as loanAmount, "
				+ " ls.credit_time as creditTime, "
				+ " ls.`type` as loanType, "
				+ " ls.feeState, "
				+ " ls.refunway as refundWay, "
				+ " ls.inadvanceRepayPermit, "
				+ " ls.inadvanceRepayPermitThreshold, "
				+ " rr.preRepayDate, "
				+ " rr.periods, "
				+ " (select case when (t.repayState = 6 or t.repayState = 5) then t.realMoney + t.middleRealMoney + t.afterRealMoney "
				+ " else t.money + t.preRepayMoney + t.middleMoney + t.middlePreRepayMoney + t.afterMoney + t.afterPreRepayMoney "
				+ " end "
				+ " from repaymentrecord t "
				+ " where id = rr.id "
				+ " ) + IF(ls.loansignType_id = 5, IFNULL(rr.companyPreFee, 0), 0) as repayAmount, "
				+ " rr.repayState, "
				+ " rr.repayTime, "
				+ " (select count(1) from repaymentrecord where DATE_FORMAT(now(),'%Y-%m-%d') <= DATE_FORMAT(preRepayDate,'%Y-%m-%d') and repayState = 1 and id = rr.id) as overdue, "
				+ " ls.id as loanId "
				+ " from repaymentrecord rr "
				+ " left outer join loansign ls on rr.loanSign_id = ls.id "
				+ " where ls.status >= 6 "
				+ " and ls.userbasicinfo_id = ? ");
		
//		StringBuffer sql = new StringBuffer(" select rr.id as repayRecordId, "
//				+ " ls.name loanName, "
//				+ " ls.remonth as loanPeriods, "
//				+ " ls.issueLoan as loanAmount, "
//				+ " ls.credit_time as creditTime, "
//				+ " ls.`type` as loanType, "
//				+ " ls.feeState, "
//				+ " ls.refunway as refundWay, "
//				+ " ls.inadvanceRepayPermit, "
//				+ " ls.inadvanceRepayPermitThreshold, "
//				+ " rr.preRepayDate, "
//				+ " rr.periods, "
//				+ " IFNULL((select sum(money) + sum(middleMoney) + sum(afterMoney) + sum(preRepayMoney) + sum(middlePreRepayMoney) + sum(afterPreRepayMoney) "
//				+ " from repaymentrecord "
//				+ " where id = rr.id) + IF(ls.loansignType_id = 5, IFNULL(rr.companyPreFee, 0), 0), 0) as amount, "
//				+ " rr.repayState, "
//				+ " rr.repayTime, "
//				+ " (select count(1) "
//				+ " from repaymentrecord "
//				+ " where DATE_FORMAT(now(),'%Y-%m-%d') <= DATE_FORMAT(preRepayDate,'%Y-%m-%d') "
//				+ " and repayState = 1 "
//				+ " and id = rr.id) as overdue, "
//				+ " IFNULL(ls.id, null) as loanId "
//				+ " from repaymentrecord rr "
//				+ " left outer join loansign ls on rr.loanSign_id = ls.id "
//				+ " where ls.status >= 6 "
//				+ " and ls.userbasicinfo_id = ? ");
		
		StringBuffer sqlCount = new StringBuffer(" select count(1) from repaymentrecord rr "
				+ " left join loansign ls on rr.loanSign_id = ls.id "
				+ " where ls.status >= 6 "
				+ " and ls.userbasicinfo_id = ? ");
		params.add(user.getId());

		// 项目名称
		if (StringUtil.isNotBlank(loanName)) { 
			sql.append(" and ls.name like ? ");
			sqlCount.append(" and ls.name like ? ");
			params.add("%" + loanName + "%");
		}
		// 开始时间
		if (StringUtil.isNotBlank(begin)) { 
			sql.append(" and date_format(rr.preRepayDate, '%Y-%m-%d') >= ? ");
			sqlCount.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') >= ? ");
			params.add(begin);
		}
		// 结束时间
		if (StringUtil.isNotBlank(end)) { 
			sql.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') <= ? ");
			sqlCount.append(" and date_format(rr.preRepayDate,'%Y-%m-%d') <= ? ");
			params.add(end);
		}
		if (search != null && !"".equals(search)) {
			sql.append(" and rr.preRepayDate <= now() and rr.preRepayDate >= DATE_SUB(now(), INTERVAL ? MONTH) ");
			sqlCount.append(" and rr.preRepayDate <= now() and rr.preRepayDate >= DATE_SUB(now(), INTERVAL ? MONTH) ");
			params.add(search);
		}
		
		// 设置总条数量
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString(), params.toArray()).intValue());

		sql.append(" order by ls.`name` desc, rr.repayState, rr.preRepayDate desc LIMIT ")
			.append((page.getPageNum() - Constant.STATUES_ONE) * page.getNumPerPage())
			.append(",")
			.append(page.getNumPerPage());

		List list = dao.findBySql(sql.toString(), params.toArray());
		page.setList(list);
		return page;
	}
	
	public Long processRepaymentId(Long loan_id) {
		String sql = "select id from repaymentrecord where loanSign_id = ? and repayState in (1,3) order by periods limit 0,1 ";
		Object obj = dao.findObjectBySql(sql, loan_id);
		return obj != null ? Long.valueOf(obj.toString()) : 0;
	}
	
	/** 查询还款记录 */
	public List loansignRepaymentCount(Long userId, Long loansignId,
			Integer subType) {
		String sql = "select r.id,l.periods,l.repayState,l.preRepayDate,ifnull(r.money+r.preRepayMoney,0.00),IFNULL(r.middleMoney+r.middlePreRepayMoney,0.00),IFNULL(r.afterMoney+r.afterPreRepayMoney,0.00),IFNULL(r.money+r.realMoney,0.00),IFNULL(r.middleMoney+r.middleRealMoney,0.00),IFNULL(r.afterMoney+r.afterRealMoney,0.00) ,r.loanType from repaymentrecordparticulars  r ,repaymentrecord l where r.repaymentrecordId=l.id  and l.loanSign_id=?    and r.userId=?  and r.loanType=?";
		List list = dao.findBySql(sql, loansignId, userId, subType);
		return list;
	}

	public List loansignRepayment(Long userId, Long loansignId,
			Integer subType, int no) {
		String sql = "select r.id,l.periods,l.repayState,l.preRepayDate,ifnull(r.money+r.preRepayMoney,0.00),IFNULL(r.middleMoney+r.middlePreRepayMoney,0.00),IFNULL(r.afterMoney+r.afterPreRepayMoney,0.00),IFNULL(r.money+r.realMoney,0.00),IFNULL(r.middleMoney+r.middleRealMoney,0.00),IFNULL(r.afterMoney+r.afterRealMoney,0.00),r.loanType  from repaymentrecordparticulars  r ,repaymentrecord l where r.repaymentrecordId=l.id  and l.loanSign_id=?    and r.userId=? and r.loanType=?  LIMIT "
				+ no + ",10";
		List list = dao.findBySql(sql, loansignId, userId, subType);
		return list;
	}

	/** 交易记录-资金流水信息 */
	public PageModel tradeRecord(HttpServletRequest request,
			PageModel page,String begin,String end,Integer search,String type) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		StringBuffer sql = new StringBuffer("select id,explan,income,money,(DATE_FORMAT(time,'%Y-%m-%d %H:%m:%s')),fee,expenditure from accountinfo where userbasic_id = "+user.getId());
		StringBuffer sqlCount = new StringBuffer("select count(1) from accountinfo where userbasic_id = "+user.getId());

		if (StringUtil.isNotBlank(type)) {  // 资金流水类型
			sql.append(" and accounttype_id in (").append(type).append(") ");
			sqlCount.append(" and accounttype_id in (").append(type).append(") ");
		}
		if (StringUtil.isNotBlank(begin)) { // 开始时间
			sql.append(" and date_format(time,'%Y-%m-%d') >= '"
					+ begin + "'");
			sqlCount.append(" and date_format(time,'%Y-%m-%d') >= '"
					+ begin + "'");
		}
		if (StringUtil.isNotBlank(end)) { // 结束时间
			sql.append(" and date_format(time,'%Y-%m-%d') <= '"
					+ end + "'");
			sqlCount.append(" and date_format(time,'%Y-%m-%d') <= '"
					+ end + "'");
		}
		if (search != null && !"".equals(search)) {
			sql.append(" and time<=now() and time>=DATE_SUB(now(),INTERVAL "+search+" MONTH) ");
			sqlCount.append(" and time<=now() and time>=DATE_SUB(now(),INTERVAL "+search+" MONTH) ");
		}
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());// 设置总条数量

		sql.append(" order by id desc LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* page.getNumPerPage()).append(",")
				.append(page.getNumPerPage());

		List list = dao.findBySql(sql.toString() );
		page.setList(list);// project集合
		return page;
	}
}
