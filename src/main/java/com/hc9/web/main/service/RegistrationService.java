package com.hc9.web.main.service;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.jfree.util.Log;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.hc9.commons.normal.Md5Util;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.constant.IntegralType;
import com.hc9.web.main.entity.Autointegral;
import com.hc9.web.main.entity.ChannelSpreadDetail;
import com.hc9.web.main.entity.Generalize;
import com.hc9.web.main.entity.RedEnvelopeDetail;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Userfundinfo;
import com.hc9.web.main.entity.Userloginlog;
import com.hc9.web.main.entity.Usermessage;
import com.hc9.web.main.entity.Userrelationinfo;
import com.hc9.web.main.entity.Validcodeinfo;
import com.hc9.web.main.redis.SysCacheManagerUtil;
import com.hc9.web.main.redis.activity.year2016.month05.HcNewerTaskCache;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.GenerateLinkUtils;
import com.hc9.web.main.util.GetIpAddress;
import com.hc9.web.main.util.StringUtil;
import com.hc9.web.main.vo.LoginRelVo;

import freemarker.template.TemplateException;

/** 用户注册 */
@Service
public class RegistrationService {

	@Resource
	private HibernateSupport commonDao;

	@Resource
	private LocalService localService;

	@Resource
	private UserbasicsinfoService userbasicsinfoService;

	@Resource
	private MemberCenterService centerService;

	@Resource
	private IntegralSevice integralSevice;
	
	@Resource
	private RedEnvelopeDetailService detailService;

