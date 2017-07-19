package com.ibm.bluemix.services.business.logic.impl.cloudant;

public class Associate {

	private String associateName;
	
	private AssociateStatusEnum associateStatus;
	
	private int associateEventsNo;

	public String getAssociateName() {
		return associateName;
	}

	public void setAssociateName(String associateName) {
		this.associateName = associateName;
	}

	public AssociateStatusEnum getAssociateStatus() {
		return associateStatus;
	}

	public void setAssociateStatus(AssociateStatusEnum associateStatus) {
		this.associateStatus = associateStatus;
	}

	public int getAssociateEventsNo() {
		return associateEventsNo;
	}

	public void setAssociateEventsNo(int associateEventsNo) {
		this.associateEventsNo = associateEventsNo;
	}
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof Associate)
        {
            return this.associateName.equals(((Associate) object).getAssociateName());
        }
        return false;
    }

}
