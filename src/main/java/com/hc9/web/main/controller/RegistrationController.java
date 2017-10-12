package com.hc9.web.main.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hc9.commons.normal.Md5Util;
import com.hc9.web.main.entity.CardImgAudit;
import com.hc9.web.main.entity.ChannelSpreadDetail;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Userrelationinfo;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.redis.activity.year2016.month03.HcPeachActivitiCache;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.CardImgAuditService;
import com.hc9.web.main.service.RegistrationService;
import com.hc9.web.main.service.UserbasicsinfoService;
import com.hc9.web.main.service.DSP.DspService;
import com.hc9.web.main.service.baofo.BaoFuLoansignService;
import com.hc9.web.main.util.CSRFTokenManager;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.GenerateLinkUtils;
import com.hc9.web.main.util.GetIpAddress;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoginRelVo;

/** 注册 */
@Controller
@RequestMapping("/registration")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RegistrationController {

	@Resource
	private RegistrationService registrationService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private BaoFuLoansignService baoFuLoansignService;
	
	@Resource
	private DspService dspService;
	
	@Resource
	private CacheManagerService cacheManagerService;
	
	@Resource
	private CardImgAuditService cardImgAuditService;

	/** 验证推荐人是否存在 */
	@RequestMapping("/checkOnly_referrer")
	@ResponseBody
	public String checkOnlyReferrer(String fieldValue) {
		// 验证是否唯一
		String name = registrationService.checkReferrer(fieldValue);
		return name;
	}

	/** 注册验证用户名唯一性 */
	@RequestMapping("/regCheckOnly_username")
	@ResponseBody
	public List regCheckOnlyUserName(String fieldId, String fieldValue) {

		List list = new ArrayList();
		list.add(fieldId);
		// 验证是否唯一
		boolean bool = registrationService.regCheckUserName(fieldValue);
		list.add(bool);
		return list;
	}

	/** 登录验证用户名唯一性 */
	@RequestMapping("/checkOnly_username")
	@ResponseBody
	public List checkOnlyUserName(String fieldId, String fieldValue) {

		List list = new ArrayList();
		list.add(fieldId);
		// 验证是否唯一
		boolean bool = registrationService.checkUserName(fieldValue);
		list.add(bool);
		return list;
	}

	/** 验证邮箱是否唯一 */
	@RequestMapping("/checkOnly_email")
	@ResponseBody
	public List checkOnlyEmail(String fieldId, String fieldValue) {
		List list = new ArrayList();
		list.add(null);
		// 验证邮箱是否唯一
		boolean bool = registrationService.checkEmail(fieldId, fieldValue);
		list.add(bool);
		return list;
	}

	/**
	 * 验证手机是否唯一
	 * 
	 * @param fieldId
	 *            验证id
	 * @param fieldValue
	 *            验证内容
	 * @return 结果集
	 */
	@RequestMapping("/checkOnly_phone")
	@ResponseBody
	public List checkOnlyPhone(String fieldId, String fieldValue) {
		List list = new ArrayList();
		list.add(null);
		// 验证邮箱是否唯一
		boolean bool = registrationService.checkPhone(fieldId, fieldValue);
		list.add(bool);
		return list;
	}

	@RequestMapping("/checkValiCode")
	@ResponseBody
	public String checkValiCode(HttpServletRequest request, String fieldValue) {
		Object code = request.getSession().getAttribute("regCode");
		if (code == null) {
			return "0"; // 表示还没获取验证码
		} else {
			if (fieldValue.equals(code.toString())) {
				return "1"; // 表示验证码匹配
			} else {
				return "-1"; // 表示输入的验证码与其不匹配
			}
		}
	}
	
	/**
	 * 检查图形验证码
	 * @param request
	 * @param imgcode
	 * @return
	 */
	@RequestMapping("/checkImageCode")
	@ResponseBody
	public String checkImageCode(HttpServletRequest request, String imgcode) {
		// 取验证码
		String regImgCode = (String) request.getSession().getAttribute("user_login");
		//判断验证码是否正确
		if (regImgCode != null && regImgCode.equalsIgnoreCase(imgcode)) {
			return "1"; // 表示验证码匹配
		}else{
			return "-1"; // 表示还没获取验证码
		}
	}
	/**
	 * 登陆邮箱检测是否激活
	 * 
	 * @param fieldId
	 *            验证id
	 * @param fieldValue
	 *            验证内容
	 * @return 结果集
	 */
	@RequestMapping("/checkLogin_email")
	@ResponseBody
	public List checkLoginEmail(String fieldId, String fieldValue) {
		List list = new ArrayList();
		list.add(fieldId);
		// 验证邮箱是否唯一
		boolean bool = registrationService.checkLoginEmail(fieldValue);
		list.add(bool);

		return list;
	}

	@RequestMapping("/checkLogin_emailx")
	@ResponseBody
	public List checkLoginEmailx(String fieldId, String fieldValue) {
		List list = new ArrayList();
		list.add(fieldId);
		// 验证邮箱是否唯一
		boolean bool = registrationService.checkLoginEmailx(fieldValue);
		list.add(bool);

		return list;
	}

	/**
	 * 用户注册
	 * 
	 * @param userName
	 *            用户名
	 * @param email
	 *            用户邮箱
	 * @param pwd
	 *            用户登录密码
	 * @param captcha
	 *            验证码
	 * @param number
	 *            会员编号
	 * @param recommend
	 *            推荐人
	 * @param request
	 *            请求
	 * @param response
	 *            相应
	 * @return 成功true 失败false
	 * 2015-11-20修改过程：当注册成功后，取cookie数据，如果有数据，就记录到推广渠道详情表；当注册失败，取cookie数据，记录到推广渠道详情表
	 */
	@RequestMapping(value="register.htm",method = RequestMethod.POST)
	@ResponseBody
	public String registrationMethod(@RequestParam String CSRFToken,String userName, String phone1,
			String pwd, String pcode, String number,String imgcode,
			HttpServletRequest request, HttpServletResponse response) {
		// 跳转路径
		String num = "0";
		if (!userName.matches("[A-Za-z_0-9]{6,18}")) {
			return "11";  // 用户名不正确
		}
		if(request.getSession().getAttribute(CSRFTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME)==null){
			return "3";
		}
		if(CSRFToken == null ||!CSRFToken.equals(request.getSession().getAttribute(CSRFTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME).toString())){
			return "3";
		}
		boolean hasUser=!registrationService.checkUserName(userName);	//注册再次校验用户名唯一
		if(hasUser){      //用户名唯一的可以注册
			return "2";
		}


		// 取图形验证码
		String regImgCode = (String) request.getSession().getAttribute("user_login");
		// 获取手机号
		String regPhone = (String) request.getSession().getAttribute("regPhone");
		if(regPhone == null || !phone1.equals(regPhone)){
			num = "10"; //手机号码不对
		}else 
		// 判断图形验证码是否正确
		if (regImgCode != null && regImgCode.equalsIgnoreCase(imgcode)) {
			try {
				// 取手机验证码
				String validate = (String) request.getSession().getAttribute("regCode");
				// 判断手机验证码是否正确
				if (validate != null && validate.equalsIgnoreCase(pcode)) {
					// 调用推广链接
					Userbasicsinfo promoter = (Userbasicsinfo) request.getSession().getAttribute("generuser");
					// MD5加密密码
					pwd = Md5Util.execute(pwd);
					// 调用注册方法
					Userbasicsinfo isToPromoter = registrationService.registrationSave(userName, phone1, pwd, number, promoter, 1 , request);
					cacheManagerService.updateRegisterRelCache(userName, phone1);
					// 注册成功
					if (isToPromoter != null) {
						num = "1";
						// 添加推广渠道记录状态
						//取cookie中的渠道ID
						String value=dspService.getValueFromCookie(request);
						Map<String,String> map=null;
						String channelId=null;
						if(value!=null){
							map=dspService.getSpecialValue(value, "@_@");
							channelId=map.get("spreadId");
							addChannelSpreadDetail(userName.trim(),channelId,value,1,request.getSession().getId());
							dspService.channelSwitch(request,com.hc9.web.main.util.Constant.USER_REG,new String[]{String.valueOf(isToPromoter.getId())});
						}
						LOG.info(isToPromoter.getUserName() + "新用户注册！。。。");// 日志记录注册用户
					}
				} else {
					num = "4"; // 验证码有误
				}
			} catch (Exception e) {
				num = "3";
				//TODO保存 失败记录
				// 添加推广渠道记录状态
				//取cookie中的渠道ID
				String value=dspService.getValueFromCookie(request);
				Map<String,String> map=null;
				String channelId=null;
				if(value!=null){
					map=dspService.getSpecialValue(value, "@_@");
					channelId=map.get("spreadId");
					addChannelSpreadDetail(userName.trim(),channelId,value,-1,request.getSession().getId());

				}
				LOG.error("注册出现错误" + e.getMessage());
			}
			request.getSession().removeAttribute("user_login");
		}else{
			num="8";//图片验证码错误
		}
		
		// 注册成功后跳转到安全中心
		return num;
	}
	//TODO 保存ChannelSpreadDetail
	private void addChannelSpreadDetail(String userName,String channelId,String value,int status,String sessionId){
		ChannelSpreadDetail csd = new ChannelSpreadDetail();
		csd.setRegUserName(userName.trim());
		csd.setSessionId(sessionId);
		csd.setSpreadId(channelId);
		csd.setCookieValue(value);
		csd.setRegStatus(status);
		// 保存
		registrationService.saveSpreadDetail(csd);
	}
	/**
	 * 注册宝付
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/registBaofoo.htm")
	@ResponseBody
	public String registBaoFoo(HttpServletRequest request,String cardId,String name,Integer realAuth) {
		// 得到当前用户信息
		Userbasicsinfo userbasics = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasics
				.getId());
		String reState = ipsRegister(request, user.getId().toString(), "0", "0",cardId,name);
		return reState;
	}
	/**
	 * 中奖纪录实名认证（注册宝付）
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/registRealBaofoo.htm")
	@ResponseBody
	public String registRealBaofoo(HttpServletRequest request,String cardId,String name,String cardImg) {
		// 得到当前用户信息
		Userbasicsinfo userbasics = (Userbasicsinfo) request.getSession()
				.getAttribute(Constant.SESSION_USER);
		Userbasicsinfo user = userbasicsinfoService.queryUserById(userbasics
				.getId());
		String reState = "10";
		try {
			if(!StringUtil.isBlank(cardImg)){
				Date date = new Date();
				String createTime = DateFormatUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
				Userrelationinfo userrelationinfo = user.getUserrelationinfo();
				user.setUserrelationinfo(userrelationinfo);
				
				CardImgAudit cardImgAuditState = cardImgAuditService.getCardImgAuditByState(user.getId(),"0");
				if (cardImgAuditState != null) {
					return "12";  // 正在审核状态不能反复提交
				} else {
					CardImgAudit cardImgAudit = cardImgAuditService.getCardImgAudit(user.getId());
					if (cardImgAudit != null) {  // 修改审核记录
						cardImgAudit.setCardImg(cardImg);
						cardImgAudit.setCardImgTime(createTime);
						cardImgAudit.setCardImgState(0);
						cardImgAudit.setCardImgRemark(null);
						cardImgAudit.setCardImgAudit(null);
						cardImgAudit.setCardImgAuditTime(null);
						cardImgAuditService.updateCardImgAudit(cardImgAudit);
						HcPeachActivitiCache.setUserAuditRemind(user.getId(),2,"1");
					} else {  // 新增审核记录
						cardImgAudit = new CardImgAudit();
						cardImgAudit.setUserbasicsinfo(user);
						cardImgAudit.setCardImg(cardImg);
						cardImgAudit.setCardImgState(0);
						cardImgAudit.setCardImgTime(createTime);
						cardImgAuditService.save(cardImgAudit);
					}
					// 修改身份证图片路径
					userbasicsinfoService.updateCardImg(user, cardImg);
					Integer isAuthIps = user.getIsAuthIps();
					if (isAuthIps == null) {
						isAuthIps = 0;
					}
					if (isAuthIps == 1) {
						reState = "11";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
		if (StringUtil.isBlank(user.getpMerBillNo())) {
			reState = ipsRegister(request, user.getId().toString(), "0", "0",cardId,name);
		}
		return reState;
	}

	/**
	 * 会员登录
	 * @param userName 用户名（邮箱）
	 * @param pwd 密码
	 * @param captcha 验证码
	 * @param request 请求
	 * @return 视图
	 * @throws ParseException  异常
	 */
	@RequestMapping("/login")
	@ResponseBody
	public String loginMthod(String userName, String pwd, String captcha,boolean remember_user,
			HttpServletRequest request,HttpServletResponse response) throws ParseException {
		userName = userName.trim();
		pwd = pwd.trim();
		captcha = captcha.trim();
		String msg = "";
		// 错误次数
		Integer error = 0;
		// 取验证码
		String validate = (String) request.getSession().getAttribute("user_login");
		// 判断验证码是否正确
		if (validate != null && validate.equalsIgnoreCase(captcha)) {

			// 验证登录是否成功
			Userbasicsinfo user = registrationService.loginMethod(userName, pwd);
			// 如果用户名、密码匹配
			if (user != null) {
				request.getSession().removeAttribute("errorPwd");
				
				// 判断该会员是否被后台管理员禁用
				LoginRelVo loginRelVo = SysCacheManagerUtil.getLoginRelVoByLoginNo(userName);
				boolean isLock = false;
				if(loginRelVo != null) {
					int lock = loginRelVo.getIsLock();
					if(lock == 1) {
						isLock = true;
					}
				} else {
					isLock = registrationService.isLock(user);
				}
				// 如果该会员未被管理员禁用
				if (!isLock) {
					// 判断锁定时间是否已过
					boolean b = SysCacheManagerUtil.isLockTimeOut(userName);
					// 如果已过
					if (b) {
						// 获取ip
						String ip = GetIpAddress.getIp(request);
						// 添加登录日志
						registrationService.saveUserLog(user, ip);
						request.getSession().setAttribute(Constant.SESSION_USER, user);

						if (remember_user) {
							Cookie cok = new Cookie("userName", userName);
							cok.setMaxAge(30*24*60*60);
							cok.setPath("/");
							response.addCookie(cok);
						}
						
						String promoteNo = "";
						Userbasicsinfo usr = userbasicsinfoService.queryUserById(user.getId());
						int flag = usr.getUserType();
						if (flag == 2) {
							promoteNo = usr.getStaffNo();
							
						} else {
							promoteNo = usr.getId().toString();
						}
						request.getSession().setAttribute(
								"myPromoteLikn",
								GenerateLinkUtils.getServiceHostnew(request)
										+ "h5/h5regist.htm?member=" + promoteNo);
						msg = "success";
					} else {
						// 如果还未过，保存时间
						request.getSession().setAttribute("isLock", user.getFailTime());
						msg = "isLock";
					}
				} else {
					msg = "adminLock";// 该会员被管理员禁用
				}
			} else {
				request.getSession().removeAttribute("errorPwd");
				// 如果不匹配、密码错误，存在错误次数+1
				error = SysCacheManagerUtil.increaseLoginErrorNumToday(userName);
				if (error >= 5) {
					msg = "errorLock"; // 密码5次以上
				} else {
					// 保存错误次数
					msg = "errorPwd";
					request.getSession().setAttribute("errorPwd", error);
				}
			}
			request.getSession().removeAttribute("user_login");
		} else {
			// 验证码错误
			msg = "errorValidate";
		}
		return msg;
	}

	/**
	 * 验证邮箱激活链接的方法
	 * 
	 * @param activationid
	 *            激活邮箱链接的用户id
	 * @param request
	 * @return
	 */
	@RequestMapping("/activateAccount")
	public String activateAccount(Long activationid, HttpServletRequest request) {
		Integer identy = registrationService.activateAccount(activationid,
				request);
		String url = "/WEB-INF/views/failure";
		if (identy == 8) {

			url = "/WEB-INF/views/reg_success";
		}
		request.setAttribute("identy", identy);
		return url;
	}

	/***
	 * 绑定宝付账户 若无宝付账户，直接注册 若有先绑定验证码，获得验证码进行注册
	 * 
	 * @param request
	 * @param userId
	 * @param sendBindCode
	 * @param sendNum
	 *            0-无 1-有
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/ipsRegister")
	public String ipsRegister(HttpServletRequest request, String userId,
			String sendBindCode, String sendNum,String cardId,String name) {
		return baoFuLoansignService.ipsRegisterService(request, userId,
				sendBindCode, sendNum,cardId,name);
	}
	
}
