package com.hc9.web.main.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hc9.web.main.vo.PageModel;

public class StatisticsUtil {
	
	/** 组装分页模型对象 */
	public static PageModel comsitePageModel(String start, String limit) {
		PageModel page = new PageModel();
		// 每页显示条数
		if (StringUtil.isNotBlank(limit) && StringUtil.isNumberString(limit)) {
			page.setNumPerPage(Integer.parseInt(limit) > 0 ? Integer.parseInt(limit) : 20);
		} else {
			page.setNumPerPage(20);
		}
		// 计算当前页
		if (StringUtil.isNotBlank(start) && StringUtil.isNumberString(start)) {
			page.setPageNum(Integer.parseInt(start) / page.getNumPerPage() + 1);
		}
		return page;
	}
	
	/** 从 对象获取字符串 */
	public static String getStringFromObject(Object obj) {
		if(obj == null) {
			return "";
		}
		return (String)obj;
	}
	
	/** 从bigdecimal获取double类型的值 */
	public static Double getDoubleFromBigdecimal(BigDecimal big) {
		Double result = 0.0;
		if(big != null) {
			result = big.doubleValue();
		}
		return result;
	}
	
	/** 从 对象获取字符串 */
	public static Integer getIntegerFromObject(Object obj) {
		if(obj == null) {
			return 0;
		}
		return (Integer)obj;
	}
	
	/** 从 对象获取字符串 */
	public static long getLongFromBigInteger(Object obj) {
		if(obj == null) {
			return 0L;
		}
		BigInteger result = (BigInteger)obj;
		return result.longValue();
	}
	
	/** 根据部门id获取部门名称 */
	public static String queryDepartmentNameById(Integer id) {
		String result = "暂无";
		if(id != null) {
			
			if (id == 1) {
				result = "总裁办";
			} else if (id == 2) {
				result = "财务部";
			} else if (id == 3) {
				result = "行政部";
			} else if (id == 4) {
				result = "副总办";
			} else if (id == 5) {
				result = "运营中心";
			} else if (id == 6) {
				result = "培训部";
			} else if (id == 7) {
				result = "风控部";
			} else if (id == 8) {
				result = "IT部";
			} else if (id == 9) {
				result = "摄影部";
			} else if (id == 10) {
				result = "推广部";
			} else if (id == 11) {
				result = "项目部";
			} else if (id == 12) {
				result = "客服部";
			} else if (id == 13) {
				result = "事业一部";
			} else if (id == 14) {
				result = "事业二部";
			}else if (id == 15) {
				result = "离职员工";
			}
		}
		return result;
	}
	
	/**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(Long v1,Long v2,int scale){
        if(scale<0){
            throw new IllegalArgumentException(
                "要保留的小数位数必须是一个正整数或者0");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
}
