package com.ibm.mea.build.web.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.bluemix.services.business.logic.impl.vision_double.MultipleFrameUploader;

@Path("frame")
public class FrameResource {

	@Path("/offline-upload")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFiles(final FormDataMultiPart multiPart, @FormDataParam("cameraId") String cameraId, @FormDataParam("eventTime") String evtTime) {

		Map<String, File> tmpFiles = new HashMap<String, File>();
		
		List<FormDataBodyPart> bodyParts = multiPart.getFields("file");
		for (int i = 0; i < bodyParts.size(); i++) {
			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
			String fileName = bodyParts.get(i).getContentDisposition().getFileName();
			
			try {
				tmpFiles.put(fileName, this.createTmpFileForInputStream(bodyPartEntity.getInputStream()));
			} catch (Exception e) {
				e.printStackTrace();
				// event with exception will not be added to the list of events
			}
		}
		
		MultipleFrameUploader frameUploader = new MultipleFrameUploader(tmpFiles, cameraId, evtTime);
		frameUploader.start();
		
		return Response.ok().build();
	}
	
	@POST
	@Path("/upload")
 	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFrame(@FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData,
			@FormDataParam("cameraId") String cameraId, @FormDataParam("eventTime") String evtTime)
	{
		VisionDoubleEvent event;
		try {
			File frameFile = this.createTmpFileForInputStream(fileInputStream);
			
			FrameHandler frameHandler = new FrameHandler();
			if(evtTime == null)
			{
				SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
				evtTime = sdfTime.format(new Date());
			}
			
			event = frameHandler.handleFile(frameFile, fileMetaData.getFileName(), cameraId, evtTime);
			return Response.ok(event).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
		}
	}
	
	private File createTmpFileForInputStream(InputStream fileInputStream) throws IOException {
		File tmpFile = File.createTempFile("vision-", ".jpg");
		tmpFile.deleteOnExit();	
		Files.copy(fileInputStream, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		return tmpFile;
	}
	
}
