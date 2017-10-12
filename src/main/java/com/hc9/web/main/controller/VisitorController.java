package com.hc9.web.main.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.redis.sys.vo.ArticleVo;
import com.hc9.web.main.redis.sys.vo.BannerVo;
import com.hc9.web.main.redis.sys.vo.LoanDynamicVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;
import com.hc9.web.main.service.LoanSignService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.VisitorService;
import com.hc9.web.main.service.article.ColumnManageService;
import com.hc9.web.main.util.CSRFTokenManager;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoanRecommendVo;
import com.hc9.web.main.vo.LoanlistVo;
import com.hc9.web.main.vo.LoginRelVo;

/** 普通请求 */
@RequestMapping({ "visitor", "/" })
@Controller
public class VisitorController {

	@Resource
	private VisitorService visitorservice;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private LoanSignService loanSignService;
	
	@Resource
	private ColumnManageService columnServic;

	/**
	 * 跳转到登陆界面
	 * @return 登陆界面 */
	@RequestMapping("to-login")
	public String toLogin(HttpServletRequest request, String id, String name, String skip) {
		request.setAttribute("isLogin", id);
		request.setAttribute("userName", name);
		request.setAttribute("skip", skip);
		return "WEB-INF/views/hc9/login";
	}

	/**
	 * 跳转到支付完之后
	 * @return 支付完之后界面
	 */
	@RequestMapping("cardForword")
	public String cardForword() {
		return "WEB-INF/views/cardForword";
	}

