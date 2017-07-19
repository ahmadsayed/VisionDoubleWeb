package com.ibm.bluemix.services.business.logic.impl.vision_double;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.IBluemixObjectStorage;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.Associate;
import com.ibm.bluemix.services.business.logic.impl.cloudant.AssociateStatusEnum;
import com.ibm.bluemix.services.business.logic.impl.cloudant.UserConfiguration;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.mea.build.web.rest.Constants;

public class Trainer extends Thread {
	
	private String associateName;
	
	private String camId;
	
	Trainer(String associateName, String camId){
		this.associateName = associateName;
		this.camId = camId;
	}
	
	public void run() {
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		UserConfiguration user = cloudant.getUserConfig(this.camId);
		List<String> trainedAssociates = new ArrayList<String>();
		
		for (Associate associate : user.getAssociates()) {
			if(associate.getAssociateStatus().getstatusTxt().equals(AssociateStatusEnum.TRAINED.getstatusTxt()) ||
					associate.getAssociateEventsNo() >= Constants.IMAGES_NO_TO_TRAIN && associate.getAssociateStatus().getstatusTxt().equals(AssociateStatusEnum.NOT_TRAINED.getstatusTxt()) ||
					associate.getAssociateName().equals(this.associateName))
			{
				associate.setAssociateStatus(AssociateStatusEnum.TRAINED);
				trainedAssociates.add(associate.getAssociateName());
			}
		}
		cloudant.update(user);
		
		if(BusinessLogicFactory.getBluemixWatsonVisualRecognition().isClassifierReady())
		{
			System.out.println("Start training for associate : " + this.associateName);
			// 1- delete existing zip files from object storage
			try {
				deleteTrainingSetsForAsscoiates(trainedAssociates);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error in cleaning object storage.");
			}
			System.err.println("Object storage cleaned from previous sets.");
			
			// 2- build new positive sets for training the classifier
			List<String> positiveExamples = new ArrayList<String>();
			for (String associate : trainedAssociates) {
				List<VisionDoubleEvent> associateEvents = cloudant.getEventsForAssociate(associate);
				try {
					buildTrainingSetsForAsscoiates(positiveExamples, associateEvents, associate);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Error in training set creation for : " + associate);
				}
			}
			System.err.println("Object storage updated with the new sets.");
			
			// zip files have been created successfully and stored at object storage.
			try {
				BusinessLogicFactory.getBluemixWatsonVisualRecognition().reTrainClassifier(
						BusinessLogicFactory.getBluemixObjectStorageBusiness().getFile("Random.zip"), positiveExamples);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error in trainning the classifier for " + associateName);
				
			}
		}
		System.err.println("End training");
	}

	private void deleteTrainingSetsForAsscoiates(List<String> trainedAssociates) throws Exception {
		IBluemixObjectStorage objectStorageBus = BusinessLogicFactory.getBluemixObjectStorageBusiness();
		for (String associate : trainedAssociates) {
			objectStorageBus.deleteObject(associate + ".zip");
		}
	}
	
	private void buildTrainingSetsForAsscoiates(List<String> positiveExamples, List<VisionDoubleEvent> associateEvents, String associateName) throws IOException{
		Set<String> uniqueTitles = new HashSet<String>();
		for (VisionDoubleEvent event : associateEvents) {
			uniqueTitles.add(event.getFrameTitle());
		}
		
		System.err.println("Start building trainging set for : " + associateName);
		IBluemixObjectStorage objectStorageBus = BusinessLogicFactory.getBluemixObjectStorageBusiness();

		final int BUFFER = 2048;
        byte buffer[] = new byte[BUFFER];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(out);
        
		for (String eventTitle : uniqueTitles) {
			System.err.println("add file " + eventTitle);
	        InputStream in = objectStorageBus.getFile(eventTitle);
	        zos.putNextEntry(new ZipEntry(eventTitle));
	        int length;
	        while ((length = in.read(buffer)) > 0) {
	            zos.write(buffer, 0, length);
	        }
	        zos.closeEntry();
	        in.close();
		}
        zos.close();
        
        String trainingSetFileName = associateName + ".zip";
        ByteArrayInputStream zipButeArrayStream = new ByteArrayInputStream(out.toByteArray());
        
        objectStorageBus.storeObject(trainingSetFileName, zipButeArrayStream);
        
        positiveExamples.add(trainingSetFileName);
        System.err.println("End building trainging set for : " + associateName);
//        out.close();
	}

//	private void buildTrainingSetsForAsscoiates(Map<String, ByteArrayInputStream> positiveExamples, List<VisionDoubleEvent> associateEvents, String associateName) throws IOException{
//		System.err.println("Start building trainging set for : " + associateName);
//		IBluemixObjectStorage objectStorageBus = BusinessLogicFactory.getBluemixObjectStorageBusiness();
//
//		final int BUFFER = 2048;
//        byte buffer[] = new byte[BUFFER];
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        ZipOutputStream zos = new ZipOutputStream(out);
//        
//		for (VisionDoubleEvent event : associateEvents) {
//			System.err.println("add file " + event.getFrameTitle());
//	        InputStream in = objectStorageBus.getFile(event.getFrameTitle());
//	        zos.putNextEntry(new ZipEntry(event.getFrameTitle()));
//	        int length;
//	        while ((length = in.read(buffer)) > 0) {
//	            zos.write(buffer, 0, length);
//	        }
//	        zos.closeEntry();
//	        in.close();
//		}
//        zos.close();
//        
//        String trainingSetFileName = associateName + ".zip";
//        ByteArrayInputStream zipButeArrayStream = new ByteArrayInputStream(out.toByteArray());
//        
//        objectStorageBus.storeObject(trainingSetFileName, zipButeArrayStream);
//        
//        positiveExamples.put(trainingSetFileName, zipButeArrayStream);
//        System.err.println("End building trainging set for : " + associateName);
////        out.close();
//	}
}
