package com.hc9.web.main.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.hc9.web.main.constant.HostAddress;
import com.hc9.web.main.constant.ParameterIps;

/** 基础系统常量 */
public class Constant {

    /** 默认编码格式 */
	public static final String CHARSET_DEFAULT = "UTF-8";
    
    /** 一段时间内最大请求次数 */
	public static final int NUMBER_MAX_QUICK_REQUEST_COUNT = 20;
    
    /** 一段时间 */
	public static final int NUMBER_MAX_QUICK_REQUEST_TIME = 10000;
    
    /** 首页 */
	public static final String WEB_INDEX = "index";
    
	public static final String SUCCESS="0000";
    
    /** URL_ERROR_500 */
	public static final String URL_ERROR_500 = "/error-500.jsp";
    
    /** 服务器域名 */
	public static final String WEBSERVER = HostAddress.getHostAddress();
    
    /** URL_ERROR_500_MSG
     */
	public static final String URL_ERROR_500_MSG = "msg";
    
    /** URL_ERROR_500_MSG_VAL_0 */
	public static final String URL_ERROR_500_MSG_VAL_0 = "您请求的方式非法！";
    
    /** URL_SUCCESS_REGIST */
	public static final String URL_SUCCESS_REGIST = "/member_index/member_center.htm";

    /**URL_LOGIN*/
	public static final String URL_LOGIN = WEBSERVER+"/visitor/to-login";
    
    /** PATH_MARKER_MODEL */
	public static final String PATH_MARKER_MODEL = "config/marker/html/";
    
    /** PATH_DYNAMIC_VIEW */
	public static final String PATH_DYNAMIC_VIEW = "views/framework/";
    
    /** DEFAULT_TIME_FORMAT */
	public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** DEFAULT_DATE_FORMAT */
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /** 产品认购信息 */
	public static final String ATTRIBUTE_PRODUCT_PAY_INFO = "attribute_product_pay_info";
    
    /** ATTRIBUTE_UPDATE_HEADER_TIME */
	public static final String ATTRIBUTE_UPDATE_HEADER_TIME = "update_header_time";

    /** ATTRIBUTE_ROOT_PATH */
	public static final String ATTRIBUTE_ROOT_PATH = "root_path";

    /** ATTRIBUTE_MSG */
	public static final String ATTRIBUTE_MSG = "msg";
    
    /**
     * ATTRIBUTE_USER
     */
	public static final String ATTRIBUTE_USER = "session_user";

    /**
     * ATTRIBUTE_REGIST_CHECK_CODE
     */
	public static final String ATTRIBUTE_REGIST_CHECK_CODE = "regist_check_code";
    
    /**
     * ATTRIBUTE_LOGIN_CHECK_CODE
     */
	public static final String ATTRIBUTE_LOGIN_CHECK_CODE = "login_check_code";

    /**
     * ATTRIBUTE_TOPIC
     */
	public static final String ATTRIBUTE_TOPIC = "topics";
    
    /**
     * ATTRIBUTE_ACTIVE_TOPIC
     */
	public static final String ATTRIBUTE_ACTIVE_TOPIC = "activetopic";

    /**
     * PROPERTIES_MSG_TITLE_WELCOME_REGIST
     */
	public static final String PROPERTIES_MSG_TITLE_WELCOME_REGIST = "msg_title_welcome_regist";
    
    /**
     * PROPERTIES_MSG_CONTEXT_WELCOME_REGIST
     */
	public static final String PROPERTIES_MSG_CONTEXT_WELCOME_REGIST = "msg_context_welcome_regist";

    /**
     * PROPERTIES_EMAIL_SUBJECT_ACCOUNT_ACTIVATE
     */
	public static final String PROPERTIES_EMAIL_SUBJECT_ACCOUNT_ACTIVATE = "msg_subject_account_activate";
    
    /**
     * PROPERTIES_EMAIL_CONTEXT_ACCOUNT_ACTIVATE
     */
	public static final String PROPERTIES_EMAIL_CONTEXT_ACCOUNT_ACTIVATE = "msg_context_account_activate";

    /**
     * NUMBER_MAX_ERROR_LOGIN
     */
	public static final int NUMBER_MAX_ERROR_LOGIN = 5;

