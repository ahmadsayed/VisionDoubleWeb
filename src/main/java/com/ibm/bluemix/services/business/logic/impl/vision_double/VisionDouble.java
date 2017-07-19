package com.ibm.bluemix.services.business.logic.impl.vision_double;

import java.util.ArrayList;

import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.IVisionDouble;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.Associate;
import com.ibm.bluemix.services.business.logic.impl.cloudant.AssociateStatusEnum;
import com.ibm.bluemix.services.business.logic.impl.cloudant.UserConfiguration;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.mea.build.web.rest.Constants;

public class VisionDouble implements IVisionDouble {
	
	@Override
	public int classifyAs(String selectedFrameId, String associateName, String camId) {
		updateEvent(selectedFrameId, associateName);
		return increaseAssociateImagesCount(associateName, camId);
	}
	
	private int increaseAssociateImagesCount(String associateName, String camId) {
		int imagesNo = 0;
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		
		UserConfiguration user = cloudant.getUserConfig(camId);
		for (Associate associate : user.getAssociates()) {
			if(associateName.equals(associate.getAssociateName()))
			{
				imagesNo = associate.getAssociateEventsNo() + 1;
				associate.setAssociateEventsNo(imagesNo);
			}
		}
		cloudant.update(user);
		return imagesNo;
	}

	@Override
	public void classifyNewAssociate(String selectedFrameId, String newAssociateName, String camId) {
		System.err.println("i will train "+selectedFrameId+" as a new associate : "+newAssociateName);
		
		try{
			updateUserAssociates(newAssociateName, camId);
			updateEvent(selectedFrameId, newAssociateName);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String updateEvent(String selectedFrameId, String newAssociateName){
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		
		VisionDoubleEvent event = cloudant.getEvent(selectedFrameId);
		if(event != null)
		{
			event.setClassifiedAs(newAssociateName);
		}
		cloudant.update(event);
		
		return event.getFrameTitle();
	}
	
	private void updateUserAssociates(String newAssociateName, String camId){
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		
		UserConfiguration user = cloudant.getUserConfig(camId);
		if(user.getAssociates() == null)
		{
			user.setAssociates(new ArrayList<Associate>());
		} 
		Associate associate = new Associate();
		associate.setAssociateName(newAssociateName);
		associate.setAssociateStatus(AssociateStatusEnum.NOT_TRAINED);
		associate.setAssociateEventsNo(1);
		user.getAssociates().add(associate);

		cloudant.update(user);
	}

	@Override
	public void startTrainForAssociate(String associateName, String camId) {
		System.err.println("I will create a thread to train the service on : "+associateName);
		Trainer trainer = new Trainer(associateName, camId);
		trainer.start();
	}

	@Override
	public boolean ableToTrain(String camId) {
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		
		UserConfiguration user = cloudant.getUserConfig(camId);
		if(user.getAssociates() == null || user.getAssociates().size() < 2)
		{
			return false;
		}
		int noAssociatesForTraining = 0;
		for (Associate associate : user.getAssociates()) {
			if(associate.getAssociateEventsNo() >= Constants.IMAGES_NO_TO_TRAIN)
				noAssociatesForTraining++;
		}
		return noAssociatesForTraining >= Constants.MIN_NO_ASSOCIATES_TO_TRAIN;
	}
}
