package com.hc9.web.main.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.Generalize;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.GeneralizeService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.GenerateLinkUtils;
import com.hc9.web.main.vo.PageModel;

/** 会员推广控制层 */
@Controller
@RequestMapping("/generalize")
public class GeneralizeController {

	/** 注入后台会员服务层 */
	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	/** 会员推广信息服务层 */
	@Resource
	private GeneralizeService generaLizeService;

	/** 生成当前登录会员的推广链接 */
	@CheckLoginOnMethod
	@RequestMapping("/get_promote_links")
	public String getPromoteLinks(HttpServletRequest request) {

		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		userbasic = userbasicsinfoService.queryUserById(userbasic.getId());

		String promoteNo = "";
		int flag = userbasic.getUserType();
		if (flag == 1) {
			promoteNo = userbasic.getId().toString();
		} else if (flag == 2) {
			promoteNo = userbasic.getStaffNo();
		}

		request.getSession().setAttribute(
				"promoteLikn",
				GenerateLinkUtils.getServiceHostnew(request)
						+ "visitor/to-regist?member=" + promoteNo);
		request.setAttribute("user", userbasic);
		request.setAttribute("promoteNo", promoteNo);
		return "/WEB-INF/views/member/generalize";
	}

	@CheckLoginOnMethod
	@RequestMapping("/retrieveReferrer")
	@ResponseBody
	public String retrieveReferrer(HttpServletRequest request, String realName,
			String phone, String identity) {
		Userbasicsinfo userbasic = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		if (realName.equals(userbasic.getName())
				&& phone.equals(userbasic.getUserrelationinfo().getPhone())
				&& identity.equals(userbasic.getUserrelationinfo().getCardId())) {
			return "self";
		}
		Userbasicsinfo byReferrer = userbasicsinfoService.checkUserIsExist(realName,
				phone, identity);
		if (byReferrer != null) {
			if (byReferrer.getUserType() == 2) { 
				return "employee";
			}
			if(userbasic.getUserType()==1){  
				if (byReferrer.getUserType() == 4) { 
					return "brokerage";
				}
			}
			if (generaLizeService.getGeneralize(byReferrer.getId())) {
				return "exist";
			}
			Generalize successGen = generaLizeService.getGeneralizeByState(byReferrer.getId(), userbasic.getId() , "0,1,2");
			if (successGen != null) {
				return "mutual";  
			}
			Generalize gen = generaLizeService.getGeneralizeByState(userbasic.getId(),byReferrer.getId(),"3");
			if (gen != null) {  
				gen.setAdddate(DateUtils.format("yyyy-MM-dd"));
				gen.setUanme(userbasic.getName());
				gen.setGenuid(userbasic.getId());
				gen.setByUser(byReferrer);
				gen.setRemark("");
				gen.setState(0);  
				generaLizeService.updateGeneralized(gen);
			} else {    
				gen = new Generalize();
				gen.setAdddate(DateUtils.format("yyyy-MM-dd"));
				gen.setUanme(userbasic.getName());
				gen.setGenuid(userbasic.getId());
				gen.setByUser(byReferrer);
				gen.setState(0); 
				generaLizeService.addGeneralized(gen);
			}
			return "success";
		} else {
			return "fail";
		}
	}

	/** 查询当前登录人的推广信息 */
	@RequestMapping("/promote_record.htm")
	public String queryGenlizePage(HttpServletRequest request, PageModel page,
			Integer no) {
		if (no != null && !"".equals(no)) {
			page.setPageNum(no);
		} else {
			page.setPageNum(1);
		}
		// 获取当前登录人
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		user = userbasicsinfoService.queryUserById(user.getId());
		// 判断当前是否有人登录
		if (null != user) {
			// 查询推广信息
			page.setList(generaLizeService.queryGenlizePage(user.getId() + "",
					page));
		}
		// 保存分页信息
		request.setAttribute("page", page);
		return "/WEB-INF/views/hc9/member/loan/promoteRecord";
	}

	/** 会员推广奖金查询 */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/genmoney_list")
	public String queryGmoneyPage(HttpServletRequest request, PageModel page) {

		// 获取当前登录人
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);

		List generlist = null;

		// 判断当前是否有人登录
		if (null != user) {
			// 查询推广信息
			generlist = generaLizeService.querygenMoenyPage(user.getId() + "",
					page);
		}

		// 保存查询结果
		request.setAttribute("gmoney", generlist);
		// 保存分页信息
		request.setAttribute("page", page);

		// 会员推广奖金
		request.setAttribute("gen", "gmoney");

		return "/WEB-INF/views/member/generalizelist";
	}

	/**
	 * 推广奖励记录
	 */
	@CheckLoginOnMethod
	@RequestMapping("/getPromoteReward.htm")
	public String getCommissionRecord(HttpServletRequest request,
			PageModel page, String beginTime, String endTime,
			Integer no, String search) {
		// 获取当前用户
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		if (no != null) {
			page.setPageNum(no);
		} else {
			page.setPageNum(1);
		}
		String userId = user.getId().toString();
		page = generaLizeService.queryPromoteReward(userId,
				page, beginTime, endTime, search);
		//待收佣金
		Double collectMoney=generaLizeService.getPromoteReward(userId, beginTime, endTime, search, 0);
		//已收佣金
		Double receivedMoney=generaLizeService.getPromoteReward(userId, beginTime, endTime, search, 1);
		Object obj = generaLizeService.getPromoteMoneyCount(userId, beginTime, endTime, search);
		request.setAttribute("page", page);
		request.setAttribute("collectMoney", collectMoney);
		request.setAttribute("receivedMoney", receivedMoney);
		request.setAttribute("recommCollect", obj);
		return "/WEB-INF/views/hc9/member/loan/rewardInfo";
	}
}