package com.ibm.bluemix.services.business.logic;

import java.util.List;

import com.cloudant.client.api.model.Response;
import com.ibm.bluemix.services.business.logic.impl.cloudant.ICloudantObject;
import com.ibm.bluemix.services.business.logic.impl.cloudant.UserConfiguration;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.mea.build.web.rest.dto.AlertSelector;


public interface IBluemixCloudant {

	Response create(ICloudantObject obj);
	
	UserConfiguration getUserConfig(String camId);
	
	List<VisionDoubleEvent> getAlerts(String selector);
	
	List<String> getUserAssociates(String string);

	Response update(ICloudantObject user);

	VisionDoubleEvent getEvent(String selectedFrameId);

	List<VisionDoubleEvent> getEventsForAssociate(String associate);

	List<VisionDoubleEvent> getAssociateEvents(String selectedAssociate, Object fromDate, Object toDate);
	
	List<VisionDoubleEvent> getEventsBySelector(AlertSelector selector, String camId);

	void confgureRule(String selectedAssociate, String rule, String camId);

	void removeRule(String selectedAssociate, String camId);

	String getAssociateRule(String selectedAssociate, String camId);
}
