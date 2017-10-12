package com.hc9.web.main.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.baofo.BaoFuAccountService;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.vo.pay.ReturnInfo;
import com.hc9.web.main.vo.pay.crs;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** 宝付账号相关入口类 */
@Controller
@RequestMapping("/baofuaccount")
public class BaoFuAccountController {
	@Resource
	private BaoFuAccountService baoFuAccountService;
	
	/** 注册宝付校验身份证id是否已经实名认证 */
	@CheckLoginOnMethod
	@RequestMapping(value="/validCardId", method = RequestMethod.POST)
	@ResponseBody
	public String validCardId(HttpServletRequest request, String cardId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().
				getAttribute(Constant.SESSION_USER);
		if(user != null) {
			resultMap = baoFuAccountService.validCardId(cardId, user.getId());
		} else {
			resultMap.put("code", "-1");
			resultMap.put("msg", "尚未登录，请登录后再试!");
		}
		return JsonUtil.toJsonStr(resultMap);
	}
	
	/** 确定开通宝付页面-- 开通跳转页面  */
	@CheckLoginOnMethod
	@RequestMapping(value="/doOpenBaoFuAccount", method = RequestMethod.POST)
	public String doOpenBaoFuAccount(HttpServletRequest request, String userNname, String cardId) {
		try {
			// 取到登录用户sesssion
			Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute("session_user");
			if (user == null) {
				return "redirect:"+request.getSession().getServletContext().getAttribute("https")+"/h5/login.htm";
			}
			baoFuAccountService.doOpenBaoFuAccount(request, user.getId(), userNname, cardId);
			return "WEB-INF/views/hc9/account/openbaofuaccount";
		} catch(Exception e) {
			LOG.error("开通宝付过程中出现异常！", e);
			return "";
		}
	}
	
	/** 开通宝付账户 -- 宝付回调 */
	@RequestMapping("/asyncOpenBaoFuPage")
	public void asyncOpenBaoFuPage(HttpServletRequest request, ReturnInfo info) {
		LOG.error("页面接口开通宝付宝付回调所传信息为：" + JsonUtil.toJsonStr(info));
		if (info != null) {
			String result = info.getResult();
			String sign = info.getSign();
			String md5Sign = CommonUtil.MD5(result + "~|~" + ParameterIps.getMerchantKey());
			if(md5Sign.equals(sign)) {
				crs baoFuAccount = new crs();
				XStream xs = new XStream(new DomDriver());
				xs.alias(baoFuAccount.getClass().getSimpleName(), baoFuAccount.getClass());
				baoFuAccount = (crs) xs.fromXML(result);
				String code = baoFuAccount.getCode();
				if("CSD000".equals(code)) {
					String pMerBillNo = "" + baoFuAccount.getUser_id();
					LOG.error("宝付异步返回的宝付账号：" + pMerBillNo);
					baoFuAccountService.asyncOpenBaoFuPage(pMerBillNo);
				} else {
					LOG.error("页面接口开通宝付失败！");
				}
			} else {
				LOG.error("页面接口开通宝付，数字签名不通过！返回值：sign=" + sign + ",md5Sign=" + md5Sign);
			}
		} else {
			LOG.error("页面接口开通宝付，宝付所传参数为空！");
		}
	}
	
	/** 用户注册注册成功提示页面 */
	@RequestMapping("baofooSuccess")
	public String registration(ReturnInfo info, HttpServletRequest request) {
		return "WEB-INF/views/success";
	}
}