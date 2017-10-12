package com.hc9.web.main.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 站内消息
 */
@Entity
@Table(name = "usermessage")
public class Usermessage implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 会员基本信息
     */
    private Userbasicsinfo userbasicsinfo;

    /**
     * 消息类容
     */
    private String context;

    /**
     * 是否已读（0未读、1已读）
     */
    private Integer isread;

    /**
     * 消息接收时间
     */
    private String receivetime;

    /**
     * 消息标题
     */
    private String title;
    
    private Userbasicsinfo sendUser;

    /** default constructor */
    public Usermessage() {
    }

    /**
     * 构造方法
     * 
     * @param userbasicsinfo
     *            会员基本信息
     * @param context
     *            消息类容
     * @param isread
     *            是否已读
     * @param receivetime
     *            接受时间
     * @param title
     *            标题
     */
    public Usermessage(Userbasicsinfo userbasicsinfo, String context,
            Integer isread, String receivetime, String title,Userbasicsinfo sendUser) {
        this.userbasicsinfo = userbasicsinfo;
        this.context = context;
        this.isread = isread;
        this.receivetime = receivetime;
        this.title = title;
        this.sendUser=sendUser;
    }

    /**
     * 主键
     * 
     * @return 主键
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Long getId() {
        return this.id;
    }

    /**
     * 主键
     * 
     * @param id
     *            主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 会员基本信息
     * 
     * @return 会员基本信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public Userbasicsinfo getUserbasicsinfo() {
        return this.userbasicsinfo;
    }

    /**
     * 会员基本信息
     * 
     * @param userbasicsinfo
     *            会员基本信息
     */
    public void setUserbasicsinfo(Userbasicsinfo userbasicsinfo) {
        this.userbasicsinfo = userbasicsinfo;
    }

    /**
     * 消息类容
     * 
     * @return 消息类容
     */
    @Column(name = "context", length = 300)
    public String getContext() {
        return this.context;
    }

    /**
     * 消息类容
     * 
     * @param context
     *            消息类容
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * 是否已读
     * 
     * @return 是否已读
     */
    @Column(name = "isread")
    public Integer getIsread() {
        return this.isread;
    }

    /**
     * 是否已读
     * 
     * @param isread
     *            是否已读
     */
    public void setIsread(Integer isread) {
        this.isread = isread;
    }

    /**
     * 接受时间
     * 
     * @return 接受时间
     */
    @Column(name = "receivetime", length = 30)
    public String getReceivetime() {
        return this.receivetime;
    }

    /**
     * 接受时间
     * 
     * @param receivetime
     *            接受时间
     */
    public void setReceivetime(String receivetime) {
        this.receivetime = receivetime;
    }

    /**
     * 标题
     * 
     * @return 标题
     */
    @Column(name = "title", length = 256)
    public String getTitle() {
        return this.title;
    }

    /**
     * 标题
     * 
     * @param title
     *            标题
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_id")
	public Userbasicsinfo getSendUser() {
		return sendUser;
	}

	public void setSendUser(Userbasicsinfo sendUser) {
		this.sendUser = sendUser;
	}
    
    

}