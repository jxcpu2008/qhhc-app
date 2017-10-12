package com.hc9.web.main.service.dadabus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.DadaBusCashCertificate;
import com.hc9.web.main.redis.RedisHelper;
import com.hc9.web.main.redis.activity.year2015.DadaBusCache;
import com.hc9.web.main.service.CacheManagerService;
import com.hc9.web.main.service.RedEnvelopeDetailService;
import com.hc9.web.main.service.dadabus.encrypt.DadaBusCashVo;
import com.hc9.web.main.service.dadabus.encrypt.DadaBusConfigInfo;
import com.hc9.web.main.service.dadabus.encrypt.Md5Encrypt;
import com.hc9.web.main.service.dadabus.encrypt.RsaUtil;
import com.hc9.web.main.util.Arith;
import com.hc9.web.main.util.DateFormatUtil;
import com.hc9.web.main.util.DateUtil;
import com.hc9.web.main.util.JsonUtil;
import com.hc9.web.main.util.StringUtil;
import com.jubaopen.commons.LOG;

/** 嗒嗒巴士代金券送票相关接口 */
@Service
public class DadaBusService {
	@Resource
    private HibernateSupport dao;

	@Resource
	private RedEnvelopeDetailService redEnvelopeDetailService;
	
	/** 赠送嗒嗒巴士代金券相关接口
	 * 2015年12月15日-2016年2月15日 
	 * @param loanId 项目id
	 * @param loanRecordId 投资记录id
	 * @param userId 投资用户主键id
	 * @param investMoney 投资金额
	 * @param mobilePhone 投资用户手机号码
	 * @param monthNum 投资标的的月份
	 * @param subTyp 投资项目的类型：1、优先；2、夹层；3、劣后；
	 * @param refunway 分红方式 1.按月 2.按季度 3.天标还款
	 * @return double 所赠送的嗒嗒巴士代金券总金额
	 *  */
	public double giveDadaBusCashCertificate(Long loanId, Long loanRecordId, Long userId, 
			Double investMoney, String mobilePhone, int monthNum, int subTyp, int refunway) {
		double dadaBusCashMoney = 0;
		/** 投资优先和夹层的投资记录才有机会获取嗒嗒巴士代金券 */
		if(subTyp == 1 || subTyp == 2) {
			if(refunway == 1 || refunway ==2) {
				/**  判断是否活动期间内 */
				if(DadaBusCache.isDadaBusCashActivity(new Date())) {
					/** 是否存在首笔投资记录 */
					if(!DadaBusCache.existFirstLoanRecord(userId)) {
						/** 记录下用户的首次投资记录id */
						DadaBusCache.setDadaBusCashLoanRecordId(userId, loanRecordId);
						String userkey = "STR:HC9:DADABUS:CASH:RECORD:FLAG:" + userId;
						/** 每人中奖机会，实物奖品最高1次:已获奖的不再参与实物奖品抽奖 */
						if(!RedisHelper.isKeyExist(userkey)) {
							List<DadaBusCashCertificate> dadaBusList = generateDadaBusCashList(
									loanId, loanRecordId, userId, investMoney, mobilePhone, monthNum);
							if(dadaBusList.size() > 0) {
								dao.saveOrUpdateAll(dadaBusList);
								/** 记录用户代金券发放标识 */
								RedisHelper.set(userkey, "1");
								for(DadaBusCashCertificate vo : dadaBusList) {
									dadaBusCashMoney = Arith.add(dadaBusCashMoney, vo.getMoney());
								}
								dispatchDadaBusCash(dadaBusList);
							}
						}
					}
				}
			}
		}
		return dadaBusCashMoney;
	}
	
