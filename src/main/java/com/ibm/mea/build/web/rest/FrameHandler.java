package com.ibm.mea.build.web.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;

import com.ibm.bluemix.services.business.logic.IAlertHandler;
import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.IBluemixWatsonVisualRecognition;
import com.ibm.bluemix.services.business.logic.impl.EventRulesEnum;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.UserConfiguration;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.bluemix.services.business.logic.impl.vision_double.DummyHandler;
import com.ibm.bluemix.services.business.logic.impl.vision_double.NotifyHandler;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;

public class FrameHandler {
	
	public VisionDoubleEvent handleFile(File frameFile, String fileName, String cameraId, String evtTime) throws Exception {
		try {
			System.err.println("Start handle frame with file name : "+fileName);
			
			VisionDoubleEvent event = new VisionDoubleEvent();
			event.setCameraId(cameraId);
			event.setFrameTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(evtTime).getTime());
			FaceIdentification identifier = new FaceIdentification();			
			IBluemixWatsonVisualRecognition visualRecognition = BusinessLogicFactory.getBluemixWatsonVisualRecognition();
			List<VisualClass> imgClasses = visualRecognition.analyzeImg(frameFile);
			event.setVisualRecognitionClasses(imgClasses);
			List<VisualClass> faceIdentificationClasses = null;
			if(isPeople(imgClasses))
			{

				List<VisualClassification> faceIdentificationResult = identifier.getAllFaces(frameFile);
				if(faceIdentificationResult != null)
				{
					if(
							faceIdentificationResult.size() > 0 &&
							faceIdentificationResult.get(0).getImages().size() > 0 &&
							faceIdentificationResult.get(0).getImages().get(0).getClassifiers().size() >0)
					{
						
						faceIdentificationClasses = faceIdentificationResult.get(0).getImages().get(0).getClassifiers().get(0).getClasses();
						event.setAllFIClasses(faceIdentificationClasses);
						event.setFaceIdentificationClasses(getHighestScoreIdentification(faceIdentificationClasses));
					} else
					{
						event.setFaceIdentificationClasses(new ArrayList<VisualClass>());
					}
				}
			}
			
			IAlertHandler handler = null;
			IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
			UserConfiguration cnfg = cloudant.getUserConfig(cameraId);
			if(cnfg != null)
			{
				String userAlertRule = decideAlertRule(cnfg, imgClasses, faceIdentificationClasses);
				if(userAlertRule != null){
					event.setRule(userAlertRule);
					
					if(userAlertRule.equals(EventRulesEnum.ALERT.getCode()) || userAlertRule.equals(EventRulesEnum.IGNORE.getCode()))
					{
						handler = new NotifyHandler();
					} else {
						handler = new DummyHandler();
					}
				} else{
					event.setRule(EventRulesEnum.UNDEFINED.getCode());
					handler = new NotifyHandler();
				}
			} else{
				event.setRule(EventRulesEnum.UNDEFINED.getCode());
				handler = new NotifyHandler();
			}
			InputStream fileInputStream = null;
			try{
				fileInputStream = new FileInputStream(frameFile);
				if(isPeople(imgClasses)) {
					handler.handle(fileName, new ByteArrayInputStream(identifier.cropFace(frameFile)), event, cnfg.getPhoneNumber(), cnfg.getLocation());
				} else {
					handler.handle(fileName, fileInputStream, event, cnfg.getPhoneNumber(), cnfg.getLocation());
				}
				//TODO_HA serializer to not to return the unneeded event info to the caller (e.g. vr classes)
				System.err.println("End handle frame with file name : "+fileName);
				return event;
			} catch (WebApplicationException e) {
				e.printStackTrace();
				throw e;
//		        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
			} finally {
				if(fileInputStream != null)
					fileInputStream.close();
			}
		} catch(IOException ioe){
			ioe.printStackTrace();
			throw ioe;
//			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ioe).build();
		} catch(Exception e)
		{
			e.printStackTrace();
			throw e;
//			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
		}
	}
	
	private List<VisualClass> getHighestScoreIdentification(List<VisualClass> classes) {
		List<VisualClass> highestClassList = new ArrayList<VisualClass>();
		
		VisualClass highestClass = null;
		Double highestScore = 0.0;
		for (VisualClass visualClass : classes) {
			if(visualClass.getScore() > highestScore)
			{
				highestClass = visualClass;
				if (visualClass.getName().indexOf(".") > 0)
					highestClass.setName(visualClass.getName().substring(0, visualClass.getName().lastIndexOf(".")));
				highestScore = visualClass.getScore();
			}
		}
		
		highestClassList.add(highestClass);
		return highestClassList;
	}
	
	private String decideAlertRule(UserConfiguration cnfg, List<VisualClass> defaultClasses, List<VisualClass> faceIdentificationClasses) {
	if(faceIdentificationClasses != null)
	{
		String fiRule = decideFaceIdentificationClassesAlertRule(cnfg, faceIdentificationClasses);
		if(!fiRule.equals("-1"))
			return fiRule;
	}
	return decideDefaultClassesAlertRule(cnfg, defaultClasses);
}
	
	private String decideDefaultClassesAlertRule(UserConfiguration cnfg, List<VisualClass> defaultClasses) {
		String rule = "-1";
		
		List<VisualClass> highClassesList = new ArrayList<VisualClass>();
		for (VisualClass visualClass : defaultClasses) {
			if(visualClass.getScore() > 0.5)
			{
				highClassesList.add(visualClass);
			}
		}
		for (VisualClass visualClass : highClassesList) {
			String userConfiguredRule = null;
			for (Entry<String, String> userRule : cnfg.getEventsRules().entrySet()) {
				if(userRule.getKey().equalsIgnoreCase(visualClass.getName()))
					userConfiguredRule = userRule.getValue();
			}
			
			if(userConfiguredRule != null && userConfiguredRule.equals(EventRulesEnum.ALERT.getCode()))
				return EventRulesEnum.ALERT.getCode();
			else if(userConfiguredRule != null && userConfiguredRule.equals(EventRulesEnum.IGNORE.getCode()))
				rule = EventRulesEnum.IGNORE.getCode();
		}
		return rule;
	}
	
	private String decideFaceIdentificationClassesAlertRule(UserConfiguration cnfg, List<VisualClass> faceIdentificationClasses) {
		String rule = "-1";
		
		List<VisualClass> highClassesList = new ArrayList<VisualClass>();
		for (VisualClass visualClass : faceIdentificationClasses) {
			if(visualClass.getScore() > 0.5)
			{
				highClassesList.add(visualClass);
			}
		}
		for (VisualClass visualClass : highClassesList) {
			String userConfiguredRule = null;
			for (Entry<String, String> userRule : cnfg.getEventsRules().entrySet()) {
				if(userRule.getKey().equalsIgnoreCase(visualClass.getName()))
					userConfiguredRule = userRule.getValue();
			}
			
			if(userConfiguredRule != null && userConfiguredRule.equals(EventRulesEnum.ALERT.getCode()))
				return EventRulesEnum.ALERT.getCode();
			else if(userConfiguredRule != null && userConfiguredRule.equals(EventRulesEnum.IGNORE.getCode()))
				rule = EventRulesEnum.IGNORE.getCode();
		}
		return rule;
	}
	
