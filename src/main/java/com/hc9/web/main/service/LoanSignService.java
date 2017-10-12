package com.hc9.web.main.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Costratio;
import com.hc9.web.main.entity.Loandynamic;
import com.hc9.web.main.entity.Loanrecord;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.sys.vo.LoanDynamicVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.MoneyUtil;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoanContract;
import com.hc9.web.main.vo.LoanRecommendVo;
import com.hc9.web.main.vo.LoanlistVo;
import com.hc9.web.main.vo.PageModel;

/** 标的通用查询 */
@Service
public class LoanSignService {

	@Resource
	HibernateSupport dao;

	/** 根据sql语句查询出条数 */
	public int queryCount(String sql) {
		Object object = dao.findObjectBySql(sql, null);
		return object != null ? Integer.parseInt(object.toString()) : 0;
	}

	/** 根据主键id获取标的对象信息 */
	public Loansign getLoansignById(String id) {
		try {
			String sql = "select * from loansign where id=?";
			Loansign loansign = dao.findObjectBySql(sql, Loansign.class, id);
			return loansign;
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	/** 查询Loansign */
	public Loansign getLoansign(String id) {
		try {
			String sql = "select * from loansign where id=?";
			Loansign loansign = dao.findObjectBySql(sql, Loansign.class, id);
			return loansign;
		} catch (DataAccessException e) {
			return null;
		}
	}

	/** 根据用户Id，项目ID、投标类型 */
	public Loanrecord getLoanRecordID(String loanrecordId) {
		try {
			String sql = "select  * from loanrecord where  id=? ";
			Loanrecord loanrecord = dao.findObjectBySql(sql, Loanrecord.class,
					loanrecordId);
			return loanrecord;
		} catch (Exception e) {
			return null;
		}
	}

	/** 获取最后一期还款日期 */
	public String getPreRepayDate(String loansignId) {
		String sql = "select MAX(l.preRepayDate) from repaymentrecord l where l.loanSign_id=? ";
		Object preRepayDate = dao.findObjectBySql(sql, loansignId);
		if (preRepayDate != null) {
			return preRepayDate.toString();
		} else {
			return null;
		}
	}

	/** 查询到平台当前的费用比例 */
	public Costratio queryCostratio() {
		 List<Costratio> list = dao.find("from Costratio");
	        if(list != null && list.size() > 0){
	            return list.get(0);
	        }
	        return null;
	}

	/** 更新投资记录 */
	public void updateLoanRecord(Loanrecord loanrecord, Loansign loansign) {
		dao.update(loanrecord);
		dao.update(loansign);
	}

	/** 通过id */
	public Loanrecord getLoanRecord(String id) {
		try {
			return dao.get(Loanrecord.class, Long.valueOf(id));
		} catch (DataAccessException e) {
			return null;
		}
	}

	/** 投资记录 */
	public PageModel getloanrecord(HttpServletRequest request, PageModel page,
			Integer state, String beginTime, String endTime, Integer timeno,
			Userbasicsinfo user) {
		page.setNumPerPage(5);

		String status = "";
		if (state != null) {
			if (state == 1) {
				status = " and ls.status in (1,2,3,4,5)";
			} else if (state == 2) {
				status = " and ls.status in (6,7)";
			} else if (state == 3) {
				status = " and  ls.status=8 ";
			}
		} else {
			status = " and ls.state = 2 and ls.status in (1,2,3,4,5,6,7,8)";
		}
		
		StringBuffer sql = new StringBuffer("select ls.id,ls.name,lr.tenderMoney,");
		sql.append("lr.tenderTime,lr.subType,ls.prio_rate,lr.isSucceed,ls.prio_aword_rate,lr.pContractNo,");
		sql.append("IF(ls.status = 8,(select sum(ifnull(rmp.realMoney,0)+IFNULL(rmp.middleRealMoney,0)+IFNULL(rmp.afterRealMoney,0))-lr.tenderMoney");
		sql.append(" from repaymentrecordparticulars rmp where rmp.loanrecordId=lr.id and rmp.repState = 1), ");
		sql.append(" IF(loansignType_id = 5,(select sum(ifnull(rmp.realMoney,0)-ifnull(rmp.money,0)) from repaymentrecordparticulars rmp where rmp.loanrecordId=lr.id and rmp.repState = 1),");
		sql.append(" (select sum(ifnull(rmp.realMoney,0)+IFNULL(rmp.middleRealMoney,0)+IFNULL(rmp.afterRealMoney,0))");
		sql.append("from repaymentrecordparticulars rmp where rmp.loanrecordId=lr.id and rmp.repState = 1)))+");
		sql.append("IFNULL((select incomeMoney from voteincome where loanRecordId=lr.id and status = 1),0)");
		sql.append(",IFNULL(lr.id,0),IF(lr.redEnvelopeMoney > 0,lr.redEnvelopeMoney,''), ");
		sql.append("(select interestRate from interestincreasecard where loanrecordId = lr.id LIMIT 1),ls.loansignType_id");
		sql.append(",ls.status,ls.type,ls.remonth,ls.credit_time,DATE_ADD(STR_TO_DATE(ls.credit_time,'%Y-%m-%d %H:%i:%s'),INTERVAL ls.remonth month) AS repayment_time");
		sql.append(",(SELECT sum(IFNULL(rmp.preRepayMoney,0)+IFNULL(rmp.middlePreRepayMoney,0)+IFNULL(rmp.afterPreRepayMoney,0)) from repaymentrecordparticulars rmp,repaymentrecord rp where rmp.repaymentrecordId=rp.id and rmp.loanrecordId=lr.id )");
		sql.append("+IFNULL((select incomeMoney from voteincome where loanRecordId=lr.id),0)");
		sql.append(",t.number");
		sql.append(",DATE_ADD(STR_TO_DATE(ls.credit_time,'%Y-%m-%d %H:%i:%s'),INTERVAL ls.remonth day) AS repayment_add_time");
		sql.append(" from loansign ls ,loanrecord lr ");
		sql.append(" ,(select id,number,tenderTime from (select ls.id,count(*) number,MAX(lr.tenderTime) tenderTime from loansign ls ,loanrecord lr where  lr.loanSign_id=ls.id and lr.isSucceed=1 and lr.userbasicinfo_id=");
		sql.append(user.getId());
		sql.append(" and ls.state = 2 ");
		sql.append(status);
		sql.append(" group by ls.id order by  lr.tenderTime desc LIMIT ");
		sql.append((page.getPageNum() - Constant.STATUES_ONE)* page.getNumPerPage());
		sql.append(",");
		sql.append(page.getNumPerPage());
		sql.append(" ) tt) t");
		sql.append(" where lr.loanSign_id=ls.id and lr.isSucceed=1 and lr.userbasicinfo_id="+user.getId());
		sql.append(" and t.id =ls.id");

		StringBuffer sqlCount = new StringBuffer();
		sqlCount.append("select count(id) from (");
		sqlCount.append("select ls.id from loansign ls ,loanrecord lr ");
		sqlCount.append("where  lr.loanSign_id=ls.id and lr.isSucceed=1 and lr.userbasicinfo_id=");
		sqlCount.append(user.getId());
		
		sql.append(status);
		sqlCount.append(status);

		if (StringUtil.isNotBlank(beginTime)) {
			sql.append(" and DATE_FORMAT(lr.tenderTime,'%y-%m-%d') >= ").
			append("DATE_FORMAT('"+beginTime).append("','%y-%m-%d')");
			sqlCount.append(" and DATE_FORMAT(lr.tenderTime,'%y-%m-%d') >= ").
			append("DATE_FORMAT('"+beginTime).append("','%y-%m-%d')");
		}

		if (StringUtil.isNotBlank(endTime)) {
			sql.append(" and DATE_FORMAT(lr.tenderTime,'%y-%m-%d') <= ").
			append("DATE_FORMAT('"+endTime).append("','%y-%m-%d')");
			sqlCount.append(" and DATE_FORMAT(lr.tenderTime,'%y-%m-%d') <= ").
			append("DATE_FORMAT('"+endTime).append("','%y-%m-%d')");
		}

		if (timeno != null && !"".equals(timeno)) {
			sql.append(" and DATE_SUB(CURDATE(), INTERVAL " + timeno
					+ " MONTH) <= DATE_FORMAT(lr.tenderTime,'%y-%m-%d')");
			sqlCount.append(" and DATE_SUB(CURDATE(), INTERVAL " + timeno
					+ " MONTH) <= DATE_FORMAT(lr.tenderTime,'%y-%m-%d')");
		}
		sqlCount.append(" group by ls.id");
		sqlCount.append(") tt");

		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());// 设置总条数量
		sql.append("  order by t.tenderTime desc,ls.id desc, lr.tenderTime desc  ");

		List<Loanrecord> list = dao.findBySql(sql.toString());
		page.setList(list);// Loansign集合
		return page;
	}

	/** 根据还款计划任务表ID 查询还款记录详情 */
	public Repaymentrecord getrepaymentrecordByid(Long id) {
		String sql = "SELECT * from repaymentrecord where id=?";
		List<Repaymentrecord> list = dao.findBySql(sql, Repaymentrecord.class,
				id);
		return list.size() > 0 ? list.get(list.size() - 1) : null;
	}

	/** 查询购买成功的 */
	public Double getSumLoanTenderMoney(String loansignId) {
		if (StringUtil.isNotBlank(loansignId)) {
			String sql = "select sum(tenderMoney) from loanrecord where loanSign_id=? and isSucceed=1";
			Object tenderMoney = dao.findObjectBySql(sql, loansignId);
			if (tenderMoney != null) {
				return Double.valueOf(tenderMoney.toString());
			} else {
				return 0.00;
			}
		} else {
			return 0.00;
		}
	}

	/** 收益明细-投资记录  */
	@SuppressWarnings("rawtypes")
	public PageModel getLoanIncome(PageModel page, Long userId,
			String beginTime, String endTime, Integer timeno) {

		StringBuffer sql = new StringBuffer(
				"SELECT ls.name,lr.subType,lr.tenderMoney,lr.tenderTime,lr.order_id,lr.id,ifnull(ls.id,0),ls.loansignType_id,ls.type,ls.remonth,"
						+ "(select rr.preRepayDate from repaymentrecordparticulars rp,repaymentrecord rr where rp.repaymentrecordId=rr.id and rp.loanrecordId=lr.id order by rr.periods desc limit 0,1)"
						+ " from loanrecord lr,loansign ls "
						+ " where lr.userbasicinfo_id=? and lr.isSucceed=1 and lr.loanSign_id = ls.id and ls.status >= 6 and ls.status <= 8 ");
		StringBuffer sqlCount = new StringBuffer(
				"SELECT count(1) from loanrecord lr,loansign ls "
						+ " where lr.userbasicinfo_id=?  and lr.isSucceed=1 and lr.loanSign_id = ls.id and ls.status >= 6 and ls.status <= 8 ");

		if (beginTime != null) {
			sql.append(" and lr.tenderTime >= '").append(beginTime)
					.append(" ' ");
			sqlCount.append(" and lr.tenderTime >= '").append(beginTime)
					.append(" ' ");
		}

		if (endTime != null) {
			sql.append(" and lr.tenderTime <= '").append(endTime)
					.append(" ' ");
			sqlCount.append(" and lr.tenderTime <= '").append(endTime)
					.append(" ' ");
		}
		if (timeno != null) {
			sql.append(" and lr.tenderTime<=now() and lr.tenderTime>=DATE_SUB(now(),INTERVAL "+timeno+" MONTH) ");
			sqlCount.append(" and lr.tenderTime<=now() and lr.tenderTime>=DATE_SUB(now(),INTERVAL "+timeno+" MONTH) ");
		}

		page.setTotalCount(dao.queryNumberSql(sqlCount.toString(), userId)
				.intValue());
		sql.append(" order by lr.tenderTime desc LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* page.getNumPerPage()).append(",")
				.append(page.getNumPerPage());
		List list = dao.findBySql(sql.toString(), userId);
		page.setList(list);
		return page;
	}
	
	// 通过投资信息查询还款信息
	public List findDetailBylrId(Long lrId) {
		StringBuffer buf = new StringBuffer("SELECT rmp.id,rp.periods, ");
		buf.append("(IFNULL(rmp.money,0)+IFNULL(rmp.middleMoney,0)+IFNULL(rmp.afterMoney,0)),");
		buf.append("(IFNULL(rmp.preRepayMoney,0)+IFNULL(rmp.middlePreRepayMoney,0)+IFNULL(rmp.afterPreRepayMoney,0)),");
		buf.append("rp.preRepayDate,");
		buf.append("(IFNULL(rmp.realMoney,0)+IFNULL(rmp.middleRealMoney,0)+IFNULL(rmp.afterRealMoney,0)),rmp.repState, ");
		buf.append("(select incomeMoney from voteincome where loanRecordId=lr.id),(select incomeMoney from voteincome where loanRecordId=lr.id and status = 1) ");
		buf.append(",ls.prio_rate,ls.prio_aword_rate,(select interestRate from interestincreasecard where loanrecordId = lr.id) ");
		buf.append("from loanrecord lr,repaymentrecordparticulars rmp,repaymentrecord rp,loansign ls ");
		buf.append("where rmp.repaymentrecordId=rp.id and rmp.loanrecordId=lr.id and lr.id=? and lr.loanSign_id=ls.id order by rp.preRepayDate desc ");
		List list = dao.findBySql(buf.toString(), lrId);
		return list;
	}
	
	// 查询未还款的记录总数
	public Integer notRepaymentCount(Long lrId) {
		StringBuffer buf = new StringBuffer("SELECT count(1) "); 
		buf.append("from loanrecord lr,repaymentrecordparticulars rmp,repaymentrecord rp ");
		buf.append("where rmp.repaymentrecordId=rp.id and rmp.loanrecordId=lr.id and lr.id=? and repState<>1 ");
		Object obj = dao.findObjectBySql(buf.toString(), lrId);
		return obj != null ? Integer.valueOf(obj.toString()) : 0;
	}
	
	/** 项目投资记录 */
	public PageModel getLoanrecordList(Long Id, PageModel page) {
		StringBuffer sql = new StringBuffer(
				"select lr.tenderMoney,lr.tenderTime,lr.subType,lr.webOrApp,u.userName,lr.loanType "
						+ "from loanrecord lr,userbasicsinfo u  where u.id=lr.userbasicinfo_id and  lr.isSucceed =1 and  lr.loanSign_id=?");

		StringBuffer sqlCount = new StringBuffer(
				"select count(lr.id) from loanrecord lr,userbasicsinfo u  where u.id=lr.userbasicinfo_id and  lr.isSucceed =1 and  lr.loanSign_id="
						+ Id);

		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());

		sql.append(" order by lr.tenderTime desc  LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* Constant.SRSRUES_TEN).append(",")
				.append(page.getNumPerPage());

		List list = dao.findBySql(sql.toString(), Id);
		page.setList(list);// project集合
		return page;
	}
	
	public List getAttachMent(String loanId, String type) {

		String sql = "select at.id,a.originalName,a.attachmentType,a.attachmentName,a.adminuser_id,`at`.attachment_name "
				+ "from attachment a,attachment_type at where a.attachmentType=`at`.id "
				+ " and a.attachmentType="
				+ type
				+ " and a.loansign_id="
				+ loanId;

		List list = dao.findBySql(sql.toString());
		return list;
	}
	
	/** 根据还款编号查询还款记录  */
	public Repaymentrecord getRepaymentId(Long id) {
		Repaymentrecord repaymentrecord = dao.get(Repaymentrecord.class, id);
		return repaymentrecord;
	}
	
	/** 统计购买人数 */
	public Object getTenderCount(String pId) {
		String sql = "select count(1) from loanrecord where isSucceed=1 and  loanSign_id="
				+ pId;
		Object obj = dao.findObjectBySql(sql.toString());
		return obj;
	}

	public PageModel queryLoansignList(HttpServletRequest request,
			PageModel page, String loanName, String begin, String end,
			Integer search, Integer state) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		StringBuffer sql = new StringBuffer(
				"select ls.id,ls.name,ls.status,ls.issueloan,ls.publish_time,");
		sql.append("IFNULL((select sum(money)+sum(middleMoney)+sum(afterMoney)+sum(preRepayMoney)");
		sql.append("+sum(middlePreRepayMoney)+sum(afterPreRepayMoney) from repaymentrecord where loanSign_id = ls.id),0),");
		sql.append("IFNULL((select sum(tenderMoney) from loanrecord where isSucceed = 1 and loanSign_id=ls.id),0) ");
		sql.append("from loansign ls where ls.state=2 and ls.userbasicinfo_id=?");

		StringBuffer sqlCount = new StringBuffer(
				"select count(ls.id) from loansign ls where ls.state=2 and ls.userbasicinfo_id=");
		sqlCount.append(user.getId());
		if (StringUtil.isNotBlank(loanName)) { // 项目名称
			sql.append(" and ls.name like '%" + loanName + "%'");
			sqlCount.append(" and ls.name like '%" + loanName + "%'");
		}
		if (StringUtil.isNotBlank(begin)) { // 开始时间
			sql.append(" and date_format(ls.publish_time,'%Y-%m-%d') >= '"
					+ begin + "'");
			sqlCount.append(" and date_format(ls.publish_time,'%Y-%m-%d') >= '"
					+ begin + "'");
		}
		if (StringUtil.isNotBlank(end)) { // 结束时间
			sql.append(" and date_format(ls.publish_time,'%Y-%m-%d') <= '"
					+ end + "'");
			sqlCount.append(" and date_format(ls.publish_time,'%Y-%m-%d') <= '"
					+ end + "'");
		}
		if (search != null && !"".equals(search)) {
			sql.append(" and DATE_SUB(CURDATE(), INTERVAL " + search
					+ " MONTH) <= ls.publish_time ");
			sqlCount.append(" and DATE_SUB(CURDATE(), INTERVAL " + search
					+ " MONTH) <= ls.publish_time ");
		}
		if (state != null) {
			if (state == 1) {
				sql.append(" and ls.status=1"); // 进行中
				sqlCount.append(" and ls.status=1");
			} else if (state == 2) {
				sql.append(" and ls.status in (2,3,4,5)");// 满标中
				sqlCount.append(" and ls.status in (2,3,4,5)");
			} else if (state == 3) {
				sql.append(" and ls.status in (6,7)");// 还款中
				sqlCount.append(" and ls.status in (6,7)");
			} else if (state == 4) {
				sql.append(" and ls.status=8");// 已完成
				sqlCount.append(" and ls.status=8");
			}
		}
		page.setTotalCount(dao.queryNumberSql(sqlCount.toString()).intValue());// 设置总条数量
		sql.append(" order by ls.id desc  LIMIT ")
				.append((page.getPageNum() - Constant.STATUES_ONE)
						* page.getNumPerPage()).append(",")
				.append(page.getNumPerPage());

		List list = dao.findBySql(sql.toString(), user.getId());
		page.setList(list);// Loansign集合
		return page;
	}

	public Loansign getloansign(HttpServletRequest request, String loanid) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		String sql = " select * from loansign where id=?";
		List list = dao.findBySql(sql, Loansign.class, loanid);
		Loansign ls = (Loansign) (list.size() > 0 ? list.get(0) : null);
		return ls;
	}

