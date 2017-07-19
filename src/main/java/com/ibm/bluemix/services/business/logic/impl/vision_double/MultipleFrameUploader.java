package com.ibm.bluemix.services.business.logic.impl.vision_double;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import com.ibm.mea.build.web.rest.FrameHandler;

public class MultipleFrameUploader extends Thread {
	
	private Map<String, File> files;

	private String cameraId;
	
	private String evtTime;

	public MultipleFrameUploader(Map<String, File> files, String cameraId, String evtTime) {
		this.files = files;
		this.cameraId = cameraId;
		this.evtTime = evtTime;
	}

	public void run() {
		for (Entry<String, File> tmpFile : files.entrySet()) {
			FrameHandler frameHandler = new FrameHandler();
			try {
				frameHandler.handleFile(tmpFile.getValue(), tmpFile.getKey(), cameraId, evtTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
