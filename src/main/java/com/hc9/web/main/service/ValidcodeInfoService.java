package com.hc9.web.main.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hc9.web.main.common.hibernate.impl.HibernateSupport;
import com.hc9.web.main.entity.Validcodeinfo;

@Service
public class ValidcodeInfoService {

    @Resource
    private HibernateSupport commonDao;

    /** 得到用户的短信限制信息 */
    public Validcodeinfo getValidcodeinfoByUid(Long id){
        StringBuffer sb=new StringBuffer("select * from validcodeinfo where user_id=").append(id);
        List<Validcodeinfo> validList= commonDao.findBySql(sb.toString(), Validcodeinfo.class);
        return validList.size()>0?validList.get(0):null;
    }
    
    public void update(Validcodeinfo validcodeinfo){
        commonDao.update(validcodeinfo);
    }
}