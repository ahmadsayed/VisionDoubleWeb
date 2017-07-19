package com.ibm.bluemix.services.business.logic.impl.visual_recognition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.bluemix.services.business.logic.IBluemixWatsonVisualRecognition;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualRecognitionOptions;

public class BluemixWatsonVisualRecognition implements IBluemixWatsonVisualRecognition{

	private static Logger LOGGER = Logger.getLogger(BluemixWatsonVisualRecognition.class.getName());
	
	@Override
	public List<VisualClass> analyzeImg(File frameFile) {
		VisualRecognitionOptions options = new VisualRecognitionOptions.Builder().images(frameFile).build();
		ClassifyImagesOptions classifyOptions = new ClassifyImagesOptions.Builder().images(frameFile).build();
		
		VisualRecognitionResult vrResult = analyze(options, classifyOptions);
		
		return ((VisualClassifier)vrResult.getKeywords().get(0)).getClasses();
	}
	
	private VisualRecognitionResult analyze(VisualRecognitionOptions options, ClassifyImagesOptions classifyOptions) {
		VisualRecognitionResult result = new VisualRecognitionResult();

	    LOGGER.info("Calling Image Keyword...");
	    try {
	      VisualClassification execute = VisualRecognitionMgr.getVisualRecognition().classify(classifyOptions).execute();
	      List<ImageClassification> imageClassifiers = execute.getImages();
	      if (imageClassifiers != null && !imageClassifiers.isEmpty()) {
	        result.setKeywords(imageClassifiers.get(0).getClassifiers());
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    return result;
	}
	
    public void rebuildClassifier(File negative, File... files) {
        System.err.println("I will delete the existing classifier...");
        if (getFriendAndFamilyClassifier() != null) {
        	VisualRecognitionMgr.getVisualRecognition().deleteClassifier(getFriendAndFamilyClassifier().getId()).execute();
        }
        System.err.println("Classifier has been deleted...");
        createCustomIdentifier(negative, files);
    }
    
    private VisualClassifier getFriendAndFamilyClassifier () {
        List<VisualClassifier> visualClassifiers = VisualRecognitionMgr.getVisualRecognition().getClassifiers().execute();
        for (VisualClassifier vc : visualClassifiers) {
            if (vc.getName().equalsIgnoreCase("FriendAndFamily")) {
                return vc;
            }
        }
        return null;
    }
    

    
    private void createCustomIdentifier(File negative, File... files) {
        ClassifierOptions.Builder builder = new ClassifierOptions.Builder();
        for (File f  : files) {
        	String pSetName = f.getName().split(".zip")[0];
        	System.err.println("CalssifierBuilder -> Add pSet with name " + pSetName);
            builder.addClass(pSetName, f);
        }
        if (negative != null) {
            builder.negativeExamples(negative);
                    
        }
        builder.classifierName("FriendAndFamily");
        System.err.println("I will create the new classifier...");
        VisualRecognitionMgr.getVisualRecognition().createClassifier(builder.build()).execute();
        System.err.println("Classifier has been created...");
    }

	@Override
	public void reTrainClassifier(InputStream negativeExamples,
			List<String> positiveExamples) throws IOException {
		File negative = null;
		File [] fileList  = null;		
		if (negativeExamples != null) {
			negative = File.createTempFile("random-", ".zip");
			negative.deleteOnExit();
			Files.copy(negativeExamples, negative.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.err.println("Negative set temp file " + negative.getName() + " has been created in " + negative.toPath());
		}
		if (positiveExamples != null) {
			int numberOfClasses  = positiveExamples.size();
			fileList = new File[numberOfClasses];
			int i = 0;
			for (String key : positiveExamples) {
				fileList[i] = File.createTempFile(key, ".zip");
				fileList[i].deleteOnExit();
				Files.copy(BusinessLogicFactory.getBluemixObjectStorageBusiness().getFile(key), fileList[i].toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.err.println("Positive set temp file " + fileList[i].getName() + " has been created in " + fileList[i].toPath());
				i++;
			}
		}
		rebuildClassifier(negative, fileList);
//		System.err.println("Training.........................................................................");
	}

	@Override
	public boolean isClassifierReady() {
		return true;
	}
}
