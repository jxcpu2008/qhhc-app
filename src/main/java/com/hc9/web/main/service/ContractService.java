package com.hc9.web.main.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.util.Constant;
import com.hc9.web.main.util.FreeMarkerUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

import freemarker.template.TemplateException;

/** 合同生成service */
@Service
public class ContractService {
    private static final int wdDoNotSaveChanges = 0;// 
    private static final int wdFormatPDF = 17;
    
    /**合同保存路径*/
    private String path="config/marker/contract/";

    private String contractTemplate;
    
    public void setTemplate(String fileName){
   	 this.contractTemplate = path + fileName+".ftl";
   }

    public boolean genContact(Map data,String path,String fileName) {
    	String fullFileName=path+"doc/"+fileName;
    	//1.生成doc
    	boolean hasWord=false;
    	hasWord=trans2Doc(fullFileName,data);
    	//2.doc转pdf
    	if(hasWord){
    		doc2Pdf(path,fileName);
    	}
    	return hasWord;
    }

	/** 加载模板生成doc文件 */
    public boolean trans2Doc(String fullFileName,Map map){
    	fullFileName +=".doc";
    	boolean flag=false;
    	try {
			FreeMarkerUtil.execute(this.contractTemplate,map,Constant.CHARSET_DEFAULT,fullFileName);
			flag= true;
		} catch (IOException e) {
			flag=false;
			LOG.error("文件读写出错！", e);
		} catch (TemplateException e) {
			LOG.error("模板加载出错！", e);
			flag=false;
		} catch (Exception e) {
			flag=false;
			LOG.error("生成doc出错！", e);
		}
    	return flag;
    }
    
    /** DOC转PDF */
    public void doc2Pdf(String path,String filename) {
    	ActiveXComponent xComponent = null;
    	try{
        	xComponent = new ActiveXComponent("Word.Application");
        	xComponent.setProperty("Visible", false);
        	Dispatch docs = xComponent.getProperty("Documents").toDispatch();
        	Dispatch doc = Dispatch.call(docs,"Open",path+"doc/"+filename+".doc",false,true).toDispatch();
        	File tofile = new File(path+"pdf/"+filename+".pdf");
        	if (tofile.exists()) {
    			tofile.delete();
    		}
        	Dispatch.call(doc,"SaveAs",	path+"pdf/"+filename+".pdf", wdFormatPDF);
        	Dispatch.call(doc, "Close", false);
    	}catch (Exception e) {
    		LOG.error("doc转pdf过程中出现错误！", e);
    	}finally {
			if (xComponent != null)
				xComponent.invoke("Quit", wdDoNotSaveChanges);
		}
	}
}