	/** 展示在首页的标信息 */
	public List<Object[]> getLoansignlist() {
		String sql = "select ls.id, ls.name,ls.remonth,ls.issueLoan,ls.loanUnit,ls.rest_money,ls.prio_rate,ls.prio_aword_rate,ls.type,ls.status,ls.loansignType_id,ls.activityStatus  "
				+ " from loansign ls where ls.state=2 and (ls.status >0 and ls.status <9) and (ls.onIndex =1 or ls.recommend =1) "
				+ " ORDER BY ls.rest_money DESC, ls.publish_time DESC,ls.state LIMIT 0 ,8";
		List<Object[]> list = dao.findBySql(sql);
		return list;
	}
	
	/** 查询首页的标列表信息 并缓存 */
	public List<LoanlistVo> updateLoanlist() {
		List<LoanlistVo> loanList = new ArrayList<>();
		List<Object[]> list = getLoansignlist();
		
		if(list == null || list.size() <= 0){
			return loanList;
		}
		
		for(int i=0;i<list.size();i++){
			LoanlistVo vo=new LoanlistVo();
			vo.setId(list.get(i)[0].toString());
			vo.setName(list.get(i)[1].toString());
			vo.setRemonth(list.get(i)[2].toString());
			vo.setIssueLoan(list.get(i)[3].toString());
			vo.setLoanUnit(list.get(i)[4].toString());
			vo.setRestMoney(list.get(i)[5].toString());
			vo.setPrioRate(list.get(i)[6].toString());
			vo.setPrioAwordRate(list.get(i)[7].toString());
			vo.setType(list.get(i)[8].toString());
			vo.setStatus(list.get(i)[9].toString());
			vo.setTypeId(list.get(i)[10].toString());
			vo.setActivityStatus(list.get(i)[11]==null?"0":list.get(i)[11].toString());
			loanList.add(vo);
		}
		WebCacheManagerUtil.setWebIndexLoanListToRedis(loanList);
		return loanList;
	}