	/**
	 * 用户注册
	 * 
	 * @param userName 用户名
	 * @param phone 电话
	 * @param pwd 登陆了密码
	 * @param number 会员编号
	 * @param recommend
	 * @param character 用户类型
	 * @param promoter 推广人
	 * @param record 抽奖投资记录
	 * @param request request
	 * @return 如果存在，返回用户信息，不存在返回null
	 * 
	 * @throws TemplateException
	 *             TemplateException
	 * @throws IOException
	 *             IOException
	 * @throws DataAccessException
	 *             DataAccessException
	 */
	public Userbasicsinfo registrationSave(String userName, String phone,
			String pwd, String number, Userbasicsinfo promoter, Integer registerSource, HttpServletRequest request)
			throws DataAccessException, IOException, TemplateException {

		// 当前时间
		String date = DateUtils.format(null);

		// 用户基本信息
		Userbasicsinfo userInfo = new Userbasicsinfo();
		// 用户关联信息表
		Userrelationinfo user = new Userrelationinfo();
		// user.setRecommend(recommend);
		// 用户资金信息
		Userfundinfo userFund = new Userfundinfo();
		// 用户系统消息
		Usermessage userMessage = new Usermessage();
		// 邮箱限制信息
		Validcodeinfo validcodeinfo = new Validcodeinfo();
		// 积分
		// Manualintegral mt = new Manualintegral();

		// 注册时间
		userInfo.setCreateTime(date);
		// 邮箱激活验证码
		userInfo.setRandomCode(StringUtil.getvalidcode());
		// 是否被锁[1-是，0-否]
		userInfo.setIsLock(0);
		// 机构投资人
		userInfo.setIsorgperson(0);
		// 众持融资人
		userInfo.setIscrowdhold(0);
		// 众筹融资人
		userInfo.setIscrowdfundingperson(0);
		// 登录错误次数
		userInfo.setErrorNum(0);
		// 用户类型
		userInfo.setIsCreditor(1);
		// 用户名/邮箱
		userInfo.setUserName(userName.trim());
		// 实名未认证
		userInfo.setName("");
		userInfo.setCardStatus(0);

		// 积分设为0
		userInfo.setUserintegral(0);

		userInfo.setpMerBillNo("");

		// 初始化登录密码
		userInfo.setPassword(pwd);
		// 初始化交易密码（与登录密码一致）
		userInfo.setTransPassword(pwd);
		// 普通会员
		userInfo.setUserType(1);
		// 注册来源
		userInfo.setRegisterSource(registerSource);

		// 保存会员基本信息
		commonDao.save(userInfo);

		// 判断是否有推广人，如果有插入推广数据.推广人获得推广积分要到被推广人实名认证后
		Userbasicsinfo promoter2 = null;
		if (!StringUtil.isBlank(number)) {
			boolean isNumber = true;
			if (!StringUtil.isNumberString(number)) {
				isNumber = false;
			}
			String sql = "SELECT * from userbasicsinfo where ";
			// 纯数字，2种可能，员工编号或者手机号；非数字，3种可能，普通人的推广编号或者用户名或者实名.只考虑普通人的推广编号
			if (isNumber) {
				if (number.length() > 10) {
					String queryPhone = "SELECT id from userrelationinfo WHERE phone=?";
					Object obj = commonDao.findObjectBySql(queryPhone, number);
					sql += "id=?";
					number = obj.toString();
				} else {
					if("0".equals(number.substring(0, 1))){
						sql += "staff_no = ?";
					}else{
						sql += "id = ?";
					}
				}

			} else {
				number = number.toUpperCase();
				if (number.indexOf("TG-") >= 0) { // “TG-001”
					number = number.substring(3);
					sql += "id=?";
				}

			}

			// sql+="userName=?";

			// 查询用户是否存在
			promoter2 = commonDao.findObjectBySql(sql, Userbasicsinfo.class,
					number);
		}
		if (promoter != null || promoter2 != null) {

			Generalize generalize = new Generalize();
			generalize.setAdddate(date);
			if (promoter != null) {
				generalize.setGenuid(promoter.getId());
			} else if (promoter == null) {
				generalize.setGenuid(promoter2.getId());
			}

			generalize.setByUser(userInfo);
			generalize.setUanme(userInfo.getName());
			generalize.setState(1);// 推广初始1
			generalize.setCode(number);// 记录推广码
			// 保存推广信息
			commonDao.save(generalize);

		}
		// 机构认证状态
		user.setAudit(0);
		// 手机
		user.setPhone(phone);
		// 是否认证
		user.setPhonePass(1);
		// 是否激活邮箱
		user.setEmailisPass(0);
		// 默认头像路径
		user.setImgUrl("/resources/images/headimg.jpg");
		// 用户基本信息
		user.setUserbasicsinfo(userInfo);
		// 保存用户关联信息
		commonDao.save(user);

		// 会员邮箱限制信息
		validcodeinfo.setUserbasicsinfo(userInfo);
		validcodeinfo
				.setEmailagaintime(System.currentTimeMillis() + 2 * 60 * 1000L);
		validcodeinfo.setEmailovertime(System.currentTimeMillis() + 24 * 60
				* 60 * 1000L);
		validcodeinfo.setEmailcode(userInfo.getRandomCode());
		commonDao.save(validcodeinfo);

		// 用户基本信息
		userFund.setUserbasicsinfo(userInfo);
		// 可用余额
		userFund.setCashBalance(0.00);
		userFund.setOperationMoney(0.00);
		// 奖金余额
		userFund.setBonusBalance(0.00);
		userFund.setMoney(0.0000);
		// 授信额度
		userFund.setCredit(0.00);
		// 保存用户资金信息
		commonDao.save(userFund);

		// 用户基本信息
		userMessage.setUserbasicsinfo(userInfo);
		// 消息内容
		String myphone = centerService.getEncryptionPhone(phone);

		userMessage.setContext("恭喜您成功注册成为前海红筹的用户！您的注册帐户是:" + userName
				+ " 手机号是:" + myphone);
		// 未读
		userMessage.setIsread(0);
		// 发送时间
		userMessage.setReceivetime(date);
		// 发送主题
		userMessage.setTitle("注册成功");
		// 保存系统消息
		commonDao.save(userMessage);

		// 关联用户基本信息
		userInfo.setUserrelationinfo(user);

		commonDao.update(userInfo);
		
		HcNewerTaskCache.giveNewerRegisterRedenvelopeKey(userInfo.getId());
		
		// 保存注册用户session
		request.getSession().setAttribute(Constant.SESSION_USER, userInfo);

		return userInfo;
	}

