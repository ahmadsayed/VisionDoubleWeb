package com.ibm.bluemix.services.business.logic;

import java.io.InputStream;

public interface IBluemixObjectStorage {

	String storeObject(String fileName, InputStream fileInputStream);

	InputStream getFile(String fileName);

	void deleteObject(String fileName) throws Exception;
}