	/** 生成嗒嗒巴士代金券列表 */
	private List<DadaBusCashCertificate> generateDadaBusCashList(Long loanId, Long loanRecordId, Long userId, 
			Double investMoney, String mobilePhone, int monthNum) {
		List<DadaBusCashCertificate> dadaBusList = new ArrayList<DadaBusCashCertificate>();
		if(monthNum < 3) {
			if(investMoney >= 1000 && investMoney < 5000) {
				//10元嗒嗒巴士代金券
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 10.0, mobilePhone, 1));
			} else if(investMoney >= 5000 && investMoney < 10000) {
				//55元嗒嗒巴士代金券 -- 5、50（共2张）
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 5.0, mobilePhone, 1));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 2));
			} else if(investMoney >= 10000 && investMoney < 30000) {
				//115元嗒嗒巴士代金券 -- 5、10、50、50（共3张）
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 10.0, mobilePhone, 1));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 2));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 3));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 5.0, mobilePhone, 4));
			} else if(investMoney >= 30000) {
				//400元嗒嗒巴士代金券 -- 10、20、20、50、100、200（共6张）
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 10.0, mobilePhone, 1));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 20.0, mobilePhone, 2));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 20.0, mobilePhone, 3));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 4));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 100.0, mobilePhone, 5));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 200.0, mobilePhone, 6));
			}
		} else if(monthNum >= 3) {
			if(investMoney >= 1000 && investMoney < 5000) {
				//20元嗒嗒巴士代金券
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 20.0, mobilePhone, 1));
			} else if(investMoney >= 5000 && investMoney < 10000) {
				//110元嗒嗒巴士代金券 -- 10、50、50（共3张）
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 10.0, mobilePhone, 1));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 2));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 3));
			} else if(investMoney >= 10000 && investMoney < 30000) {
				//230元嗒嗒巴士代金券 -- 10、20、50、50、100（共5张）
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 10.0, mobilePhone, 1));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 20.0, mobilePhone, 2));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 3));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 4));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 100.0, mobilePhone, 5));
			} else if(investMoney >= 30000) {
				//700元嗒嗒巴士代金券 -- 10、20、20、50、100、100、200、200（共8张）
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 10.0, mobilePhone, 1));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 20.0, mobilePhone, 2));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 20.0, mobilePhone, 3));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 50.0, mobilePhone, 4));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 100.0, mobilePhone, 5));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 100.0, mobilePhone, 6));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 200.0, mobilePhone, 7));
				dadaBusList.add(generateDadaBusCash(loanId, loanRecordId, userId, 200.0, mobilePhone, 8));
			}
		}
		return dadaBusList;
	}
	
	/** 生成嗒嗒巴士券 */
	private DadaBusCashCertificate generateDadaBusCash(Long loanId, Long loanRecordId, Long userId, 
			Double money, String mobilePhone, int count) {
		Date date = new Date();
		String createTime = DateFormatUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
		String dateTime = DateFormatUtil.dateToString(new Date(), "yyyyMMdd");
		String orderNo = "TP" + dateTime + loanRecordId + count + generateRandomStr();//发送给嗒嗒巴士的订单号TP+X，10<=X<=24
		DadaBusCashCertificate dadaBusCash = new DadaBusCashCertificate();
		dadaBusCash.setUserId(userId);
		dadaBusCash.setLoanId(loanId);
		dadaBusCash.setLoanRecordId(loanRecordId);
		dadaBusCash.setOrderNo(orderNo);
		dadaBusCash.setMoney(money);
		dadaBusCash.setMobilePhone(mobilePhone);
		dadaBusCash.setStatus("1");//状态：1、已发放，待嗒嗒巴士确认；3、嗒嗒巴士确认成功；
		dadaBusCash.setCreateTime(createTime);
		return dadaBusCash;
	}
	
	/** 六位随机数生成器 */
	private String generateRandomStr() {
		String str = "";
		for(int i = 0; i < 3; i++) {
			int random = (int)(Math.random() * 10);
			str += random;
		}
		return str;
	}

	/** 调用嗒嗒巴士接口发放代金券 */
	private void dispatchDadaBusCash(List<DadaBusCashCertificate> dadaBusList) {
		if(dadaBusList.size() > 0) {
			LOG.error("投资用户待发放的代金券信息如下：" + JsonUtil.toJsonStr(dadaBusList));
			for(DadaBusCashCertificate dadaBusCash : dadaBusList) {
				dispatchDadaBusCash(dadaBusCash);
			}
		}
	}
	
	/** 哒哒巴士代金券发放相关 */
	public void dispatchDadaBusCash(DadaBusCashCertificate dadaBusCash) {
		final DadaBusCashCertificate finalDadaBusCash = dadaBusCash;
		final HibernateSupport finalDao = dao;
		CacheManagerService.threadPoolExecutor.submit(
        	new Thread() {
            	public void run() {
            		LOG.error("投资更新缓存开始：" + Thread.currentThread());
            		BufferedReader in = null;  
    		        String content = null;  
    		        try {
    		        	String url = generateDadaBusRequestUrl(finalDadaBusCash);
    		            // 定义HttpClient  
    		            HttpClient client = new DefaultHttpClient();  
    		            // 实例化HTTP方法  
    		            HttpGet request = new HttpGet();  
    		            request.setURI(new URI(url));
    		            HttpResponse response = client.execute(request);  
    		            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));  
    		            StringBuffer sb = new StringBuffer("");  
    		            String line = "";  
    		            while ((line = in.readLine()) != null) {
    		                sb.append(line);  
    		            }
    		            in.close();  
    		            content = sb.toString();  
    		            LOG.error("嗒嗒巴士代金券发放接口响应结果：" + content);
    		            if(StringUtil.isNotBlank(content)) {
    		            	JSONObject jsonObj = JSON.parseObject(content);
        		            String ret = jsonObj.getString("ret");
        		            String msg = jsonObj.getString("msg");
        		            LOG.error("嗒嗒巴士响应编码，ret=" + ret + ",msg=" + msg);
        		            if("0".equals(ret)) {
        		            	String data = jsonObj.getString("data");
        		            	if(StringUtil.isNotBlank(data)) {
	        		            	List<DadaBusCashVo> list = JSON.parseArray(data, DadaBusCashVo.class);
	        		                if(list != null && list.size() > 0) {
	        		                	DadaBusCashVo vo = list.get(0);
	        		                	String sql = "update dadabuscashcertificate set couponCode=?,price=?," + 
	        		                	"startTime=?,endTime=?,status=3 where orderNo=?";
	        		                	finalDao.executeSql(sql, vo.getCoupon_code(), vo.getPrice(), 
	        		                			vo.getStart_time(), vo.getEnd_time(), finalDadaBusCash.getOrderNo());
	        		                }
	        		            }
        		            }
    		            }
    		        } catch(Exception e) {
    		        	LOG.error("嗒嗒巴士代金券发放过程报错, 对应的代金券信息位：" + JsonUtil.toJsonStr(finalDadaBusCash) +"!", e);
    		        } finally {  
    		            if (in != null) {  
    		                try {  
    		                    in.close();// 最后要关闭BufferedReader  
    		                } catch (Exception e) {  
    		                    LOG.error("嗒嗒巴士代金券发放过程中读写流关闭过程中报错！", e);  
    		                }  
    		            }  
    		        }
            		LOG.error("投资更新缓存结束：" + Thread.currentThread());
            	}
        	}
        );
	}
	
	/** 组装嗒嗒巴士代金券发放相关url */
	private String generateDadaBusRequestUrl(DadaBusCashCertificate dadaBusCash) {
		try {
			String company_code = "hongchou";//合作方码
	        String send_time = "" + System.currentTimeMillis();//请求发送时间
	        String coupon_type = getDadaCouponTypeIdByMoney(dadaBusCash.getMoney());//发放的优惠券类型ID:5：318；10：319；20：320；50：321；100：322；20：323； */
	        
	        String mobile = dadaBusCash.getMobilePhone();//用户手机号
	        String order_number = dadaBusCash.getOrderNo();//交易号
	        String param="company_code=" + company_code + "&send_time=" + send_time 
	        		+ "&coupon_type=" + coupon_type + "&mobile=" + mobile + "&order_number=" + order_number;
	        LOG.error("开始调用嗒嗒巴士代金券发放接口，所传递的参数位：" + param);
	        String ciphertext = RsaUtil.encryptByDadaBusPublicKey(param);
	        String md5ParamStr = Md5Encrypt.md5EncryptString(param);
	        String signature = RsaUtil.encryptByHc9PrivateKey(md5ParamStr);
	        ciphertext = URLEncoder.encode(ciphertext, "utf-8");
	        signature = URLEncoder.encode(signature, "utf-8");
	        String dadaServiceUrl = DadaBusConfigInfo.getDadaBusServiceUrl();
	        String url = dadaServiceUrl + "?ciphertext=" + ciphertext + "&signature=" + signature;
	        LOG.error("调用嗒嗒巴士所使用的url位：" + url);
	        return url;
		} catch(Exception e) {
			throw new RuntimeException("嗒嗒巴士代金券发放url组装过程中出现异常！", e);
		}
	}
	
	/** 获取嗒嗒巴士代金券金额对应的优惠券类型id
	 * 发放的优惠券类型ID:5：318；10：319；20：320；50：321；100：322；200：323；  */
	private String getDadaCouponTypeIdByMoney(Double money) {
		String str = "" + money.longValue();
		return DadaBusConfigInfo.getCashTypeIdMap().get(str);
	}
	
	/** 生成嗒嗒巴士券（抽奖） */
	public DadaBusCashCertificate generateDadaBusCash(Long userId, 
			Double money, String mobilePhone) {
		Date date = new Date();
		String createTime = DateFormatUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
		String dateTime = DateFormatUtil.dateToString(new Date(), "yyyyMMdd");
		String dateTimeDetail = DateFormatUtil.dateToString(new Date(), "yyyyMMddHHmmss");
		String key = "DADA:TODAY:" + dateTime;
		long count = RedisHelper.incrBy(key, 1);
		String orderNo = "TP" + dateTime + count + generateRandomStr();//发送给嗒嗒巴士的订单号TP+X，10<=X<=24
		DadaBusCashCertificate dadaBusCash = new DadaBusCashCertificate();
		dadaBusCash.setUserId(userId);
		dadaBusCash.setLoanId(0L);
		dadaBusCash.setLoanRecordId(0L);
		dadaBusCash.setOrderNo(orderNo);
		dadaBusCash.setStartTime(dateTimeDetail);
		dadaBusCash.setEndTime(DateUtil.getSpecifiedMonthAfter(dateTimeDetail, 12 , "yyyyMMddHHmmss"));  // 有效期1年
		dadaBusCash.setMoney(money);
		dadaBusCash.setMobilePhone(mobilePhone);
		dadaBusCash.setStatus("1");//状态：1、已发放，待嗒嗒巴士确认；3、嗒嗒巴士确认成功；
		dadaBusCash.setCreateTime(createTime);
		return dadaBusCash;
	}

}