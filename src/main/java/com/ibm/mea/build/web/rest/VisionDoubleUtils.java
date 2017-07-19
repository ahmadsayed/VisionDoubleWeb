package com.ibm.mea.build.web.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;

import com.ibm.bluemix.services.business.logic.IBluemixObjectStorage;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;

public class VisionDoubleUtils {

	public static String getImageContent(String imageTitle) throws IOException {
		IBluemixObjectStorage objectStorage = BusinessLogicFactory.getBluemixObjectStorageBusiness();
		InputStream photoStream = objectStorage.getFile(imageTitle);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(photoStream, output);
		//TODO_HA should handle diff extension
		return "data:image/png;base64," + DatatypeConverter.printBase64Binary(output.toByteArray());
	}
}
