package com.ibm.mea.build.web.rest.object_storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.ibm.bluemix.services.business.logic.IBluemixObjectStorage;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;

@Path("object-storage")
public class BluemixObjectStorageResource {
	
	@POST
	@Path("/upload")
 	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response uploadPdfFile(@FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		IBluemixObjectStorage objectStorageBus = BusinessLogicFactory.getBluemixObjectStorageBusiness();
		objectStorageBus.storeObject(fileMetaData.getFileName(), fileInputStream);
			
		return Response.ok("Data uploaded successfully !!").build();
	}
	
	@GET
	@Path("/zip")
 	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response zipFile() throws Exception
	{
		IBluemixObjectStorage objectStorageBus = BusinessLogicFactory.getBluemixObjectStorageBusiness();
		InputStream in = objectStorageBus.getFile("Colin_Powell_0007.jpg");
		
		final int BUFFER = 2048;
        byte buffer[] = new byte[BUFFER];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(out);
        
        zos.putNextEntry(new ZipEntry("Colin_Powell_0007.jpg"));
        int length;
        while ((length = in.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        zos.closeEntry();
        zos.close();
        
        objectStorageBus.storeObject("Colin_Powell_0007.zip", new ByteArrayInputStream(out.toByteArray()));
			
		return Response.ok("Data uploaded successfully !!").build();
	}
}
