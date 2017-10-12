package com.hc9.web.main.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.dao.CacheManagerDao;
import com.hc9.web.main.entity.ActivityMonkey;
import com.hc9.web.main.entity.Loansign;
import com.hc9.web.main.entity.Loansignbasics;
import com.hc9.web.main.entity.Repaymentrecord;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.redis.sys.vo.LoansignTypeVo;
import com.hc9.web.main.redis.sys.vo.LoansignVo;
import com.hc9.web.main.redis.sys.vo.LoansignbasicsVo;
import com.hc9.web.main.redis.sys.vo.RepaymentrecordVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoginRelVo;
import com.hc9.web.main.vo.PageModel;
import com.jubaopen.commons.LOG;

@Service
public class CacheManagerService {

	@Resource
	private CacheManagerDao cacheManagerDao;
	
	@Resource
	private UserbasicsinfoService userbasicsinfoService;
	
	@Resource
	private LoanManageService loanManageService;
	
	@Resource
	private MemberCenterService memberCenterService;
	
	@Resource
	private LoanSignService loanSignService;
	
	@Resource
	private LoanrecordService loanrecordService;
	
	@Resource
    AppCacheService appCacheService;
	
	public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            20,
            20,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadPoolExecutor.CallerRunsPolicy());
	
	/** 累计投资相关缓存更新 */
	public Map<String, String> updateTotalInvestMoney() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("累计投资相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		loanSignService.gettotalInvestment("INT:HC9:INDEX:INVEST:TOTAL:NUMS");
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("累计投资相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 总注册人数相关缓存更新 */
	public Map<String, String> updateTotalRegisterNum() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("总注册人数相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		userbasicsinfoService.getcurrentRegUsers("INT:HC9:USR:REGISTER:TOTAL:NUMS");
		
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("总注册人数相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 热门推荐列表相关缓存更新 */
	public Map<String, String> hotIntroduceLoanList() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("热门推荐相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		loanSignService.getRecommand();
		appCacheService.updateAppIndexLoanListCache();
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("热门推荐相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 首页标列表更新 */
	public Map<String, String> updateIndexLoanList() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("首页标列表相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		loanSignService.updateLoanlist();
		appCacheService.updateAppIndexLoanListCache();
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("首页标列表相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 我要众持页面标列表更新 */
	public Map<String, String> updateZhongChiPageLoanList() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("我要众持标列表相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		PageModel page = new PageModel();
		page.setPageNum(1);
		page.setNumPerPage(8);
		page = loanManageService.getLoanList(null, null, null, null, page);
		SysCacheManagerUtil.setBuyPayLoanListCache(page);
		appCacheService.updateAppInvestLoanListCache();//app我要投资列表
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("我要众持标列表相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 待回款项目列表相关缓存更新 */
	public Map<String, String> updateToReturnLoanList() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("待回款项目列表相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		loanSignService.getLoanLoandynamic();
		
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("待回款项目列表相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 注册成功后更新用户相关缓存信息 */
	public void updateRegisterRelCache(String userName, String phone) {
		SysCacheManagerUtil.increaseTotalRegisterNum();
		LoginRelVo loginRelVo = cacheManagerDao.queryLoginRelVoBy(userName, phone);
		if(loginRelVo != null) {
			updateLoginRelVoToRedis(loginRelVo);
		}
	}
	
	/** 更新用户信息至redis缓存中 */
	public void updateLoginRelVoToRedis(LoginRelVo loginRelVo) {
		String userName = loginRelVo.getUserName();
		String phone = loginRelVo.getPhone();
		String staffNo = loginRelVo.getStaffNo();
		
		String jsonData = JsonUtil.toJsonStr(loginRelVo);
		/** 登录账号相关对应redis信息 */
		if(StringUtil.isNotBlank(userName)) {
			SysCacheManagerUtil.setLoginRelVoByUserName(userName, jsonData);
		}
		
		/** 手机号对应相关redis信息 */
		if(StringUtil.isNotBlank(phone)) {
			LoginRelVo oldLoginRelVo = SysCacheManagerUtil.getLoginRelVoById("" + loginRelVo.getId());
			if(oldLoginRelVo!=null){
				String oldPhone = oldLoginRelVo.getPhone();
				if(!phone.equals(oldPhone)) {
					String phoneKey = "STR:HC9:USR:LOGING:REL:PHONE:" + oldPhone;
					RedisHelper.del(phoneKey);
				}
			}
			SysCacheManagerUtil.setLoginRelVoByhone(phone, jsonData);
		}
		
		/** 用户id对应相关的redis信息 */
		SysCacheManagerUtil.setLoginRelVoById(loginRelVo.getId(), jsonData);
		
		/** 员工编号对应的相关信息 */
		if(StringUtil.isNotBlank(staffNo)) {
			SysCacheManagerUtil.setLoginRelVoByStaffNo(staffNo, jsonData);
		}
	}
	
	/** 满标放款根据项目id更新用户的回款记录 */
	public void updateUserRepaymentListByLoanSignId(Long loanSignId) {
		updateLoanDetailRelCache("" + loanSignId);
		List<Long> userIdList = cacheManagerDao.queryUserIdListByLoanSignId(loanSignId);
		if(userIdList != null && userIdList.size() > 0) {
			for(Long userId : userIdList) {
				memberCenterService.repaymentBackList(userId);//用户的还款和回款还款信息更新
				System.out.println("项目" + loanSignId + "满标放款更新用户" + userId + "对应回款列表信息成功！");
				LOG.error("项目" + loanSignId + "满标放款更新用户" + userId + "对应回款列表信息成功！");
			}
		}
	}
	
	/** 更新标详情缓存信息 */
	@SuppressWarnings("rawtypes")
	public Map<String, String> updateLoanDetailRelCache(String loanId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("更新标详情缓存信息至redis开始！");
		long start = System.currentTimeMillis();
		
		Loansign loan = loanSignService.getLoansignById(loanId);
		if(loan != null) {
			String borrwerKey = "LST:HC9:LOANSIGN:BORROWER:LOANID:" + loanId;
			String borrwer = "暂无";
			
			if(StringUtil.isNotBlank(loan.getUserbasicsinfo().getStaffNo())) {
				borrwer = loan.getUserbasicsinfo().getStaffNo();
			}
			updateLoansignToRedis(loan);
			RedisHelper.set(borrwerKey, borrwer);
			
			// 统计购买人数
			String investNumKey = "INT:HC9:LOANSIGN:INVEST:NUM:" + loanId;
			// 统计购买人数
			Object investNum = loanSignService.getTenderCount(loanId);
			
			// 查询众筹附件
			List attachList1 = null;
			List attachList2 = null;
			List attachList3 = null;
			List attachList4 = null;
			List attachList5 = null;
						
			String attachList1Key = "LST:HC9:LOANSIGN:ATTACH:LST:1:" + loanId;
			String attachList2Key = "LST:HC9:LOANSIGN:ATTACH:LST:2:" + loanId;
			String attachList3Key = "LST:HC9:LOANSIGN:ATTACH:LST:3:" + loanId;
			String attachList4Key = "LST:HC9:LOANSIGN:ATTACH:LST:4:" + loanId;
			String attachList5Key = "LST:HC9:LOANSIGN:ATTACH:LST:5:" + loanId;
						
			// 查询众筹附件
			attachList1 = loanSignService.getAttachMent(loanId, "1");// 项目证明
			attachList2 = loanSignService.getAttachMent(loanId, "2");// 资产包证明
			attachList3 = loanSignService.getAttachMent(loanId, "3");// 担保证明
			attachList4 = loanSignService.getAttachMent(loanId, "4");// 保障证明
			attachList5 = loanSignService.getAttachMent(loanId, "5");// 监管资金证明
			RedisHelper.set(investNumKey, "" + investNum);
			IndexDataCache.set(attachList1Key, attachList1);
			IndexDataCache.set(attachList2Key, attachList2);
			IndexDataCache.set(attachList3Key, attachList3);
			IndexDataCache.set(attachList4Key, attachList4);
			IndexDataCache.set(attachList5Key, attachList5);
			
			PageModel page = new PageModel();
			page.setPageNum(1);
			page.setNumPerPage(10);
			page = loanSignService.getLoanrecordList(Long.parseLong(loanId), page);
			
			String loanrecordListStr = JsonUtil.toJsonStr(page);
			String loanRecordkey = "LST:HC9:LOANSIGN:DETAIL:BUY:LIST:FIRST:PAGE:" + loanId;
			RedisHelper.set(loanRecordkey, loanrecordListStr);
			resultMap.put("msg", "更新成功！");
		} else {
			resultMap.put("msg", "所录入的标id在数据库中不存在！");
		}
		
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("更新标详情缓存信息至redis成功, 共花费 " + spendTime + " 秒！");
		
		appCacheService.updateAppLoanDetailCache(loanId);
		resultMap.put("code", "0");
		return resultMap;
	}
	
	/** 更新标详情页缓存至redis中 */
	public void updateLoansignToRedis(Loansign loansign) {
		/** 组装标的信息 */
		LoansignVo loansignVo = new LoansignVo();
		loansignVo.setId(loansign.getId());
		loansignVo.setName(loansign.getName());
		loansignVo.setIssueLoan(loansign.getIssueLoan());
		loansignVo.setRestMoney(loansign.getRestMoney());
		loansignVo.setPrioRate(loansign.getPrioRate());
		loansignVo.setPrioAwordRate(loansign.getPrioAwordRate());
		loansignVo.setPrioRestMoney(loansign.getPrioRestMoney());
		loansignVo.setMidRestMoney(loansign.getMidRestMoney());
		loansignVo.setAfterRestMoney(loansign.getAfterRestMoney());
		loansignVo.setLoanUnit(loansign.getLoanUnit());
		loansignVo.setPublishTime(loansign.getPublishTime());
		loansignVo.setCreditTime(loansign.getCreditTime());
		loansignVo.setStatus(loansign.getStatus());
		loansignVo.setType(loansign.getType());
		loansignVo.setRemonth(loansign.getRemonth());
		loansignVo.setValidity(loansign.getValidity());
		loansignVo.setActivityStatus(loansign.getActivityStatus());
		
		/** 更新项目基本信息 */
		updateLoansignbasicsVoToRedis(loansignVo, loansign);
		
		/** 项目类型 */
		LoansignTypeVo loansignTypeVo = new LoansignTypeVo();
		loansignTypeVo.setId(loansign.getLoansignType().getId());
		loansignVo.setLoansignType(loansignTypeVo);
		
		/** 更新还款记录信息 */
		updateLoansignVoDetailToRedis(loansignVo, loansign);
		
		WebCacheManagerUtil.setWebLoanSignDetailToRedis(loansignVo);
	}
	
	/** 组装项目基本信息 */
	private void updateLoansignbasicsVoToRedis(LoansignVo loansignVo, Loansign loansign) {
		Loansignbasics loansignbasics = loansign.getLoansignbasics();
		if(loansignbasics != null) {
			LoansignbasicsVo vo = new LoansignbasicsVo();
			vo.setRemark(loansignbasics.getRemark());
			vo.setBehoof(loansignbasics.getBehoof());
			vo.setHistory(loansignbasics.getHistory());
			vo.setRiskAdvice(loansignbasics.getRiskAdvice());
			loansignVo.setLoansignbasics(vo);
		}
	}
	
	/** 组装标详情信息 */
	private void updateLoansignVoDetailToRedis(LoansignVo loansignVo, Loansign loansign) {
		List<Repaymentrecord> repaymentrecords = loansign.getRepaymentrecords();
		if(repaymentrecords != null && repaymentrecords.size() > 0) {
			List<RepaymentrecordVo> repayList = new ArrayList<RepaymentrecordVo>();
			for(Repaymentrecord vo : repaymentrecords) {
				RepaymentrecordVo repayment = new RepaymentrecordVo();
				repayment.setRepayState(vo.getRepayState());
				repayment.setPeriods(vo.getPeriods());
				repayment.setRepayTime(vo.getRepayTime());
				repayList.add(repayment);
			}
			loansignVo.setRepaymentrecords(repayList);
		}
	}
	
	/** 更新所有标详细缓存信息 */
	public Map<String, String> updateAllLoanDetailRelCache() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("更新标详情缓存信息至redis开始！");
		long start = System.currentTimeMillis();
		
		List<Long> loanIdList = cacheManagerDao.queryAllLoanIdList();
		if(loanIdList.size() > 0) {
			for(Long loanId : loanIdList) {
				updateLoanDetailRelCache("" + loanId);
				appCacheService.updateAppLoanDetailCache("" + loanId);
			}
		}
			
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("更新标详情缓存信息至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** H5我要众持页面标列表更新 */
	public Map<String, String> updateH5ZhongChiPageLoanList() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("首页标列表相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		PageModel page = new PageModel();
		page.setPageNum(1);
		page.setNumPerPage(10);
		page = loanManageService.getLoanList(page);;
		SysCacheManagerUtil.setH5BuyPayLoanListCache(page);
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("首页标列表相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** H5热门推荐列表相关缓存更新 */
	public Map<String, String> updateH5HotIntroduceLoanList() {
		Map<String, String> resultMap = new HashMap<String, String>();
		LOG.error("H5热门推荐相关缓存更新至redis开始！");
		long start = System.currentTimeMillis();
		String key="LIST:HC9:INDEX:LOAN:RECOMMAND2";
		loanManageService.getLoanRecommandList(key);
		
		long spendTime = (System.currentTimeMillis() - start) / 1000;
		LOG.error("H5热门推荐相关缓存更新至redis成功, 共花费 " + spendTime + " 秒！");
		resultMap.put("code", "0");
		resultMap.put("msg", "更新成功！");
		return resultMap;
	}
	
	/** 新春猴给力活动 */
	public static ActivityMonkey generateActivityMonkey(String userId, String phone, String priority, int type, String loanId, 
			String loanName, String loanRecordId, String rewardMoney, int week) {
		Date date = new Date();
		String createTime = DateFormatUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
		
		ActivityMonkey activityMonkey = new ActivityMonkey();
		activityMonkey.setUserId(Long.parseLong(userId));
		activityMonkey.setMobilePhone(phone);
		activityMonkey.setMoney(Double.parseDouble(priority));
		activityMonkey.setType(type);
		activityMonkey.setLoanId(Long.parseLong(loanId));
		activityMonkey.setLoanName(loanName);
		activityMonkey.setLoanRecordId(Long.parseLong(loanRecordId));
		activityMonkey.setRewardMoney(Double.parseDouble(rewardMoney));
		activityMonkey.setCreateTime(createTime);
		activityMonkey.setWeek(week);
		activityMonkey.setStatus(0);
		activityMonkey.setExamineStatus(0);
		return activityMonkey;
	}
	
	public int week(){
		Date beginDate0 = DateFormatUtil.stringToDate("2016-01-18", "yyyy-MM-dd");
		Date beginDate1 = DateFormatUtil.stringToDate("2016-01-25", "yyyy-MM-dd");
		Date beginDate2 = DateFormatUtil.stringToDate("2016-02-01", "yyyy-MM-dd");
		Date beginDate3 = DateFormatUtil.stringToDate("2016-02-08", "yyyy-MM-dd");
		Date beginDate4 = DateFormatUtil.stringToDate("2016-02-15", "yyyy-MM-dd");
		Date beginDate5 = DateFormatUtil.stringToDate("2016-02-22", "yyyy-MM-dd");
		Date beginDate6 = DateFormatUtil.stringToDate("2016-02-29", "yyyy-MM-dd");
		
		Date currentDate = new Date();
		if(DateFormatUtil.isBefore(beginDate6, currentDate)){
			return 6;
		}else if(DateFormatUtil.isBefore(beginDate5, currentDate)){
			return 5;
		}else if(DateFormatUtil.isBefore(beginDate4, currentDate)){
			return 4;
		}else if(DateFormatUtil.isBefore(beginDate3, currentDate)){
			return 3;
		}else if(DateFormatUtil.isBefore(beginDate2, currentDate)){
			return 2;
		}else if(DateFormatUtil.isBefore(beginDate1, currentDate)){
			return 1;
		}else if(DateFormatUtil.isBefore(beginDate0, currentDate)){
			return 0;
		}else{
			return -1;
		}
	}
}