	/** 项目动态 */
	public List<Loandynamic> getLoanLoandynamic() {
		String sql = "SELECT * FROM loandynamic ld order by ld.publishTime desc ";
		List<Loandynamic> list = dao.findBySql(sql, Loandynamic.class);
		if(list != null && list.size() > 0) {
			List<LoanDynamicVo> dynamicList = new ArrayList<LoanDynamicVo>();
			for(Loandynamic dynamic : list) {
				LoanDynamicVo vo = new LoanDynamicVo();
				vo.setLoanId(dynamic.getLoanId());
				vo.setTitle(dynamic.getTitle());
				dynamicList.add(vo);
			}
			WebCacheManagerUtil.setWebLoanDynamicListToRedis(dynamicList);
		}
		return list;
	}

	/** 热门推荐 */
	public List<LoanRecommendVo> getRecommand() {
		List<LoanRecommendVo> recommandList = new ArrayList<LoanRecommendVo>();
		String sql = "SELECT ls.id,ls.`name`,lb.recommandReason,lb.riskAdvice,ls.loansignType_id FROM loansign ls JOIN loansignbasics lb ON ls.id=lb.id WHERE ls.state=2 and (ls.status >0 and ls.status <9) and ls.recommend=1 LIMIT 0,2";
		List<Object[]> list = dao.findBySql(sql);
		if(null == list || list.size() <= 0){
			return recommandList;
		}
		for(int i=0;i<list.size();i++){
			LoanRecommendVo vo=new LoanRecommendVo();
			vo.setId(list.get(i)[0].toString());
			vo.setName(list.get(i)[1].toString());
			vo.setReason(list.get(i)[2]!=null?list.get(i)[2].toString():"");
			vo.setAdvice(list.get(i)[3]!=null?(list.get(i)[3].toString()):"");
			vo.setTypeId(list.get(i)[4]!=null?(list.get(i)[4].toString()):"");
			recommandList.add(vo);
		}
		WebCacheManagerUtil.setWebRecommandLoanListToRedis(recommandList);
		return recommandList;
	}

