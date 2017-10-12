package com.hc9.web.main.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.web.main.common.annotation.CheckLoginOnMethod;
import com.hc9.web.main.entity.Banktype;
import com.hc9.web.main.entity.UserBank;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.service.UserBankService;
import com.hc9.web.main.service.baofo.BaoFuLoansignService;
import com.hc9.web.main.service.baofo.BaoFuService;
import com.hc9.web.main.util.Constant;

/** 用户银行卡管理 */
@Controller
@RequestMapping("userBank")
public class UserBankController {
	@Resource
	private UserBankService userBankService;

	@Resource
	private BaoFuService baoFuService;

	@Resource
	private BaoFuLoansignService baoFuLoansignService;

	/** 打开绑定银行卡页面 */
	@CheckLoginOnMethod
	@RequestMapping("/toBindBank.htm")
	public String toBindBank(HttpServletRequest request) {
		List<Banktype> bankType = userBankService.getBankType();
		Userbasicsinfo user = (Userbasicsinfo) request.getSession().getAttribute(Constant.SESSION_USER);
		List<UserBank> userbanks = userBankService.getUserBankList(user.getId().toString());
		request.setAttribute("userbanks", userbanks);
		request.setAttribute("bankType", bankType);
		return "WEB-INF/views/hc9/member/bindBank";
	}

	/** 查询银行卡信息 */
	@CheckLoginOnMethod
	@ResponseBody
	@RequestMapping("/synchronizeUserBank.htm")
	public String synchronizeUserBank(HttpServletRequest request, Long userId) {
		Userbasicsinfo user = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		return baoFuService.getUserBank(user.getId());
	}

	/** 前端添加银行卡 type =0 删除 +id type =1 新增 +userbank */
	@RequestMapping("ipsOpBankCard.htm")
	@CheckLoginOnMethod
	@ResponseBody
	public synchronized String ipsOpBankCard(HttpServletRequest request,
			String type, UserBank userBank, String id, String validateCode) {
		if (type == "1" || type.equals("1")) {
			// 取验证码
			String validate = (String) request.getSession().getAttribute(
					"user_login");
			if (validate.equalsIgnoreCase(validateCode)) {
				return baoFuLoansignService.ipsOpBankCardService(request, type,
						userBank, id, validateCode);
			} else {
				return "5"; // 表示验证码错误
			}
		} else {
			return baoFuLoansignService.ipsOpBankCardService(request, type,
					userBank, id, validateCode);
		}
	}
}