    /** 后台会员登录成功 */
	public static final String ADMINLOGIN_SUCCESS = "adminuser";

    /** 前台会员登录成功 */
	public static final String SESSION_USER = "session_user";
    
    /** 个人中心左菜单显示状态显示*/    
	public static final String BORROW_APPLY="borrow_apply";
    /** 返回http状态码 请求成功 */
	public static final String HTTP_STATUSCODE_SUCCESS = "200";

    /** 返回http状态码 请求错误 */
	public static final String HTTP_STATUSCODE_ERROR = "300";

    /** 返回http状态码 session失效 */
	public static final String HTTP_STATUSCODE_TIME_OUT = "301";

    /**
     * 有关平台的一些状态定义
     * 0*/
	public static final Integer STATUES_ZERO = 0;
    
    /**
     * 有关平台的一些状态定义
     *  1*/
	public static final Integer STATUES_ONE = 1;
    
    /**
     * 有关平台的一些状态定义
     *  2*/
	public static final Integer STATUES_TWO = 2;
    
    /**
     * 有关平台的一些状态定义
     *  3*/
	public static final Integer STATUES_THERE = 3;
   
    /**
     * 有关平台的一些状态定义
     * 4
     */
	public static final Integer STATUES_FOUR = 4;
    
    /**
     * 有关平台的一些状态定义
     * 5
     */
	public static final Integer STATUES_FIVE = 5;
    /**有关平台的一些状态定义
     * 6
     */
	public static final Integer STATUES_SIX = 6;
    /**有关平台的一些状态定义
     * 7*/
	public static final Integer STATUES_SEVEN = 7;
    /**有关平台的一些状态定义
     * 8
     * */
	public static final Integer STATUES_EIGHT = 8;
    /**有关平台的一些状态定义
     * 9
     * */
	public static final Integer STATUES_NINE = 9;
    /**有关平台的一些状态定义
     * 10
     * */
	public static final Integer SRSRUES_TEN = 10;
    /**有关平台的一些状态定义
     * 11
     * */
	public static final Integer SRSRUES_ELEVEN = 11;

    /** 逾期利息(该逾期利息由平台定义)*/
	public static final Double OVERDUE_INTEREST = 0.02;

    /**产品的计算公式*/
	public static final String FORMULA = "((投资金额*客户年化收益率)/365)*期限(天)";

    /**短信失效毫秒数*/
	public static final Long MILLISECONDS = 2 * 60 * 1000l;
    
    /**转账*/
    public static final String TRANSFER="Transfer";
    /**银行列表*/
    public static final String GET_BANK_LIST="GetBankList";
    /**账户余额查询*/
    public static final String QUERY_FOR_ACCBALANCE="QueryForAccBalance";
    /**解冻保证金*/
    public static final String GUARANTEE_UNFREEZE="GuaranteeUnfreeze";
    /**查询托管用户信息**/
    public static final String QUERY_MER_USER_INFO="QueryMerUserInfo";
    /**
     * 前台环讯返回地址
     */
    public static final String WEB_URL = ParameterIps.getWeburl();
    
    /** 注册宝付 */
	/** sdk开通宝付异步接口*/
	public static String PC_OPEN_BAOFU_ASYNC_URL = WEB_URL + "baofuaccount/asyncOpenBaoFuPage";
	
	/** 宝付开通成功接口 */
	public static final String PC_OPEN_BAOFU_ACCOUNT_SUCCESS_URL = WEB_URL + "baofuaccount/baofooSuccess";
    
    /**充值*/
    public static final String RECHARGEURL = WEB_URL+"processing/recharge.htm";
    public static final String ASYNCHRONISMRECHARGE = WEB_URL+ "processing/asynchronismRecharge.htm";
    
    /**提现*/
    public static final String WITHDRAWAL = WEB_URL+"processing/withdrawal.htm";
    public static final String WITHDRAWASYNCHRONOUS = WEB_URL+"processing/withdrawAsynchronous.htm";

    /**用户注册*/
    public static final String REGISTRATION = WEB_URL+"processing/registration.htm";
    public static final String ASYNCHRONISMREGISTRATION = WEB_URL+"processing/asynchronismRegistration.htm";
    
