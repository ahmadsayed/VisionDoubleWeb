package com.ibm.bluemix.services.business.logic.impl.cloudant;


public enum AssociateStatusEnum {

	TRAINED("TRAINED"),
	NOT_TRAINED("NOT_TRAINED");
	
	private final String statusTxt;
	
	AssociateStatusEnum(String statusTxt){
		this.statusTxt = statusTxt;
	}
	
	public String getstatusTxt() {
		return statusTxt;
	}

	AssociateStatusEnum getStatusEnum(String statusTxt){
		for(AssociateStatusEnum statusEnum : values()){
	        if( statusEnum.getstatusTxt().equals(statusTxt)){
	            return statusEnum;
	        }
	    }
	    return null;
	}
	
	@Override
	public String toString() {
		return this.statusTxt;
	}
}