//	private String decideAlertRule(UserConfiguration cnfg, List<VisualClass> defaultClasses, List<VisualClass> faceIdentificationClasses) {
//		String rule = "-1";
//		
//		List<VisualClass> mergedClasses = new ArrayList<VisualClass>();
//		mergedClasses.addAll(defaultClasses);
//		if(faceIdentificationClasses != null)
//			mergedClasses.addAll(faceIdentificationClasses);
//		
//		List<VisualClass> highClassesList = new ArrayList<VisualClass>();
//		for (VisualClass visualClass : mergedClasses) {
//			if(visualClass.getScore() > 0.5)
//			{
//				highClassesList.add(visualClass);
//			}
//		}
//		for (VisualClass visualClass : highClassesList) {
//			String userConfiguredRule = null;
//			for (Entry<String, String> userRule : cnfg.getEventsRules().entrySet()) {
//				if(userRule.getKey().equalsIgnoreCase(visualClass.getName()))
//					userConfiguredRule = userRule.getValue();
//			}
//			
//			if(userConfiguredRule != null && userConfiguredRule.equals(EventRulesEnum.ALERT.getCode()))
//				return EventRulesEnum.ALERT.getCode();
//			else if(userConfiguredRule != null && userConfiguredRule.equals(EventRulesEnum.IGNORE.getCode()))
//				rule = EventRulesEnum.IGNORE.getCode();
//		}
//		return rule;
//	}

	private boolean isPeople(List<VisualClass> imgClasses) {
		for (VisualClass visualClass : imgClasses) {
			if(visualClass.getName().equalsIgnoreCase("people") || visualClass.getName().equalsIgnoreCase("person"))
				return true;
		}
		return false;
	}

}
