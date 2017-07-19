package com.ibm.bluemix.services.business.logic.impl.cloudant;

import java.util.List;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;

public class VisionDoubleEvent implements ICloudantObject {

	private String _id;

	private String _rev;

	private String frameId;

	private String frameTitle;

	private String userId;

	private String siteId;

	private String cameraId;

	private Long frameTime;

	private List<VisualClass> visualRecognitionClasses;
	
	private List<VisualClass> faceIdentificationClass;

	private boolean userConfig = false;

	private String classifiedAs;
	
	private String photoContent;
	
	public String getPhotoContent() {
		return photoContent;
	}

	public void setPhotoContent(String photoContent) {
		this.photoContent = photoContent;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_rev() {
		return _rev;
	}

	public void set_rev(String _rev) {
		this._rev = _rev;
	}

	public String getClassifiedAs() {
		return classifiedAs;
	}

	public void setClassifiedAs(String classifiedAs) {
		this.classifiedAs = classifiedAs;
	}

	public String getFrameTitle() {
		return frameTitle;
	}

	public void setFrameTitle(String frameTitle) {
		this.frameTitle = frameTitle;
	}

	public List<VisualClass> getVisualRecognitionClasses() {
		return visualRecognitionClasses;
	}

	public void setVisualRecognitionClasses(
			List<VisualClass> visualRecognitionClasses) {
		this.visualRecognitionClasses = visualRecognitionClasses;
	}

	private String rule;

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getFrameId() {
		return frameId;
	}

	public void setFrameId(String frameId) {
		this.frameId = frameId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getCameraId() {
		return cameraId;
	}

	public void setCameraId(String cameraId) {
		this.cameraId = cameraId;
	}

	public Long getFrameTime() {
		return frameTime;
	}

	public void setFrameTime(Long frameTime) {
		this.frameTime = frameTime;
	}

	public boolean isUserConfig() {
		return userConfig;
	}

	public void setUserConfig(boolean userConfig) {
		this.userConfig = userConfig;
	}
	public List<VisualClass> getFaceIdentificationClasses() {
		return faceIdentificationClass;
	}

	public void setFaceIdentificationClasses(List<VisualClass> vClass) {
		this.faceIdentificationClass = vClass;
	}

	List<VisualClass> allFIClasses;

	public List<VisualClass> getAllFIClasses() {
		return allFIClasses;
	}

	public void setAllFIClasses(List<VisualClass> allFIClasses) {
		this.allFIClasses = allFIClasses;
	}
	
	// public Map<String, Object> toMap() {
	// Map<String, Object> frame = new HashMap<String, Object>();
	// frame.put("frameId", this.getFrameId());
	// frame.put("userId", this.getUserId());
	// frame.put("siteId", this.getSiteId());
	// frame.put("cameraId", this.getCameraId());
	// frame.put("frameTime", this.getFrameTime());
	// // frame.put("visualEvent", this.getEventType().getVisualTypeAppId());
	// frame.put("visualClass", this.getVisualRecognitionClasses());
	// frame.put("rule", this.getRule());
	// return frame;
	// }
}
