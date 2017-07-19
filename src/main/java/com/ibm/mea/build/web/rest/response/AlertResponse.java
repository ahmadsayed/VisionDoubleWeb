package com.ibm.mea.build.web.rest.response;

import java.util.List;

import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;

public class AlertResponse extends AbstractRestResponse{

	private List<VisionDoubleEvent> alerts;

	public List<VisionDoubleEvent> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<VisionDoubleEvent> alerts) {
		this.alerts = alerts;
	}
	
}
