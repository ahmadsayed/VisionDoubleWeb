package com.ibm.bluemix.services.business.logic.impl.visual_recognition;

import java.util.List;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageText;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;

public class VisualRecognitionResult {

	private String url;
	
	private List<Face> faces;
	
	private List<VisualClassifier> keywords;
	
	private ImageText sceneText;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Face> getFaces() {
		return faces;
	}

	public void setFaces(List<Face> faces) {
		this.faces = faces;
	}

	public List<VisualClassifier> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<VisualClassifier> keywords) {
		this.keywords = keywords;
	}

	public ImageText getSceneText() {
		return sceneText;
	}

	public void setSceneText(ImageText sceneText) {
		this.sceneText = sceneText;
	}
}
