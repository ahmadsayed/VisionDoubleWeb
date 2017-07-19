package com.ibm.mea.build.web.rest;

public class User {
	
	private String userName;
	
	private String profileImageTitle;

	public User(String name, String profileImageTitle){
		this.profileImageTitle = profileImageTitle;
		this.userName = name;
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
}