	/**
	 * 验证推荐人是否存在
	 * 
	 * @param userName
	 *            用户名
	 * @return 唯一true 不唯一false
	 */
	public String checkReferrer(String number) {
		boolean isNumber = true;
		if (!StringUtil.isNumberString(number)) {
			isNumber = false;
		}
		StringBuffer sql = new StringBuffer("SELECT case cardStatus when 2 then name else userName end,cardStatus from userbasicsinfo where ");
		// 纯数字，2种可能，员工编号或者手机号；非数字，3种可能，普通人的推广编号或者用户名或者实名.只考虑普通人的推广编号
		if (isNumber) {
			if (number.length() > 10 && number.startsWith("1")) {
				String queryPhone = "SELECT id from userrelationinfo WHERE phone=?";
				Object obj = commonDao.findObjectBySql(queryPhone, number);
				if (obj != null) {
					number = obj.toString();
				}
				sql.append("id=?");
			} else if(number.startsWith("0")){
				sql.append("staff_no=?");
			}else{
				sql.append("id=?");
			}

		} else {
			sql.append(" 1=2 and userName=? ");
		}
		// 配备该用户名下的条数
		Object[] name = (Object[]) commonDao.findObjectBySql(sql.toString(),
				number);
		if (name != null) {
			String content = name[0].toString();
			if (null != name[1] && "2".equals(name[1].toString())) {
				String after = content.length() > 2 ? content.substring(content
						.length() - 1) : "";
				return "推荐人姓名：" + content.substring(0, 1) + "**" + after;
			} else {
				return "推荐人用户名："
						+ content.replace(
								content.substring(2, content.length() - 2),
								"**");
			}
		} else {
			return "null";
		}
	}

	/** 验证用户名是否唯一
	 * 
	 * @param userName
	 *            用户名
	 * @return 唯一true 不唯一false
	 */
	public boolean checkUserName(String userName) {
		// 定义返回值变量
		if(SysCacheManagerUtil.isUserExistInRedis(userName)) {
			return false;
		}
		String sql = "SELECT COUNT(1) FROM userbasicsinfo a where a.userName=?";
		// 配备该用户名下的条数
		Object count = commonDao.findObjectBySql(sql, userName);
		if(Integer.parseInt(count.toString()) > 0){
			return false;
		}
		sql = "SELECT COUNT(1) FROM userrelationinfo b where  b.phone=?";
		count = commonDao.findObjectBySql(sql, userName);
		if (Integer.parseInt(count.toString()) > 0) {
			return false;
		}
		return true;
	}

	public boolean regCheckUserName(String userName) {
		// 定义返回值变量
		boolean bool = true;
		String sql = "SELECT COUNT(1) FROM userbasicsinfo "
				+ "where userName=? ";
		// 配备该用户名下的条数
		Object count = commonDao.findObjectBySql(sql.toString(), userName);
		if (Integer.parseInt(count.toString()) > 0) {
			bool = false;
		}
		return bool;
	}

	/**
	 * 验证邮箱唯一性
	 * 
	 * @param email
	 *            邮箱
	 * @return 唯一true 不唯一false
	 */
	public boolean checkEmail(String id, String email) {
		// 定义返回值变量
		boolean bool = true;
		StringBuffer sql = new StringBuffer(
				"SELECT COUNT(1) FROM userrelationinfo a WHERE a.email='"
						+ email.trim() + "'");
		if (StringUtil.isNotBlank(id)) {
			sql.append(" and a.id <> " + id);
		}
		// 配备该用户名下的条数
		Object count = commonDao.findObjectBySql(sql.toString());
		if (Integer.parseInt(count.toString()) > 0) {
			bool = false;
		}
		return bool;
	}

