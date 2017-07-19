package com.ibm.bluemix.services.business.logic.impl.visual_recognition;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;

public class VisualRecognitionMgr {

	private static VisualRecognition vision;

//	private static final String API_KEY = "27482ab3802aefa8daca8862b036554e13a8a00f";
	private static final String API_KEY = "53f88bce6a1604f9ab0e933dd3c9bd424fa93d45";  //Salma's
	
	public static VisualRecognition getVisualRecognition() {
		if (vision == null) {
			vision = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		    vision.setApiKey(API_KEY);
		}
		return vision;
	}

	private VisualRecognitionMgr() {
	}
}