    /**绑定银行卡*/
    public static final String OPBANKCARD = WEB_URL+"processing/opBankCard.htm";
    public static final String ASYNOPBANKCARD = WEB_URL+"processing/asynOpBankCard.htm";
    
    /**还款*/
    public static final String REPAYMENT = WEB_URL+"processing/repayment.htm";
    public static final String REPAYMENTASYNCHRONOUS=WEB_URL+"processing/repaymentAsynchronous.htm";
    
    /**项目投标*/
    public static final String BID = WEB_URL+"processing/returnLoanBid.htm";
    public static final String ASYNCHRONISMBID = WEB_URL+"processing/asynchronismLoanBid.htm";
    
    /**service第三方担保注册宝付授权**/
    public static final String INACCREDIT=WEB_URL+"processing/returnInaccredit.htm";
    public static final String ASYNCHRONISMINACCREDIT=WEB_URL+"processing/asynchronismInaccredit.htm";
    
    /***用户注册宝付授权**/
    public static final String INACCREDITUSRE=WEB_URL+"processing/returnInaccreditUser.htm";
    public static final String ASYNCHRONISMINACCREDITUSER=WEB_URL+"processing/asynchronismInaccreditUser.htm";
    /***用户注册宝付授权**/
    public static final String H5INACCREDITUSRE=WEB_URL+"h5/baofooSuccess.htm";
    public static final String H5ASYNCHRONISMINACCREDITUSER=WEB_URL+"processing/h5asynchronismInaccreditUser.htm";
    /**
     * 项目满标页面跳转
     */
    public static final String LOANCREDIT = WEB_URL+"processing/returnLoanCredit.htm";
    /**
     * 项目满标异步处理
     */
    public static final String ASYNCHRONISMLOANCREDIT=WEB_URL+"processing/asynchronismLoanCredit.htm";
    
    /**项目流标*/
    public static final String RETURNLOANFLOW = WEB_URL+"processing/returnLoanFlow.htm";
    /**项目流标异步*/
    public static final String ASYNCHRONISMLOANFLOW = WEB_URL+"processing/asynchronismLoanFlow.htm";
    
    /**项目还款page_url*/
    public static final String REPAYMENT_SIGN=WEB_URL+"processing/returnRepaymentSign.htm";
    /**项目还款server_url*/
    public static final String REPAYMENT_SIGN_ASYNCHRONOUS=WEB_URL+"processing/asynchronismRepaymentSign.htm";
    
    /**店铺还款page_url*/
    public static final String ProREPAYMENT_SIGN=WEB_URL+"processing/returnProRepaymentSign.htm";
    /**店铺还款server_url*/
    public static final String ProREPAYMENT_SIGN_ASYNCHRONOUS=WEB_URL+"processing/asynchronismProRepaymentSign.htm";
    
    /**会员升级page_url*/
    public static final String TransferUrl=WEB_URL+"processing/returnTransferSign.htm";
    /**会员升级server_url*/
    public static final String TransferUrl_SIGN=WEB_URL+"processing/asynchronismTransferSign.htm";
    
    /***** 下面的没用到***/
    /**债权转让放款*/
    public static final String LOANSASSIGNMENT = WEB_URL+"processing/loansAssignment.htm";    
    /**发布*/
    public static final String REGISTER_SUBJECT=WEB_URL+"baseLoanSign/pubback.htm";
    public static final String REGISTER_SUBJECT_ASYNCHRONOUS=WEB_URL+"processing/pubback.htm";
    
    /**自动投标规则*/
    public static final String AUTOMATIC=WEB_URL+"plank/returnAutomatic.htm";
    public static final String ASYNCHRONISMAUTOMATIC = WEB_URL+"processing/asynchronismAutomatic.htm";

    /**解冻保证金*/
    public static final String GUARANTEE_UNFREEZE_ASYNCHRONOUS=WEB_URL+"processing/guaranteeUnfreezeAsynchronous";
    
    /**
     * 债权匹配锁定时间
     */
    public static final int TIME_CREDITOR_LOCK_MATCH = 2;//分钟
    
    /**
     * 债权购买锁定时间
     */
    public static final int TIME_CREDITOR_LOCK_PAY = 20;//分钟
    
