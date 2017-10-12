package com.hc9.web.main.vo;

/** 公告列表相关vo辅助类 */
public class MsgVo {
	/** 文章主键id */
	private long id;
	
	/** 文章标题 */
	private String title;
	
	/** 公告内容 */
	private String content;
	
	/** 创建时间 */
	private String createTime;
	
	/** 是否已读（0未读 1已读） */
	private int isread;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getIsread() {
		return isread;
	}

	public void setIsread(int isread) {
		this.isread = isread;
	}
	
}