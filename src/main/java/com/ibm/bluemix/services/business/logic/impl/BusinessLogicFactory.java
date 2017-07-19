package com.ibm.bluemix.services.business.logic.impl;

import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.IBluemixObjectStorage;
import com.ibm.bluemix.services.business.logic.IBluemixWatsonVisualRecognition;
import com.ibm.bluemix.services.business.logic.IVisionDouble;
import com.ibm.bluemix.services.business.logic.impl.cloudant.BluemixCloudant;
import com.ibm.bluemix.services.business.logic.impl.object_storage.BluemixObjectStorage;
import com.ibm.bluemix.services.business.logic.impl.vision_double.VisionDouble;
import com.ibm.bluemix.services.business.logic.impl.visual_recognition.BluemixWatsonVisualRecognition;

public class BusinessLogicFactory {

	private static IBluemixObjectStorage bluemixObjectStorageBusiness;

	public static IBluemixObjectStorage getBluemixObjectStorageBusiness() {
		if (bluemixObjectStorageBusiness != null)
			return bluemixObjectStorageBusiness;
		return new BluemixObjectStorage();
	}

	private static IBluemixCloudant bluemixCloudantBusiness;

	public static IBluemixCloudant getBluemixCloudantBusiness() {
		if (bluemixCloudantBusiness != null)
			return bluemixCloudantBusiness;
		return new BluemixCloudant();
	}
	
	private static IBluemixWatsonVisualRecognition visualRecognition;
	
	public static IBluemixWatsonVisualRecognition getBluemixWatsonVisualRecognition() {
		if (visualRecognition != null)
			return visualRecognition;
		return new BluemixWatsonVisualRecognition();
	}

	private BusinessLogicFactory() {
	}

	private static IVisionDouble visionDouble;
	
	public static IVisionDouble getVisionDouble() {
		if (visionDouble != null)
			return visionDouble;
		return new VisionDouble();
	};
}
