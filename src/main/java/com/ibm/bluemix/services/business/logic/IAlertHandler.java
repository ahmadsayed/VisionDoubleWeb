package com.ibm.bluemix.services.business.logic;

import java.io.InputStream;

import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;

public interface IAlertHandler {

	public boolean handle(String string, InputStream fileInputStream, VisionDoubleEvent alert, String phoneNumber, String location);
}
