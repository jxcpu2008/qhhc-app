package com.hc9.web.main.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Userbasicsinfo;
import com.hc9.web.main.entity.Userrelationinfo;
import com.hc9.web.main.entity.Validcodeinfo;
import com.hc9.web.main.redis.SmsEmailCache;
import com.hc9.web.main.service.smsmail.EmailService;
import com.hc9.web.main.service.smsmail.SmsService;
import com.hc9.web.main.util.DateUtils;
import com.hc9.web.main.util.GenerateLinkUtils;
import com.hc9.web.main.util.StringUtil;
import com.jubaopen.commons.LOG;

import freemarker.template.TemplateException;

/**
 * 找回密码
 * 
 * @author frank
 * 
 */
@Service
public class FindPassWordService {

    /**
     * 数据库接口
     */
    @Resource
    private HibernateSupport commonDao;

    /**
     * 邮件接口
     */
    @Resource
    private EmailService emailService;
    /**
     * 短信接口
     */
    @Resource
    private SmsService smsService;

    
    /**
    * <p>Title: queryUserlationBysome</p>
    * <p>Description:通过电话号码或邮箱找到用户的联系信息 </p>
    * @param phone 电话
    * @param state 1.电话  2 邮箱
    * @return 用户的联系信息
    */
    public Userrelationinfo queryUserlationBysome(String phone,int state){
    	StringBuffer querysql=new StringBuffer("SELECT * from userrelationinfo where ");
    	if(state==1){
    		querysql.append("phone=").append(phone);
    	}else{
    		querysql.append("email='").append(phone).append("'");
    	}
    	
    	List<Userrelationinfo> userrelalist=commonDao.findBySql(querysql.toString(), Userrelationinfo.class);
    	return userrelalist.size()>0?userrelalist.get(0):null;
    }

    /**
     * 发送找回密码邮件
     * 
     * @param user
     *            用户信息
     * @param code
     *            验证码
     * @throws IOException
     *             异常
     * @throws TemplateException
     *             异常
     */
    public void sendEmail(Userbasicsinfo user,String email, String code,
            HttpServletRequest request) throws IOException, TemplateException {
        // 收件人地址
        Map<String, String> map = new HashMap<String, String>();
        if (null == user.getName() || "您好，您还没填写真实姓名".equals(user.getName())) {
            map.put("name", user.getUserName());
        } else {
        	map.put("name", user.getName());
        }
        user.setRandomCode(code);
        String emailActiveUrl=GenerateLinkUtils.generateResetPwdLink(user, request);
		map.put("time", DateUtils.format("yyyy-MM-dd HH:mm:ss"));
		map.put("emailActiveUrl", emailActiveUrl);
        
        String[] msg = emailService.getEmailResources("forget-email.ftl", map);
        // 发送邮件链接地址
        emailService.sendEmail(msg[0], msg[1], email);
    }

    /**
     * 发送找回密码邮件
     * 
     * @param user
     *            会员基本信息
     * @return 1 发送成功，2 发送频繁
     * @throws TemplateException
     *             异常
     * @throws IOException
     *             异常
     */
    @SuppressWarnings("unchecked")
    public Integer sendEmailCodel(Userbasicsinfo user,String email,HttpServletRequest request) throws IOException, TemplateException {
        // 查询消息表里面是否已经存在该用户发送记录
        List<Validcodeinfo> list = commonDao
                .find("from Validcodeinfo validate where validate.userbasicsinfo.id="
                        + user.getId());

        // 邮件验证码
        String code = StringUtil.getvalidcode();

        Validcodeinfo info = null;
        // 如果存在
        if (list != null && list.size() > 0) {
            info = list.get(0);

            // 如果发送时间不超过两分钟
            Long time = System.currentTimeMillis() - 60 * 2 * 1000;
            if (null != info.getEmailagaintime()
                    && info.getEmailagaintime() > time) {
                return 2;
            } else {
                // 修改邮箱随机验证码
                info.setEmailcode(code);
                // 修改邮箱验证码发送时间
                info.setEmailagaintime(System.currentTimeMillis());
                // 修改邮箱验证码过期时间(两个小时后验证码失效)
                info.setEmailovertime(System.currentTimeMillis() + 60 * 60 * 2
                        * 1000);
                info.setUserbasicsinfo(user);
                commonDao.update(info);
            }
        } else {
            info = new Validcodeinfo();
            // 修改邮箱随机验证码
            info.setEmailcode(code);
            // 修改邮箱验证码发送时间
            info.setEmailagaintime(System.currentTimeMillis());
            // 修改邮箱验证码过期时间(两个小时后验证码失效)
            info.setEmailovertime(System.currentTimeMillis() + 60 * 60 * 2
                    * 1000);
            info.setUserbasicsinfo(user);
            commonDao.save(info);

        }
        // 发送邮件
        sendEmail(user, email, code, request);
        return 1;
    }
    
    
    /**
    * <p>Title: sendsesCodel</p>
    * <p>Description:发送短信验证码找回密码 </p>
    * @param user 用户
    * @param phone  电话
    * @param request 请求
    * @return 1 发送成功，4发送频繁 5 异常
    */
    public Integer sendsesCodel(Userbasicsinfo user,String phone,HttpServletRequest request) {
    	// 查询消息表里面是否已经存在该用户发送记录
        List<Validcodeinfo> list = commonDao
                .find("from Validcodeinfo validate where validate.userbasicsinfo.id="+ user.getId());
        //短信发送的验证码
        String code = StringUtil.getvalidcode();
        request.getSession().setAttribute("regCode",code);

        Validcodeinfo info = null;
        
        if (list != null && list.size() > 0) {
        	info = list.get(0);

            // 如果发送时间不超过两分钟
            Long time = System.currentTimeMillis() - 60 * 2 * 1000;
            if (null != info.getSmsagainTime()&& info.getSmsagainTime() > time) {
                return 4;
            } else {
            	 // 修改短信随机验证码
                info.setSmsCode(code);
                // 修改短信验证码发送时间
                info.setSmsagainTime(System.currentTimeMillis());
                // 修改邮箱验证码过期时间(30分钟验证码失效)
                info.setSmsoverTime(System.currentTimeMillis() + 60 * 30* 1000);
                info.setUserbasicsinfo(user);
                
                commonDao.update(info);
            }
        }else{
        	   info = new Validcodeinfo();
               // 修改短信随机验证码
               info.setSmsCode(code);
               // 修改短信验证码发送时间
               info.setSmsagainTime(System.currentTimeMillis());
               // 修改邮箱验证码过期时间(30分钟验证码失效)
               info.setSmsoverTime(System.currentTimeMillis() + 60 * 30* 1000);
               info.setUserbasicsinfo(user);
               commonDao.save(info);
        }
        //发送短信

		  try {
			Map<String,String> map = new HashMap<String,String>();
			map.put("code", code);
			String content = smsService.getSmsResources("check-code.ftl", map);
			int trigger=Integer.valueOf(SmsEmailCache.getSmsTriggerChannel());
			smsService.chooseSmsChannel(trigger, content, phone);
		} catch (Exception e) {
			LOG.error("短信发送失败 "+e.getMessage());
		}
		
        return 1;
    }
}
