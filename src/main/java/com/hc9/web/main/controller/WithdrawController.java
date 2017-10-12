package com.hc9.web.main.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.WithdrawCard;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.WithdrawServices;
import com.hc9.web.main.service.baofo.BaoFuLoansignService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.vo.PageModel;

/** 用户提现操作 */
@Controller
@RequestMapping("/withdraw")
public class WithdrawController {

	@Resource
	private HibernateSupport dao;

	/** 提现sercices **/
	@Resource
	private WithdrawServices withdrawServices;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private BaoFuLoansignService baoFuLoansignService;

	/** 打开提现页面 */
	@CheckLoginOnMethod
	@RequestMapping("openWithdrawCash.htm")
	public String openWithdrawCash(HttpServletRequest request) {
		Userbasicsinfo user = ((Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER));
		if (user != null) {
			WithdrawCard withdrawCard = withdrawServices.queryLatestWithdrawCard(user.getId());
			request.setAttribute("withdrawCard", withdrawCard);
			
		}
		return "WEB-INF/views/hc9/member/trade/withdraw";
	}
	
	/** 提现平台收取手续费 */
	@CheckLoginOnMethod
	@ResponseBody
	@RequestMapping("getWithdrawFee.htm")
	public String getWithdrawFee(HttpServletRequest request,Double money){
		Userbasicsinfo user = ((Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER));
		double result=withdrawServices.takeWithdrawFee(user, money);		
		return String.valueOf(result);
	}

	/**
	 * 查询用户当前的所有的提现信息
	 * @return 返回提现记录页面
	 */
	@RequestMapping("/withdrawRecoed.htm")
	public String withdrawRecoed(HttpServletRequest request, String beginTime,
			String endTime, Integer search, Integer no) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Long user_id = user.getId();
		user = userbasicsinfoService.queryUserById(user_id);
		PageModel page = new PageModel();
		page.setPageNum(no == null ? 1 : no);
		// 获取提现记录
		withdrawServices.withdrawList(user_id, beginTime, endTime, search, page);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/trade/withdrawRecord";
	}

	/** 提现 */
	@CheckLoginOnMethod
	@ResponseBody
	@RequestMapping("ipsWithdraw.htm")
	public String ipsWithdraw(HttpServletRequest request, Double money,String type,Long withdrawCardId) {
		return baoFuLoansignService.ipsWithdrawService(request, money, withdrawCardId);
	}
	
	/** 提现业务查询 前端调用 */
	@CheckLoginOnMethod
	@RequestMapping("ipsWithdrawNum.htm")
	@ResponseBody
	public String ipsWithdrawNum(HttpServletRequest request, String wId) {
		return baoFuLoansignService.returnWithdrawNumService(request, wId);
	}
}