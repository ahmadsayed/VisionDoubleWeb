package com.ibm.bluemix.services.business.logic.impl;


public enum EventRulesEnum {

	IGNORE("0", "Ignore"),
	ALERT("1", "Alert"),
	UNDEFINED("-1", "Undefined");
	
	private final String code;
	
	private final String text;
	
	EventRulesEnum(String code, String text){
		this.code = code;
		this.text = text;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getText() {
		return text;
	}

	public static EventRulesEnum getAlertRulesEnumByCode(String code){
		for(EventRulesEnum ruleEnum : values()){
	        if( ruleEnum.getCode().equals(code)){
	            return ruleEnum;
	        }
	    }
	    return null;
	}
	
	public static EventRulesEnum getAlertRulesEnumByText(String text){
		for(EventRulesEnum ruleEnum : values()){
	        if( ruleEnum.getText().equals(text)){
	            return ruleEnum;
	        }
	    }
	    return null;
	}
}
