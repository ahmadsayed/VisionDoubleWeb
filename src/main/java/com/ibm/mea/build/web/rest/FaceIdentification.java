package com.ibm.mea.build.web.rest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.ibm.bluemix.services.business.logic.impl.visual_recognition.VisualRecognitionMgr;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageFace;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Location;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualRecognitionOptions;

/**
 *
 * @author ahmedh
 */
public class FaceIdentification {

    VisualRecognition service = null;

    public FaceIdentification() {
    	
        service = VisualRecognitionMgr.getVisualRecognition();
    }
    public byte[] cropFace(File file) throws IOException {
        VisualRecognitionOptions vrOptions = new VisualRecognitionOptions.Builder().images(file).build();
        DetectedFaces detecteFaces = service.detectFaces(vrOptions).execute();
        List<ImageFace> faces = detecteFaces.getImages();

        for (ImageFace face : faces) {
            for (Face f : face.getFaces()) {
                Location loc = f.getLocation();
                BufferedImage src = ImageIO.read(file);
                int width = loc.getWidth();
                if (loc.getLeft() + width > src.getWidth()) {
                    width = src.getWidth() - loc.getLeft();
                }
                
                int height = loc.getHeight();
                if (loc.getTop() + loc.getHeight() > src.getHeight()) {
                    height = src.getHeight() - loc.getHeight();
                }
                BufferedImage dst = src.getSubimage(loc.getLeft(), loc.getTop(), width, height);
                
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(dst, "jpg", out);
                return out.toByteArray();
                
            }
        }
        return null;
    }
    public void detectFacesInFolder(File directory) throws IOException {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                VisualRecognitionOptions vrOptions = new VisualRecognitionOptions.Builder().images(file).build();
                DetectedFaces detecteFaces = service.detectFaces(vrOptions).execute();
                try {
                    List<ImageFace> faces = detecteFaces.getImages();
                    for (ImageFace face : faces) {
                        int i = 0;
                        for (Face f : face.getFaces()) {
                            Location loc = f.getLocation();
                            BufferedImage src = ImageIO.read(file);
                            BufferedImage dst = src.getSubimage(loc.getLeft(), loc.getTop(), loc.getWidth(), loc.getHeight());
                            ImageIO.write(dst, "jpg", new File(directory.getAbsolutePath() + File.separator + (i + file.getName())));
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    public List<VisualClassification> getAllFaces(File file) throws Exception {
        ClassifyImagesOptions options;
        VisualRecognitionOptions vrOptions = new VisualRecognitionOptions.Builder().images(file).build();
        DetectedFaces detecteFaces = service.detectFaces(vrOptions).execute();
        List<ImageFace> faces = detecteFaces.getImages();

        int i = 1;
        List<VisualClassification> result = new ArrayList<>();
        if(faces != null)
        {
	        for (ImageFace face : faces) {
	            for (Face f : face.getFaces()) {
	                Location loc = f.getLocation();
	                BufferedImage src = ImageIO.read(file);
	                int width = loc.getWidth();
	                if (loc.getLeft() + width > src.getWidth()) {
	                    width = src.getWidth() - loc.getLeft();
	                }
	                
	                int height = loc.getHeight();
	                if (loc.getTop() + loc.getHeight() > src.getHeight()) {
	                    height = src.getHeight() - loc.getHeight();
	                }
	                BufferedImage dst = src.getSubimage(loc.getLeft(), loc.getTop(), width, height);
	                
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                ImageIO.write(dst, "jpg", out);
					if (getFriendAndFamilyClassifier() != null) {
						options = new ClassifyImagesOptions.Builder().classifierIds(getFriendAndFamilyClassifier().getId())
								.images(out.toByteArray(), "Sample" + (i++) + ".jpg").build();
						result.add(service.classify(options).execute());
					}
	
	            }
	        }
        }
        return result;
    }

    public void createCustomIdentifier(File negative, File... files) {
        ClassifierOptions.Builder builder = new ClassifierOptions.Builder();
        for (File f : files) {
            builder.addClass(f.getName().split("\\.")[0], f);
        }
        if (negative != null) {
            builder.negativeExamples(negative);

        }
        builder.classifierName("FriendAndFamily");
        service.createClassifier(builder.build()).execute();
    }

    public VisualClassifier getFriendAndFamilyClassifier() {
        List<VisualClassifier> visualClassifiers = service.getClassifiers().execute();
        for (VisualClassifier vc : visualClassifiers) {
            if (vc.getName().equalsIgnoreCase("FriendAndFamily")) {
                return vc;
            }
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception {
//		System.out.println(f.getAllFaces(new File("c:\\train\\hossam\\20170215160711.jpg")));
    	
    	FaceIdentification f = new FaceIdentification();
    	//System.out.println(f.getAllFaces(new File("C:\\Users\\admin\\Pictures\\Salma\\Salma 2.jpg")));
//		f.rebuildClassifier(
//				new File("C:\\Users\\admin\\Pictures\\training-sets\\Random.zip"), 
//				new File("C:\\Users\\admin\\Pictures\\training-sets\\Ahmed.zip"), 
//				new File("C:\\Users\\admin\\Pictures\\training-sets\\Haitham.zip"));
		
		System.out.println(f.getFriendAndFamilyClassifier());
	}

    public void updateClassifier(File file) {
        ClassifierOptions cOptions = new ClassifierOptions.Builder().addClass(file.getName(), file).build();
        service.updateClassifier(getFriendAndFamilyClassifier().getId(), cOptions).execute();
    }

    public void rebuildClassifier(File negative, File... files) {
    	if (getFriendAndFamilyClassifier() != null) {
    		service.deleteClassifier(getFriendAndFamilyClassifier().getId()).execute();
    	}
        createCustomIdentifier(negative, files);
    }
    
    
}