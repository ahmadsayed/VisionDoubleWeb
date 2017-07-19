package com.ibm.bluemix.services.business.logic.impl.cloudant;

import java.util.List;
import java.util.Map;

public class UserConfiguration implements ICloudantObject{

	private String _id;
	
	private String _rev;
	//TODO_HA temp property to diff the profile objects than the events objects
	private boolean userConfig = true;
	
	private String userId;
	
	private String siteId;
	
	private String cameraId;
	
	private Map<String, String> eventsRules;
	
	private List<Associate> associates;
	
	private String userName;
	
	private String profileImageTitle;

	private String phoneNumber;
	
	private String location; 
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getProfileImageTitle() {
		return profileImageTitle;
	}

	public void setProfileImageTitle(String profileImageTitle) {
		this.profileImageTitle = profileImageTitle;
	}

	public List<Associate> getAssociates() {
		return associates;
	}

	public void setAssociates(List<Associate> associates) {
		this.associates = associates;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isUserConfig() {
		return userConfig;
	}

	public void setUserConfig(boolean userConfig) {
		this.userConfig = userConfig;
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

	public Map<String, String> getEventsRules() {
		return eventsRules;
	}

	public void setEventsRules(Map<String, String> eventsRules) {
		this.eventsRules = eventsRules;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

//	public Map<String, Object> toMap() {
//		Map<String, Object> user = new HashMap<String, Object>();
//		user.put("userId", this.getUserId());
//		user.put("siteId", this.getSiteId());
//		user.put("cameraId", this.getCameraId());
//		user.put("eventsRules", this.getEventsRules());
//		return user;
//	}
}