	public Double queryFinancSumByUser(Long user_id) {
		String sql = "select sum(issueLoan) from loansign where userbasicinfo_id = ? and state = 2";
		Object sum_mon = dao.findObjectBySql(sql, user_id);
		return sum_mon != null ? Double.valueOf(sum_mon.toString()) : 0D;
	}
	
	/** 累计投资金额 */
	public String gettotalInvestment(String key) {
		String sql="SELECT SUM(tenderMoney) FROM loanrecord WHERE isSucceed=1";
		String value=String.valueOf(dao.findObjectBySql(sql, null));
		RedisHelper.set(key, value);
		return value;
	}
	
	/** 合同数据封装 */
	public LoanContract packageContactData(Userbasicsinfo user,Loansign loansign, Loanrecord loanrecord) {
		LoanContract loanContract=new LoanContract();
		// 出借人
		loanContract.setPartyAName(user.getName());
		loanContract.setPartyACardType("身份证");
		loanContract.setPartyACardNo(user.getUserrelationinfo().getCardId());
		loanContract.setPartyAPhone(user.getUserrelationinfo().getPhone());
		// 借款人
		Userbasicsinfo jkUser=loansign.getUserbasicsinfo();
		loanContract.setPartyBName(jkUser.getName());
		loanContract.setPartyBCardType("身份证");
		loanContract.setPartyBCardNo(jkUser.getUserrelationinfo().getCardId());
		loanContract.setPartyBPhone(jkUser.getUserrelationinfo().getPhone());

		// 投资类型
		double typeRate=0.0;
		if (loanrecord.getSubType() == 1) {
			loanContract.setSubType("优先");
			typeRate=Arith.round((loansign.getPrioRate()+loansign.getPrioAwordRate()), 3);
			if (loansign.getRefunway() == 1) {
				loanContract.setBonaType("按月付息");
			} else {
				loanContract.setBonaType("按季度");
			}
			loanContract.setBonaType(loanContract.getBonaType()+"，到期还本");
		} else if (loanrecord.getSubType() == 2) {
			loanContract.setSubType("夹层");
			typeRate=Arith.round(loansign.getMidRate(), 3);
			loanContract.setBonaType("到期还本付息");
		} else if (loanrecord.getSubType() == 3) {
			loanContract.setSubType("劣后");
			typeRate=Arith.round(loansign.getAfterRate(), 3);
			loanContract.setBonaType("到期还本付息");
		}
		loanContract.setSubTypeRate(Arith.round(Arith.mul(typeRate, 100), 2)+ "%");
		loanContract.setLoanMoney(loanrecord.getTenderMoney());
		loanContract.setLoansignId(loansign.getContractNo());
		loanContract.setContractId(loanrecord.getpContractNo());
		loanContract.setLoansignName(loansign.getName());

		String preRepayDate = getPreRepayDate(loansign.getId().toString());
		if (preRepayDate != null) {
			loanContract.setRepayYear(preRepayDate.substring(0, 4));
			loanContract.setRepayMonth(preRepayDate.substring(5, 7));
			loanContract.setRepayDay(preRepayDate.substring(8, 10));
		}
		loanContract.setCreditYear(loansign.getCreditTime().substring(0, 4));
		loanContract.setCreditMonth(loansign.getCreditTime().substring(5, 7));
		loanContract.setCreditDay(loansign.getCreditTime().substring(8, 10));
		if (loansign.getType() == 2) {
			loanContract.setBorrowMonth(loansign.getRemonth().toString()
					+ "个月(项目)");
		} else if (loansign.getType() == 3) {
			loanContract.setBorrowMonth(loansign.getRemonth().toString()
					+ "天(天标)");
		}
		loanContract.setLoanMoneyUpper(MoneyUtil.digitUppercase(loanrecord
				.getTenderMoney()));
		if (loansign.getLoansignbasics().getBehoof() != null) {
			loanContract.setBehoof(loansign.getLoansignbasics().getBehoof());
		} else {
			loanContract.setBehoof("");
		}
		return loanContract;
	}
}
