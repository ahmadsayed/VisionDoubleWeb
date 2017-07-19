package com.ibm.bluemix.services.business.logic;


public interface IVisionDouble {

	void classifyNewAssociate(String selectedFrameId, String newAssociateName, String camId);

	int classifyAs(String selectedFrameId, String newAssociateName, String camId);

	void startTrainForAssociate(String associateName, String camId);

	boolean ableToTrain(String camId);

}
