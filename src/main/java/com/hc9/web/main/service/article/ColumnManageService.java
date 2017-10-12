package com.hc9.web.main.service.article;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Article;
import com.hc9.web.main.entity.Deputysection;
import com.hc9.web.main.redis.IndexDataCache;
import com.hc9.web.main.redis.sys.vo.ArticleFullVo;
import com.hc9.web.main.redis.sys.vo.ArticleVo;
import com.hc9.web.main.redis.sys.vo.DeputysectionVo;
import com.hc9.web.main.redis.sys.vo.TopicVo;
import com.hc9.web.main.redis.sys.web.WebCacheManagerUtil;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.vo.PageModel;

/** 栏目管理 */
@Service
public class ColumnManageService {

	@Resource
	HibernateSupport commondao;

	/** 查询所有状态为显示的文章 */
	public List queryAllArticle(String did, PageModel page) {
		StringBuffer buf = new StringBuffer();
		StringBuffer bufcount = new StringBuffer();
		buf.append(" FROM article a,deputysection d WHERE a.deputysection_id=d.id and a.isShow = 1 "
				+ "and d.id = ? ORDER BY a.createTime DESC");
		bufcount.append("SELECT count(1) " + buf);
		buf.insert(0, "SELECT a.url,a.title,a.context,a.createTime ");

		page.setTotalCount(commondao.queryNumberSql(bufcount.toString(), did)
				.intValue());

		buf.append(" LIMIT ");
		buf.append(
				(page.getPageNum() - Constant.STATUES_ONE)
						* page.getNumPerPage()).append(",");
		buf.append(page.getNumPerPage());
		return commondao.findBySql(buf.toString(), Long.parseLong(did));
	}

	public List queryDecou(Long topId) {
		String sql = " FROM Deputysection a where a.isShow = 1 and a.topic.id=? order by a.topic.id,a.id asc";
		List list = commondao.find(sql, topId);

		return list;
	}

	/** 获取文章列表 */
	public List<Article> getArticleList(){
		String sql="SELECT * FROM article WHERE isShow=1 AND isRecommend=1 AND deputysection_id=? ORDER BY createTime DESC LIMIT 0,5";
		List<Article> articles = commondao.findBySql(sql, Article.class, 4);
		if(articles != null && articles.size() > 0) {
			List<ArticleVo> articleVoList = new ArrayList<ArticleVo>();
			for(Article article : articles) {
				ArticleVo vo = new ArticleVo();
				vo.setUrl(article.getUrl());
				vo.setTitle(article.getTitle());
				vo.setCreateTime(article.getCreateTime());
				articleVoList.add(vo);
			}
			WebCacheManagerUtil.setWebArticleListToRedis(articleVoList);
		}
		return articles;
	}
	
	/** 获取新闻公告2及目录 */
	public List<Deputysection> getDeputysectionList(){
		List<Deputysection> listDeputys = commondao
				.findBySql("SELECT * FROM deputysection  where isShow = ? and sectiontype_id =2 ORDER BY topic_id,id",Deputysection.class,1);
		String key="LIST:HC9:NEWS:NAVICATER:DEPUTY";
		IndexDataCache.set(key, listDeputys);
		return listDeputys;
	}

	/**
	 * 根据id查询二级栏目详情
	 * @param id 二级栏目id (二级栏目编号)
	 * @return 返回二级栏目
	 */
	public Deputysection queryDeputyById(long id) {
		return commondao.get(Deputysection.class, id);
	}

	/** 根据id查询文章详情 */
	public Article queryArticleById(long id) {
		return commondao.get(Article.class, id);
	}
	
	public List<TopicVo> resetTopicToRedis(){
		String sql="SELECT id,name,orderNum,url,pageTitle FROM topic WHERE isShow=?";
		List<Object[]> topics=commondao.findBySql(sql, 1);
		List<TopicVo> topicVos=new ArrayList<>();
		for(Object[] items:topics){
			TopicVo vo=new TopicVo();
			vo.setId(Long.parseLong(items[0].toString()));
			vo.setName(items[1].toString());
			vo.setOrderNum(items[2]==null?0:Integer.parseInt(items[2].toString()));
			vo.setUrl(items[3]==null?"":items[3].toString());
			vo.setPageTitle(items[4].toString());
			topicVos.add(vo);
		}
		
		WebCacheManagerUtil.setTopicsToRedis(topicVos);
		return topicVos;
	}
	
	public List<DeputysectionVo>  resetDeputysectionToRedis(){
		String sql="SELECT id,isRecommend,name,orderNum,pageHTML,pageTitile,url,sectiontype_id,topic_id from deputysection WHERE isShow=?";
		List<Object[]> vos=commondao.findBySql(sql, 1);
		List<DeputysectionVo> deputysectionVos=new ArrayList<>();
		for(Object[] items:vos){
			DeputysectionVo vo=new DeputysectionVo();
			vo.setId(Long.parseLong(items[0].toString()));
			vo.setIsRecommend(Integer.parseInt(items[1].toString()));
			vo.setName(items[2].toString());
			vo.setOrderNum(Integer.parseInt(items[3].toString()));
			vo.setPageHtml(items[4]==null?"":items[4].toString());
			vo.setPageTitile(items[5]==null?"":items[5].toString());
			vo.setUrl(items[6]==null?"":items[6].toString());
			vo.setSectiontype(Integer.parseInt(items[7].toString()));
			vo.setTopic(Long.parseLong(items[8].toString()));
			deputysectionVos.add(vo);
		}
		WebCacheManagerUtil.setDeputysectionToRedis(deputysectionVos);
		return deputysectionVos;
	}
	
	public List<ArticleFullVo> resetArticleToRedis(){
		String sql="SELECT id,deputysection_id,title,isRecommend,context,createTime,url FROM article WHERE isShow=? ORDER BY createTime DESC";
		List<Object[]> vos=commondao.findBySql(sql, 1);
		List<ArticleFullVo> articles=new ArrayList<>();
		for(Object[] items:vos){
			ArticleFullVo vo=new ArticleFullVo();
			vo.setId(Long.parseLong(items[0].toString()));
			vo.setDeputysection_id(Long.parseLong(items[1].toString()));
			vo.setTitle(items[2].toString());
			vo.setIsRecommend(Integer.parseInt(items[3].toString()));
			vo.setContext(items[4].toString());
			vo.setCreateTime(items[5].toString());
			vo.setUrl(items[6].toString());
			articles.add(vo);
		}
		WebCacheManagerUtil.setArticlesToRedis(articles);
		return articles;
	}
}
