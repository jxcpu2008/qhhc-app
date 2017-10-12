package com.hc9.web.main.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.RechargesService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.baofo.BaoFuLoansignService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.vo.PageModel;

/** 在线充值 */
@Controller
@RequestMapping("recharge")
public class RechargesController {

	@Resource
	private RechargesService rechargesService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private BaoFuLoansignService baoFuLoansignService;

	/** 打开在线支付页面 */
	@CheckLoginOnMethod
	@RequestMapping("openRecharge.htm")
	public String openRecharge(HttpServletRequest request) {
		return "WEB-INF/views/hc9/member/trade/recharge";
	}
	
	/** 提交ips数据 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/toIpsSubmit.htm")
	public String toIpsSubmit(HttpServletRequest request) {
		Map<String, String> map = (Map<String, String>) request.getSession()
				.getAttribute("map");
		request.setAttribute("map", map);
		request.getSession().removeAttribute("map");
		return "WEB-INF/views/hc9/member/trade/central_news";
	}

	/**
	 * 打开在线充值记录查询页面
	 * 
	 * @param beginTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param request
	 *            request
	 * @return 返回页面地址
	 */
	@RequestMapping("rechargeRecord.htm")
	public String openRechargeRecord(HttpServletRequest request,
			String beginTime, String endTime, Integer search, Integer no) {
		// 获取当前登录用户的信息
		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		userbasic = userbasicsinfoService.queryUserById(userbasic.getId());
		PageModel page = new PageModel();
		page.setPageNum(no == null ? 1 : no);
		// 获取提现信息列表
		rechargesService.rechargeList(userbasic.getId(), beginTime, endTime,
				search, page);
		request.setAttribute("page", page);
		return "WEB-INF/views/hc9/member/trade/rechargeRecord";
	}

	/** 充值 */
	@CheckLoginOnMethod
	@ResponseBody
	@RequestMapping("ipsRecharge.htm")
	public String ipsRecharge(HttpServletRequest request, Double amount,
			String additional_info, String type) {
		additional_info = "";
		return baoFuLoansignService.ipsRechargeService(request, amount, additional_info);
	}

	/** 充值查询 */
	@CheckLoginOnMethod
	@RequestMapping("ipsRechargeNum.htm")
	public String ipsRechargeNum(HttpServletRequest request, String rId,
			Integer no) throws Exception {
		return baoFuLoansignService.ipsRechargeNumService(request, rId, no);
	}
}