	/**
	 * 跳转到注册界面
	 * 
	 * @return 注册界面
	 */
	@RequestMapping("to-regist")
	public String toRegist(HttpServletRequest request, String member, String rec) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		request.getSession().removeAttribute("tgId");
		if (user != null) {
			return "redirect:/member_index/member_center.htm?index=0_0";
		}
		if (member != null) {
			LOG.debug("推广注册");
			// 判断是否为推广连接
			if (StringUtil.isNotBlank(member)) {
				// 获取推广人编号
				Userbasicsinfo genuser = null;
				LoginRelVo loginRelVo = null;
				if (member.startsWith("0")) {// “0”开头，员工
					loginRelVo = SysCacheManagerUtil.getLoginRelVoByStaffNo(member);
					if(loginRelVo == null) {
						genuser = userbasicsinfoService.queryUserByStaffNo(member);
					}
				} else {
					loginRelVo = SysCacheManagerUtil.getLoginRelVoById(member);
					if(loginRelVo == null) {
						genuser = userbasicsinfoService.queryUserById(Long.valueOf(member));
					}
				}
				
				if(loginRelVo != null) {
					genuser = new Userbasicsinfo();
					genuser.setId(loginRelVo.getId());
				}

				if (genuser != null) {
					// 将推广人保存到session中
					request.getSession().setAttribute("generuser", genuser);
				}
				request.getSession().setAttribute("tgId", member);
			}
		}
		if (rec != null) {
			if (StringUtil.isNotBlank(rec) && rec.trim().length() > 3) {
				String shopRecordId = "";
				if ((rec.indexOf("+") == -1 || rec.indexOf("/") == -1)
						&& rec.indexOf("=") == -1) {
					shopRecordId = StringUtil.correctPassword(rec);
				} else {
					shopRecordId = StringUtil.correctPassword(rec);
				}
				shopRecordId = shopRecordId.substring(3);
				if (StringUtil.isNumberString(shopRecordId)) {
					// ShopRecord
					// record=projectService.getShopRecord(Long.parseLong(shopRecordId));
					// if (record != null) {
					request.getSession().setAttribute("record", shopRecordId);
					// }
				}
			}
		}
		Object phone = request.getAttribute("phone");
		request.getSession().setAttribute("csrf",
				CSRFTokenManager.getTokenForSession(request.getSession()));
		// 快速注册
		String refPhone = request.getParameter("phone");
		if (!StringUtil.isBlank(refPhone)) {
			phone = refPhone;
		}
		if (phone != null) {
			request.setAttribute("phone", phone);
		}
		return "WEB-INF/views/hc9/regist";
	}

	/**
	 * 显示首页
	 * @param request 
	 * @return 返回首页路径
	 */
	@RequestMapping({ "/index.htm", "/" })
	public String indexShow(HttpServletRequest request) {
		return initIndex(request);
	}

	/** 初始化首页数据 */
	@SuppressWarnings("rawtypes")
	public String initIndex(HttpServletRequest request) {
		//banner
		List<BannerVo> banners = WebCacheManagerUtil.getWebBannerListFromRedis();
		if(banners == null || banners.size() < 1){
			visitorservice.query();
			banners = WebCacheManagerUtil.getWebBannerListFromRedis();
		}
		request.setAttribute("application_banner", banners);
		
		
		//累计投资INT:HC9:INDEX:INVEST:TOTAL:NUMS
		String key="INT:HC9:INDEX:INVEST:TOTAL:NUMS";
		String totalInvest = RedisHelper.get(key);
		if(null==totalInvest || "null".equals(totalInvest)){
			totalInvest = loanSignService.gettotalInvestment(key);
		}
		request.setAttribute("totalInvest", totalInvest);
		
		//注册用户数量
		key="INT:HC9:USR:REGISTER:TOTAL:NUMS";
		String currentRegUsers = RedisHelper.get(key);
		if(null==currentRegUsers || "null".equals(totalInvest)){
			currentRegUsers=userbasicsinfoService.getcurrentRegUsers(key);
		}
		request.setAttribute("currentRegUsers", currentRegUsers);
		
		//文章
		List<ArticleVo> articles = WebCacheManagerUtil.getWebArticleListFromRedis();
		if(articles == null || articles.size() < 1){
			columnServic.getArticleList();
			articles = WebCacheManagerUtil.getWebArticleListFromRedis();
		}
		request.setAttribute("artList01", articles);
		
		//项目动态
		List<LoanDynamicVo> loandynamics = WebCacheManagerUtil.getWebLoanDynamicVoListFromRedis();
		if(loandynamics == null || loandynamics.size() < 1){
			loanSignService.getLoanLoandynamic();
			loandynamics = WebCacheManagerUtil.getWebLoanDynamicVoListFromRedis();
		}
		request.setAttribute("dynamic", loandynamics);
		
		//热门推荐
		List<LoanRecommendVo> recommendVos = WebCacheManagerUtil.getWebRecommandLoanListFromRedis();
		if(recommendVos.size() < 1){
			recommendVos = loanSignService.getRecommand();
		}
		request.setAttribute("recommand", recommendVos);


		// 首页标列表
		List<LoanlistVo> loanlist = WebCacheManagerUtil.getWebIndexLoanListListFromRedis();
		if(loanlist.size() < 1){
			loanlist = loanSignService.updateLoanlist();
		}
		request.setAttribute("loanlist", loanlist);
		
		return "WEB-INF/views/hc9/index";
	}


	/**
	 * 验证用户名是否重复
	 * @param fieldId 文本框id
	 * @param fieldValue 文本框值
	 * @return List<Object>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("/checkOnlyUsername")
	@ResponseBody
	public List<Object> checkOnlyUsername(String fieldId, String fieldValue) {
		List list = new ArrayList();
		boolean flag = visitorservice.checkUserName(fieldValue);
		list.add(fieldId);
		list.add(flag);
		return list;
	}

	/** 验证用户名是否重复
	 * @param fieldId 文本框id
	 * @param fieldValue 文本框值
	 * @return List<Object>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/checkOnlyEmail")
	@ResponseBody
	public List<Object> checkOnlyEmail(String fieldId, String fieldValue) {
		List list = new ArrayList();
		boolean flag = visitorservice.checkUserEmail(fieldValue);
		list.add(fieldId);
		list.add(flag);
		return list;
	}

	/**
	 * 验证验证码是否正确
	 * @param mes_code 输入的验证码
	 * @param request 
	 * @return boolean
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	@RequestMapping("/checkValideCode")
	@ResponseBody
	public boolean checkValideCode(String mes_code, HttpServletRequest request) {
		List list = new ArrayList();
		String valideCode = request.getSession().getAttribute("user_login").toString();
		if (valideCode.equalsIgnoreCase(mes_code)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 验证验证码
	 * @param request 
	 * @param name 名字
	 * @param value 值
	 * @return boolean
	 */
	@SuppressWarnings("unused")
	private boolean checkCode(HttpServletRequest request, String name, String value) {
		Object obj = request.getSession().getAttribute(name);
		if (obj == null || value == null || !value.equalsIgnoreCase((String) obj)) {// 校验验证码
			request.setAttribute(Constant.ATTRIBUTE_MSG, "验证码错误！");
			return false;
		}
		request.getSession().removeAttribute(name);
		return true;
	}

	/** 获取服务协议 */
	@RequestMapping("/getAgreeMent.htm")
	@ResponseBody
	public Object getAgreeMent(HttpServletRequest requset) {
		Object object = visitorservice.getAgreeMent();
		return object;
	}

	/** 获取支付协议 */
	@RequestMapping("/getPayProtocol.htm")
	@ResponseBody
	public Object getPayProtocol(HttpServletRequest request) {
		Object obj = visitorservice.getPayProtocol();
		return obj;
	}

	/** 查询合同协议 */
	@RequestMapping("/getContant.htm")
	@ResponseBody
	public Object getContant(HttpServletRequest requset) {
		Object object = visitorservice.getContant();
		return object;
	}

	/** 委托书 */
	@RequestMapping("/getDelegate.htm")
	public String getDelegate(HttpServletRequest requset) {
		return "WEB-INF/views/hc9/protocol/delegate";
	}

	/** 首页安全保障 */
	@RequestMapping("/toSafeSecurity.htm")
	public String toSafeSecurity(HttpServletRequest request) {
		return "WEB-INF/views/hc9/safeSecurity";
	}

	/** 新手指引  */
	@RequestMapping("/toUserGuide.htm")
	public String toUserGuide(HttpServletRequest request) {
		return "WEB-INF/views/hc9/comerGuide";
	}
	
	/**
	 * APP下载页
	 * */
	@RequestMapping("/appDownloadExplain.htm")
	public String appDownload(HttpServletRequest request) {
		return "WEB-INF/views/hc9/activity/appDownload";
	}
	
	@RequestMapping("/registPro.htm")
	public String registPro() {
		return "/WEB-INF/views/hc9/regist_pro";
	}
}
