package com.ibm.bluemix.services.business.logic.impl.vision_double;

import java.io.InputStream;

import javax.ws.rs.WebApplicationException;

import com.ibm.bluemix.services.business.logic.IAlertHandler;
import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.IBluemixObjectStorage;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.mea.build.web.sms.SMSSenderUtil;

public class NotifyHandler implements IAlertHandler{

	@Override
	public boolean handle(String fileName, InputStream fileInputStream, VisionDoubleEvent event, String phoneNumber, String location) throws WebApplicationException{
		IBluemixObjectStorage objectStorageBus = BusinessLogicFactory.getBluemixObjectStorageBusiness();
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		
		String frameId = objectStorageBus.storeObject(fileName, fileInputStream);
		event.setFrameId(frameId);
		event.setFrameTitle(fileName);	
		
		cloudant.create(event);
		
		try {
			if (location == null) {
				location = "your home";
			}
			if (event.getFaceIdentificationClasses() != null && event.getFaceIdentificationClasses().isEmpty()) {
				SMSSenderUtil.sendSMS(phoneNumber, "Alert ... Unknown person at "+ location);
			} else if (event.getFaceIdentificationClasses() != null ){
				if (!event.getFaceIdentificationClasses().isEmpty()) {
					SMSSenderUtil.sendSMS(phoneNumber, "Alert ... "+ event.getFaceIdentificationClasses().get(0).getName()+" at " + location);
				} else {
					SMSSenderUtil.sendSMS(phoneNumber, "Alert ... Unknown person at "+ location);
				}
			}
		} 
//		catch (MalformedURLException e) {
//			System.out.println(e.getMessage());
//		} 
//		catch (IOException e) {
//			System.out.println(e.getMessage());
//		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
}
