package com.hc9.web.main.service.smsmail.emsms;

import org.springframework.stereotype.Service;

import com.hc9.commons.log.LOG;
import com.hc9.web.main.util.SingletonClient;

import cn.emay.sdk.client.api.Client;

@Service
public class EmaySms {

	private Client client;
	
	private final int Priority=3;//短信优先级
	
	public void init(String sn, String pwd, String key)
			throws Exception {
		client=SingletonClient.getClient(sn, key);
	}
	
	
	public Integer sendSMS(String content, String... telNos) throws Exception {
//		boolean sendFlag = false;
//		String concurrentLock = "STR:EMAY:SMS:SEND:CONCURRENT:LOCK";
//		/** 亿美软通短信发送注意事项：1、控制好我们系统发送短信的并发数； 2、发送短信是使用队列机制进行发送； */
//		boolean lockFlag = RedisHelper.isKeyExistSetWithExpire(concurrentLock, 3);
//		if(lockFlag) {//如果被锁定，说明有其它线程在发送短信
//			for(int i = 0; i < 10; i++) {//如果有其它线程在发送短信，最多重试获取锁12次，每次间隔500毫秒，如果还未获取到返回发送失败
//				Thread.sleep(500);
//				lockFlag = RedisHelper.isKeyExistSetWithExpire(concurrentLock, 3);
//				if(!lockFlag) {
//					sendFlag = true;
//					break;
//				}
//			}
//		} else {
//			sendFlag = true;
//		}
//		if(sendFlag) {
			int res = client.sendSMS(telNos, content, Priority);
//			RedisHelper.del(concurrentLock);
			LOG.error("亿美短信通道==短信发送结果..."+content);
			return res;
//		} else {
//			return -1;
//		}
	}
}