package com.ibm.bluemix.services.business.logic.impl.setup;

import java.util.HashMap;
import java.util.Map;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.ibm.bluemix.services.business.logic.impl.EventRulesEnum;
import com.ibm.bluemix.services.business.logic.impl.cloudant.CloudantClientMgr;
import com.ibm.bluemix.services.business.logic.impl.cloudant.UserConfiguration;

public class CloudantInstaller {
	
	public static void main(String[] args) {
		
		Map<String, String> eventsRules = new HashMap<>();
		eventsRules.put("PERSON", EventRulesEnum.ALERT.getCode());
		eventsRules.put("CAR", EventRulesEnum.ALERT.getCode());
		eventsRules.put("ANIMAL", EventRulesEnum.IGNORE.getCode());
		
		UserConfiguration userConfig = new UserConfiguration();
		userConfig.setUserConfig(true);
		userConfig.setCameraId("cam-01");
		userConfig.setSiteId("site-01");
		userConfig.setEventsRules(eventsRules);
		userConfig.setUserName("John Doe");
		userConfig.setProfileImageTitle("Profile_John Doe.png");
		
		Database db = null;
		try {
			db = CloudantClientMgr.getDB();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response r = db.save(userConfig);
		System.err.println("New configuration saved with id : " + r.getId());
	}
}
