package com.hc9.web.main.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hc9.web.main.redis.sys.vo.ArticleFullVo;
import com.hc9.web.main.redis.sys.vo.DeputysectionVo;
import com.hc9.web.main.redis.sys.vo.TopicVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;
import com.hc9.web.main.service.article.ColumnManageService;
import com.hc9.web.main.vo.PageModel;
import com.jubaopen.commons.LOG;

/**
 * 前台栏目管理
 * 
 * @author My_Ascii
 * 
 */
@Controller
@RequestMapping("to")
public class PageUrlController {

	/**
	 * 注入ColumnManageService
	 */
	@Resource
	ColumnManageService columnservice;
	

	/**
	 * 每页的条数
	 */
	private final int COUNT_PER_PAGE=10;
	/**
	 * @param page
	 *            PageModel
	 * @param request
	 *            HttpServletRequest
	 * @return 返回页面
	 */
	@RequestMapping("*.htm")
	public String queryByUrl(PageModel page, HttpServletRequest request,Integer no) {
		return forward(request, page, no);
	}


	/**
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param url
	 *            栏目路径
	 * @return String
	 */
	public String forward(HttpServletRequest request, PageModel page, Integer no) {
		
		String requestURI = request.getServletPath();// 获取请求的路径
		String param = requestURI.substring(4, requestURI.indexOf(".htm"));// 截取参数部分
		String[] params = param.split("-");// 用“-”将参数分割【param1：类似；param2：一级栏目id；param3：二级栏目id；param4：文章id】
		String  jspPage=null;
		//获取缓存数据
		//一级菜单
		List<TopicVo> topics=WebCacheManagerUtil.getTopicsFromRedis();
		if(topics==null || topics.size()<1){
			topics=columnservice.resetTopicToRedis();
		}
		//二级菜单或单页
		List<DeputysectionVo> deputysections=WebCacheManagerUtil.getDeputysectionFromRedis();
		if(deputysections==null || deputysections.size()<1){
			deputysections=columnservice.resetDeputysectionToRedis();
		}
		//文章
		List<ArticleFullVo> articles=WebCacheManagerUtil.getArticleFullVoFromRedis();
		if(articles==null || articles.size()<1){
			articles=columnservice.resetArticleToRedis();
		}
		

		request.setAttribute("topicId", Long.parseLong(params[1].toString()));// 一级栏目id
		request.setAttribute("deputyId", Long.parseLong(params[2].toString()));// 二级栏目id
		request.setAttribute("deputy", getBreadcrumbNavigation(deputysections,Long.parseLong(params[2])));// 面包屑导航
		try{
		// 列表		
		if (params[0].equals("list")) {
			if (no != null) {
				page.setPageNum(no);
			}else{
				no=1;
			}
			//当前二级菜单下的文章
			List<ArticleFullVo> currentDeputysectionArticles= getArticleList(articles,Long.parseLong(params[2].toString()));
			
			//每页的文章最后一条在数组中的位置，如果超过数组的容量，则数组的容量-1
			int end=(no*COUNT_PER_PAGE-1)>currentDeputysectionArticles.size()?currentDeputysectionArticles.size():(no*COUNT_PER_PAGE-1);
			
			//当前分页的文章
			List<ArticleFullVo> currentPage=currentDeputysectionArticles.subList((no-1)*COUNT_PER_PAGE, end);
			page.setTotalCount(currentDeputysectionArticles.size());
			page.setList(currentPage);
			
			//左侧菜单缓存
			List<DeputysectionVo> menu=getNaviMenu(deputysections,2);
			
			request.setAttribute("menu", menu);
			request.setAttribute("type", "list");
			request.setAttribute("page", page);
			jspPage= "WEB-INF/views/hc9/common/list_page";
		}else{
			List<DeputysectionVo> singlePages=getSingleList(deputysections,Long.valueOf(params[1]));
			request.setAttribute("menu", singlePages);
			//单页
			if (params[0].equals("single")) {
				request.setAttribute("type", "single");
			}
			
			// 文章
			if (params[0].equals("article")) {
				ArticleFullVo article = getArticle(articles,Long.parseLong(params[3]));
				request.setAttribute("article", article);
				request.setAttribute("type", "article");
				request.setAttribute("deputys", singlePages);
			}
			jspPage= "WEB-INF/views/hc9/common/single_page";
			
		}
		}catch (Exception e){
			LOG.error("新闻公告出错",e);
		}
		return jspPage;
	}


	/**
	 * 面包屑导航条
	 * @param deputysections
	 * @param parseLong
	 * @return
	 */
	private DeputysectionVo getBreadcrumbNavigation(List<DeputysectionVo> deputysections, long deputyId) {
		DeputysectionVo deputy=null;
		for(DeputysectionVo vo:deputysections){
			if(vo.getId()==deputyId){
				deputy=vo;
				break;
			}
		}
		return deputy;
	}


	/**
	 * 获取左侧的导航菜单
	 * @param deputysections
	 * @param type 类型:1单页，2列表页
	 * @return
	 */
	private List<DeputysectionVo> getNaviMenu(List<DeputysectionVo> deputysections,int type){
		List<DeputysectionVo> menu=new ArrayList<>();
		for(DeputysectionVo vo:deputysections){
			if(vo.getSectiontype()==type){
				menu.add(vo);
			}
		}
		return menu;
	}
	
	/**
	 * 返回文章列表
	 * @param articles
	 * @param type 所属的二级类型
	 * @return
	 */
	private List<ArticleFullVo> getArticleList(List<ArticleFullVo> articles,long type){
		List<ArticleFullVo> vos=new ArrayList<>();
		for(ArticleFullVo vo:articles){
			if(vo.getDeputysection_id()==type){
				vos.add(vo);
			}
		}
		return vos;
	}
	/**
	 * 单页文章
	 * @param listDeputys
	 * @param topicid 所属一级栏目
	 * @return
	 */
	private List<DeputysectionVo> getSingleList(List<DeputysectionVo> listDeputys, Long topicid) {
		List<DeputysectionVo> single=new ArrayList<>();
		for(DeputysectionVo vo:listDeputys){
			if(vo.getTopic()==topicid){
				single.add(vo);
			}
		}
		return single;
	}
	/**
	 * 指定id的文章
	 * @param articles
	 * @param parseLong
	 * @return
	 */
	private ArticleFullVo getArticle(List<ArticleFullVo> articles, long articleId) {
		ArticleFullVo article=new ArticleFullVo();
		for(ArticleFullVo vo:articles){
			if(vo.getId()==articleId){
				article=vo;
				break;
			}
		}
		return article;
	}

	
}
