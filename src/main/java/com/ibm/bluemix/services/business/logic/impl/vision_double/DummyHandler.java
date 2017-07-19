package com.ibm.bluemix.services.business.logic.impl.vision_double;

import java.io.InputStream;

import com.ibm.bluemix.services.business.logic.IAlertHandler;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;

public class DummyHandler implements IAlertHandler{

	@Override
	public boolean handle(String string, InputStream fileInputStream, VisionDoubleEvent alert, String phoneNumber, String location) {
		// TODO Auto-generated method stub
		return false;
	}


}
