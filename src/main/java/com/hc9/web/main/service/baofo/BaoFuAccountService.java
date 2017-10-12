package com.hc9.web.main.service.baofo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.ParameterIps;
import com.hc9.web.main.dao.UserDao;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.redis.sys.UserInfoCache;
import com.hc9.web.main.service.GeneralizeService;
import com.hc9.web.main.service.HccoindetailService;
import com.hc9.web.main.service.PayLogService;
import com.hc9.web.main.util.CommonUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.LOG;
import com.hc9.web.main.util.ParseXML;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.BaoFuAccountVo;

/** 宝付账户相关接口 */
@Service
public class BaoFuAccountService {
	@Resource
	private PayLogService payLogService;
	
	@Resource
	private UserDao userDao;
	
	@Resource
	private GeneralizeService generalizeService;
	
	@Resource
	private HccoindetailService hccoindetailService;
	
	@Resource
	private HibernateSupport dao;
	
	/** 注册宝付校验身份证id是否已经实名认证 */
	public Map<String, String> validCardId(String cardId, long userId) {
		Map<String, String> resultMap = new HashMap<String, String>();
		String sql = "select * from userrelationinfo where user_id!=? and cardId=?";
		List list = dao.findBySql(sql, userId, cardId);
		if(list != null && list.size() > 0) {
			resultMap.put("code", "-1");
			resultMap.put("msg", "当前身份证号码已被其他用户使用！");
		} else {
			resultMap.put("code", "0");
			resultMap.put("msg", "校验通过！");
		}
		return resultMap;
	}
	/** 确定开通宝付页面--调用宝付相关页面接口  */
	public void doOpenBaoFuAccount(HttpServletRequest request, long userId, String name, String cardId) {
		try {
			UserInfoCache.setNameToRedis(userId, name);
			UserInfoCache.setCardIdToRedis(userId, cardId);
			Userbasicsinfo user = userDao.getUserById(userId);
			String pMerBillNo = user.getpMerBillNo();
			if(StringUtil.isBlank(pMerBillNo)) {
				Date date = new Date();
				pMerBillNo = "" + date.getTime() + userId;
			}
			
			BaoFuAccountVo vo = new BaoFuAccountVo();
			vo.setBf_account(user.getUserrelationinfo().getPhone());
			vo.setName(name);
			vo.setId_card(cardId);
			vo.setUser_id(pMerBillNo);
			vo.setReturn_url(Constant.PC_OPEN_BAOFU_ASYNC_URL);
			vo.setPage_url(Constant.PC_OPEN_BAOFU_ACCOUNT_SUCCESS_URL);
			
			String sdkRechargeXml = ParseXML.pageOpenBaofuXML(vo);
			String sign = CommonUtil.aesEncryptKey16(sdkRechargeXml , ParameterIps.getMerchantKey());
			LOG.error("调用开通宝付页面接口所传报文：" + sdkRechargeXml);
			LOG.error("报文加密结果：" + sign);
			request.setAttribute("terminal_id", ParameterIps.getTerminalnuMber());//终端号
			request.setAttribute("merchant_id", ParameterIps.getCert());//商户号
			request.setAttribute("sign", sign);
			request.setAttribute("vo", vo);
			payLogService.savePayLog(sdkRechargeXml, user.getId(), 1, pMerBillNo, Long.valueOf(pMerBillNo), 0.00, 0.00, 0.00);
			userDao.updateUserAccountInfo(userId, pMerBillNo);
			LOG.error("生成的宝付账号：" + pMerBillNo);
		} catch(Exception e) {
			LOG.error("调用宝付页面接口过程中报错！", e);
			throw new RuntimeException("调用宝付页面接口过程中报错！", e);
		}
	}
	
	/** 开通宝付账户 -- 宝付回调 */
	public String asyncOpenBaoFuPage(String pMerBillNo) {
		Userbasicsinfo user = userDao.getUserByMerBillNo(pMerBillNo);
		if(user != null) {
			String currentTime = DateUtil.format("yyyy-MM-dd HH:mm:ss");
			String name = UserInfoCache.getNameFromRedis(user.getId());
			user.setName(name);
			user.setpIpsAcctDate(currentTime);
			user.setHasIpsAccount(1);
			user.setIsAuthIps(1);
			user.setAuthIpsTime(currentTime);
			user.setCardStatus(2);
			generalizeService.updateGeneralize(user);
			payLogService.updatePayLog(pMerBillNo, Constant.STATUES_ONE);
			userDao.updateUserInfoAfterOpenAccount(user.getId());
			
			//理财师(被推广人)
			if(generalizeService.getGeneralizeIsAuthIps(user.getId())){
				user.setUserType(Constant.STATUES_SIX);
			}
			//通过被推广人id反查推广人
			Userbasicsinfo userGen = generalizeService.queryPromoterByPromotedId(user.getId());
			if(userGen!=null){
				//添加员工推荐注册增加红筹币
				if(userGen.getUserType() == 2){
					hccoindetailService.saveHccoindetailNumber(userGen);
				}
				
				//理财师(推广人)
				if((userGen.getUserType() == 1 || userGen.getUserType() == 3) && userGen.getIsAuthIps() == 1){
					userGen.setUserType(Constant.STATUES_SIX);
					dao.update(userGen);
				}
			}
			
			LOG.error("宝付支付注册成功，用户Id=" + user.getId());
		} else {
			LOG.error("宝付支付注册失败，用户Id=" + user.getId());
		}
		return "";
	}
}