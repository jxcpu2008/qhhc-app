package com.hc9.web.main.common.messagepush.ios;

import com.hc9.web.main.common.messagepush.IOSNotification;

public class IOSUnicast extends IOSNotification {
	
	public IOSUnicast(String appkey, String appMasterSecret, String deviceToken) throws Exception {
		setAppMasterSecret(appMasterSecret);
		setPredefinedKeyValue("appkey", appkey);
		setPredefinedKeyValue("type", "unicast");
		setPredefinedKeyValue("device_tokens", deviceToken);
	}
}