    /**
     * WEBURL
     */
    public static final String WEBURL = WEB_URL+"WEB-INF/operating.jsp";
    
    /**
     * PROJECT_NAME
     */
    public static final String PROJECT_NAME = "前海红筹";
    /**
     * SYSTEM_EXCEPTION_RECEIVE
     */
    public static final String[] SYSTEM_EXCEPTION_RECEIVE = {"chengyc@hc9.com"};
    
    /**
     * 后台环讯回调地址
     */
    /**后台债权人注册*/
    public static final String REGISTRATIONBACKSTAGE = WEB_URL+"processing/ipsCallback.htm";
    /**后台债权人充值*/
    public static final String RECHARGEURLBACKSTAGE = WEB_URL+"processing/rechargeProessing.htm";
    /**后台债权人提现*/
    public static final String WITHDRAWALBACKSTAGE = WEB_URL+"processing/withdrlwalProessing.htm";
    /**
     * 用来判断是否通过安全验证
     */
    public static final String SECURITY_VERIFIY = "security_verifiy";
    /**
     * 前台充值记录中的列表分页大小
     */
    public static final int PAGE_SIZE_RECHARGE_RECORD = 10;
    
    public static final String NEW_BID="新标上线";
    public static final String END_BID="标的结束";
    
    /**成功*/
    public static final int RESULT_SUCCESSFUL = 0;
    /**失败*/
    public static final int RESULT_FAIL = 1;
    
    /**每年按360天计算*/
    public static final int YEAR=360;
    /**每月按30天计算*/
    public static final int MONTH=30;
    
    /** 红包抽奖相关金额信息:红包奖品：4:￥5; 5：￥7； 6：￥9；7：￥19；8：￥29；9：￥50； */
	private static Map<Integer, Double> redEnvMap = new HashMap<Integer, Double>();
	
	/** 红包最低使用门槛 */
	private static Map<Integer, Long> redEnvLowestRequireMap = new HashMap<Integer, Long>();
	
	static {
		redEnvMap.put(4, 5.00);
		redEnvMap.put(5, 7.00);
		redEnvMap.put(6, 9.00);
		redEnvMap.put(7, 19.00);
		redEnvMap.put(8, 29.00);
		redEnvMap.put(9, 50.00);
		
		redEnvLowestRequireMap.put(5, 1000L);
		redEnvLowestRequireMap.put(7, 5000L);
		redEnvLowestRequireMap.put(9, 10000L);
		redEnvLowestRequireMap.put(10, 10000L);
		redEnvLowestRequireMap.put(19, 20000L);
		redEnvLowestRequireMap.put(20, 20000L);
		redEnvLowestRequireMap.put(25, 25000L);
		redEnvLowestRequireMap.put(29, 30000L);
		redEnvLowestRequireMap.put(30, 30000L);
		redEnvLowestRequireMap.put(35, 35000L);
		redEnvLowestRequireMap.put(40, 40000L);
		redEnvLowestRequireMap.put(45, 45000L);
		redEnvLowestRequireMap.put(50, 50000L);
		redEnvLowestRequireMap.put(60, 60000L);
		redEnvLowestRequireMap.put(70, 70000L);
		redEnvLowestRequireMap.put(80, 80000L);
		redEnvLowestRequireMap.put(100, 150000L);
		redEnvLowestRequireMap.put(120, 180000L);
		redEnvLowestRequireMap.put(150, 220000L);
	}
	
	/** 获取抽奖所送红包的对应金额 */
	public static Double getRedEnvMoney(Integer redEnvKey) {
		return redEnvMap.get(redEnvKey);
	}
	
	/** 获取红包的最低使用金额 */
	public static Double getRedEnvLowestRequireMoney(Double redEvnMoney) {
		BigDecimal bigRedEvnMoney = new BigDecimal(redEvnMoney);
		BigDecimal big = new BigDecimal(redEnvLowestRequireMap.get(bigRedEvnMoney.intValue()));
		return big.doubleValue();
	}
	/**	用户注册 */
	public static final int USER_REG=1;
	/**	用户投资 */
	public static final int USER_INVEST=2;
}
