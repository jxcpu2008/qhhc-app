package com.hc9.web.main.service.activity.year2016;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2016.month05.HcJuChengCache;
import com.hc9.web.main.redis.activity.year2016.month05.PrizeVo;
import com.hc9.web.main.service.MemberCenterService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.StatisticsUtil;
import com.hc9.web.main.util.StringUtil;

/** 聚橙网活动对应服务类 */
@Service
public class Month05JuChengService {
	@Resource
	private HibernateSupport dao;
	
	@Resource
	private MemberCenterService memberCenterService;
	
	@Resource
	private UserbasicsinfoService userbasicsinfoService;
	
	/** 聚橙网活动查询资金余额信息 */
	public void queryJuChengUserMoneyInfo(HttpServletRequest request) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		if(user != null) {
			long userId = user.getId();
			user = userbasicsinfoService.queryUserById(userId);
			/** 账户余额 */
			double cashBalance = user.getUserfundinfo().getCashBalance();
			/** 累计投金额 */
			double totalInvestMoney = memberCenterService.queryTotalInvestMoneyOfUser(userId);
			/** 总收益 */
			double totalIncome = memberCenterService.hostIncome(userId);
			/** 门票张数 */
			int ticketNum = queryJuChengTicketNum(userId);
			request.setAttribute("cashBalance", cashBalance);
			request.setAttribute("totalInvestMoney", totalInvestMoney);
			request.setAttribute("totalIncome", totalIncome);
			request.setAttribute("ticketNum", ticketNum);
		}
		/** 获奖列表 */
		List<PrizeVo> prizeList = queryJuChengPrizeList();
		
		/** 剩余门票张数 */
		int leftTicketNum = HcJuChengCache.getLeftTicketNum();
		request.setAttribute("prizeList", prizeList);
		request.setAttribute("leftTicketNum", leftTicketNum);
	}
	
	/** 查询聚橙网活动奖品列表 */
	public List<PrizeVo> queryJuChengPrizeList() {
		List<PrizeVo> resultList = new ArrayList<PrizeVo>();
		String key = "STR:HC9:JU:CHENG:PRIZE:LIST";
		String json = RedisHelper.get(key);
		if(StringUtil.isNotBlank(json)) {
			resultList = JsonUtil.jsonToList(json, PrizeVo.class);
		} else {
			String sql = "select p.userId,r.phone,p.prizeNum from prizedetail p, userrelationinfo r "
					+ "where p.userId=r.user_id and p.prizeType=17";
			List list = dao.findBySql(sql);
			if(list != null && list.size() > 0) {
				for(Object obj : list) {
					Object[] arr = (Object[])obj;
					long userId = StatisticsUtil.getLongFromBigInteger(arr[0]);
					String phone = (String)arr[1];
					int prizeNum = (Integer)arr[2];
					
					PrizeVo vo = new PrizeVo();
					vo.setUserId(userId);
					vo.setPhone(phone);
					vo.setPrizeNum(prizeNum);
					
					resultList.add(vo);
				}
				json = JsonUtil.toJsonStr(resultList);
				RedisHelper.set(key, json);
			}
		}
		return resultList;
	}
	
	/** 查询用户聚橙网门票张数 */
	public int queryJuChengTicketNum(long userId) {
		int ticketNum = 0;
		String sql = "select prizeNum from prizedetail where userId=? and prizeType=17";
		Object obj = dao.findObjectBySql(sql, userId);
		if(obj != null) {
			ticketNum = Integer.valueOf(obj.toString());
		}
		return ticketNum;
	}
}