	/**
	 * 登录
	 * 
	 * @param userName
	 *            用户名（邮箱）
	 * @param pwd
	 *            密码
	 * @return 登录成功返回Userbasicsinfo对象
	 */
	public Userbasicsinfo loginMethod(String userName, String pwd) {
		if(SysCacheManagerUtil.getLoginErrorNumToday(userName) >= 5){
			return null;//密码连续错误5次以上
		}
		pwd = Md5Util.execute(pwd);
		Userbasicsinfo userInfo = null;
		LoginRelVo loginRelVo = SysCacheManagerUtil.getLoginRelVoByLoginNo(userName);
		if(loginRelVo != null) {
			if(userName.equals(loginRelVo.getUserName()) || userName.equals(loginRelVo.getPhone())) {
				if(pwd.equals(loginRelVo.getPassword())) {
					userInfo = new Userbasicsinfo();
					userInfo.setId(loginRelVo.getId());
					userInfo.setUserName(loginRelVo.getUserName());
					userInfo.setIsAuthIps(loginRelVo.getIsAuthIps());
					userInfo.setCardStatus(loginRelVo.getCardStatus());
					userInfo.setpMerBillNo(loginRelVo.getpMerBillNo());
					return userInfo;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		// 查询用户名（邮箱）、密码是否匹配
		String hql = "from Userbasicsinfo a,Userrelationinfo b where b.userbasicsinfo.id=a.id and (a.userName=? or b.phone=?) and a.password=? ";
		List list = commonDao.find(hql, userName, userName, pwd);
		// 如果登录成功
		if (list.size() > 0) {
			Object[] obj = (Object[]) list.get(0);
			userInfo = (Userbasicsinfo) obj[0];
			//如果错误5次以内，且最后成功登录，则删除锁定的key
			if(SysCacheManagerUtil.getLoginErrorNumToday(userName)<5){
				SysCacheManagerUtil.delLoginErrorNumToday(userName);//登录成功，删除锁定
			}
		}
		return userInfo;
	}

	/***
	 * 投标支付密码进行验证
	 * 
	 * @param id
	 * @param pwd
	 * @return
	 */
	public Userbasicsinfo loginTransPassword(Long id, String pwd) {
		Userbasicsinfo userInfo = null;
		// 查询用户名（邮箱）、密码是否匹配
		StringBuffer hql = new StringBuffer(
				"from Userbasicsinfo a  where a.id=?  and a.transPassword=? ");
		pwd = Md5Util.execute(pwd);
		List list = commonDao.find(hql.toString(), id, pwd);
		// 如果登录成功
		if (list.size() > 0) {
			userInfo = (Userbasicsinfo) list.get(0);
		}
		return userInfo;
	}

	/**
	 * 判断用户是否被管理员锁定
	 * 
	 * @param user
	 *            用户对象
	 * @return true 锁定，false 未锁定
	 */
	public boolean isLock(Userbasicsinfo user) {
		StringBuffer sql = new StringBuffer(
				"SELECT COUNT(*) FROM userbasicsinfo a WHERE a.isLock=1 AND id=?");
		Object obj = commonDao.findObjectBySql(sql.toString(), user.getId());
		// 如果大于0，锁定
		if (Integer.parseInt(obj.toString()) > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 添加登录日志
	 * 
	 * @param user
	 *            用户信息
	 * @param ip
	 *            ip地址
	 * 
	 */
	public void saveUserLog(Userbasicsinfo user, String ip) {
		String date = DateUtils.format(null);
		Userloginlog loginLog = new Userloginlog();
		loginLog.setLogintime(date);
		loginLog.setUserbasicsinfo(user);
		loginLog.setIp(ip);
		loginLog.setAddress(localService.getRequesterAddressByIP(ip));
		// 保存登录日志
		commonDao.save(loginLog);
	}

	/**
	 * 判断电话是否唯一
	 * 
	 * @param phone
	 *            电话号码
	 * @return 是否唯一
	 */
	public boolean checkPhone(String id, String phone) {
		if(SysCacheManagerUtil.isUserExistInRedis(phone)) {
			return false;
		}
		boolean bool = true;
		String sql = "SELECT COUNT(1) FROM userrelationinfo a WHERE a.phone=?";
		if (StringUtil.isNotBlank(id)) {
			sql += " and a.id <> " + id;
		}
		// 配备该条数
		Object count = commonDao.findObjectBySql(sql.toString(), phone);
		if (Integer.parseInt(count.toString()) > 0) {
			bool = false;
		} else {
			sql = "select count(1) FROM userbasicsinfo a WHERE a.userName=?";
			count = commonDao.findObjectBySql(sql, phone);
			if(Integer.parseInt(count.toString()) > 0) {
				bool = false;
			}
		}
		return bool;
	}
	
	/**
	 * 判断邮箱是否激活
	 * 
	 * @param phone
	 *            电话号码
	 * @return 是否唯一
	 */
	public boolean checkLoginEmail(String name) {
		boolean bool = false;
		StringBuffer sql = new StringBuffer(
				"SELECT COUNT(*) FROM userbasicsinfo a,userrelationinfo b WHERE b.user_id=a.id and (a.userName=? or b.email=? or b.phone=?) and b.emailisPass=1");
		// 配备该条数
		Object count = commonDao.findObjectBySql(sql.toString(), name, name,
				name);

		if (Integer.parseInt(count.toString()) > 0) {
			bool = true;
		}

		return bool;
	}

	public boolean checkLoginEmailx(String name) {
		boolean bool = false;
		StringBuffer sql = new StringBuffer(
				"SELECT COUNT(*) FROM userbasicsinfo a,userrelationinfo b WHERE b.user_id=a.id and (a.userName=? or b.email=? or b.phone=?)");
		// 配备该条数
		Object count = commonDao.findObjectBySql(sql.toString(), name, name,
				name);

		if (Integer.parseInt(count.toString()) > 0) {
			bool = true;
		}

		return bool;
	}

	/**
	 * 验证激活链接
	 * 
	 * @param id
	 *            用户id
	 * @param request
	 * @return 0表示激活失败 1表示已经激活了邮箱 2表示激活链接时间失效 3表示激活成功
	 */
	public Integer activateAccount(Long id, HttpServletRequest request) {
		// 取得user并判断是否存在该user
		Userbasicsinfo user = userbasicsinfoService.queryUserById(id);
		if (user == null) {
			return 0;
		} else {
			// 取得邮箱验证码信息，判断是否存在
			Validcodeinfo validCode = (Validcodeinfo) commonDao.findObject(
					"FROM Validcodeinfo v WHERE v.userbasicsinfo.id=?", id);
			if (validCode == null) {
				return 0;
			} else {
				// 判断是否已经激活过邮箱
				if (validCode.getEmailovertime() == null) {
					return 1;
				}
				// 取得系统当前时间的毫秒数 与链接失效时间比较
				Long time = System.currentTimeMillis();
				if (time > validCode.getEmailovertime()) {
					return 2;
				} else {
					if (GenerateLinkUtils.verifyCheckcode(user, request)) {
						// 激活验证通过则将邮箱是否激活的状态改为1通过
						user.getUserrelationinfo().setEmailisPass(1);
						// 将邮箱验证的再次发送时间和过期时间设置为null
						validCode.setEmailagaintime(null);
						validCode.setEmailovertime(null);

						// 激活成功后添加登陆日志
						String ip = GetIpAddress.getIp(request);
						saveUserLog(user, ip);
						commonDao.update(validCode);
						commonDao.update(user);
						request.getSession().setAttribute(
								Constant.SESSION_USER, user);// 重置session

						// 邮箱激活加5积分
						integralSevice.activateEmail(user, IntegralType.MAIL);

						return 8;
					} else {
						return 0;

					}
				}
			}
		}
	}

	public Serializable saveSpreadDetail(ChannelSpreadDetail csd) {
		try {
			return commonDao.save(csd);
		} catch (Exception e) {
			Log.error("保存渠道推广明细的过程中出错！", e);
			return null;
		}
	}
}
