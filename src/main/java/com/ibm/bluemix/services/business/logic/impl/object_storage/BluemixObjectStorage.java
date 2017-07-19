package com.ibm.bluemix.services.business.logic.impl.object_storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.storage.ObjectStorageService;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.DLPayload;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.storage.object.SwiftContainer;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.openstack.OSFactory;

import com.ibm.bluemix.services.business.logic.IBluemixObjectStorage;
import com.ibm.mea.build.web.rest.object_storage.BluemixObjectStorageResource;

public class BluemixObjectStorage implements IBluemixObjectStorage {
	
	private static Logger LOGGER = Logger.getLogger(BluemixObjectStorageResource.class.getName());
	private static final String userId = "60e18d9b2ffe4d1ea05dd229b03e9d86";
	private static final String password = "rU5)FeJOw[y0xOqp";
	private static final String domain = "1182831";
	private static final String project = "object_storage_6d0562aa_621c_4d33_838e_45589da94bcc";
	private static final String OBJECT_STORAGE_AUTH_URL = "https://identity.open.softlayer.com/v3";
	private static final String OBJECT_STORAGE_CONTAINER_NAME = "vision-double-container";
	
	@Override
	public String storeObject(String fileName, InputStream fileInputStream) {
		ObjectStorageService objectStorage = authenticateAndGetObjectStorageService();

		if(objectStorage != null)
		{
			if(!isContainerExists(objectStorage, OBJECT_STORAGE_CONTAINER_NAME))
			{
				ActionResponse response = ceateContainer(objectStorage, OBJECT_STORAGE_CONTAINER_NAME);
				if(!response.isSuccess())
				{
					LOGGER.severe("Unable to create container because of : " + response.getCode() + " : " + response.getFault());
					throw new WebApplicationException(response.getFault(), response.getCode());
				}
			}
			
			LOGGER.info("Storing file " + fileName +" in container " + OBJECT_STORAGE_CONTAINER_NAME + "......");
			
			Payload<InputStream> payload = new PayloadClass(fileInputStream);
			String objectId = objectStorage.objects().put(OBJECT_STORAGE_CONTAINER_NAME, fileName, payload);
			
			if(objectId != null)
			{
				System.out.println("File " + fileName +" has been stored in container " + OBJECT_STORAGE_CONTAINER_NAME + " with ID: " + objectId + " !");
				return objectId;
			} else {
				throw new WebApplicationException("Unable to store file in object storage service.", 500);
			}
		} else{
			throw new WebApplicationException("Unable to obtain object storage service instance.", 500);
		}
	}

	@Override
	public void deleteObject(String fileName) throws Exception {
		ObjectStorageService objectStorage = authenticateAndGetObjectStorageService();

		if(objectStorage != null)
		{
			LOGGER.info("Delete file " + fileName +" in container " + OBJECT_STORAGE_CONTAINER_NAME + "......");
			
			objectStorage.objects().delete(OBJECT_STORAGE_CONTAINER_NAME, fileName);
		} else{
			throw new WebApplicationException("Unable to obtain object storage service instance.", 500);
		}
	}
	
	private ActionResponse ceateContainer(ObjectStorageService objectStorage, String objectStorageContainerName) {
		return objectStorage.containers().create(objectStorageContainerName);
	}

	private boolean isContainerExists(ObjectStorageService objectStorage, String containerName){
		List<? extends SwiftContainer> containers = objectStorage.containers().list();
		for (SwiftContainer swiftContainer : containers) {
			if(swiftContainer.getName().equals(containerName))
				return true;
		}
		return false;
	}
	
	private ObjectStorageService authenticateAndGetObjectStorageService() {
		LOGGER.info("Bluemix object storage authenticating...");
		
		Identifier domainIdent = Identifier.byName(domain);
		Identifier projectIdent = Identifier.byName(project);

		OSClientV3 os = OSFactory.builderV3()
				.endpoint(OBJECT_STORAGE_AUTH_URL)
				.credentials(userId, password)
				.scopeToProject(projectIdent, domainIdent)
				.authenticate();

		LOGGER.info("Bluemix object storage authenticated successfully!");

		ObjectStorageService objectStorage = os.objectStorage();

		return objectStorage;
	}
	
	private class PayloadClass implements Payload<InputStream> {
		private InputStream stream = null;

		public PayloadClass(InputStream stream) {
			this.stream = stream;
		}

		@Override
		public void close() throws IOException {
			stream.close();
		}

		@Override
		public InputStream open() {
			return stream;
		}

		@Override
		public void closeQuietly() {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}

		@Override
		public InputStream getRaw() {
			return stream;
		}
	}

	public InputStream getFile(String fileName){
		
		ObjectStorageService objectStorage = authenticateAndGetObjectStorageService();
		SwiftObject fileObj = objectStorage.objects().get(OBJECT_STORAGE_CONTAINER_NAME, fileName);

		if(fileObj == null){ //The specified file was not found
			System.err.println("Err: File " + fileName + "is not found.");
		}

		DLPayload payload = fileObj.download();
		InputStream in = payload.getInputStream();
		
		return in;
	}
	
	public static void main(String []args) throws FileNotFoundException {
		BluemixObjectStorage b = new BluemixObjectStorage();
		b.storeObject("201702151608421.jpg", new FileInputStream(new File("C:\\train\\hossam\\20170215160841.jpg")));
	}
	
}
