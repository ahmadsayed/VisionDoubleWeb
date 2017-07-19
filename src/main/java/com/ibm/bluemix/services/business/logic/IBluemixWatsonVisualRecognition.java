package com.ibm.bluemix.services.business.logic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;

public interface IBluemixWatsonVisualRecognition {

	List<VisualClass> analyzeImg(File frameFile);
	
	public void rebuildClassifier(File negative, File... files);

	void reTrainClassifier(InputStream negativeExamples, List<String> positiveExamples) throws IOException;

	boolean isClassifierReady();
}
