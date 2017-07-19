package com.ibm.mea.build.web.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.JsonObject;
import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.mea.build.web.rest.response.AlertResponse;

@Path("events")
public class EventResource {
	
	@GET
	@Path("image/{frameId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEventImage(@Context HttpServletRequest request, @PathParam("frameId") String frameId) throws WebApplicationException, IOException {
		IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
		VisionDoubleEvent event = cloudant.getEvent(frameId);
		if(event != null)
		{
			JsonObject imageJson = new JsonObject();
			imageJson.addProperty("image", VisionDoubleUtils.getImageContent(event.getFrameTitle()));

			return Response.ok(imageJson.toString()).build();
		}
		return Response.ok().build();
	}
	
	@GET
	@Path("all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAlerts(@Context HttpServletRequest request) throws WebApplicationException {
		AlertResponse alertResponse = new AlertResponse();
		ResponseBuilder builder = null;
		try {
			String selector="\"selector\": {\"rule\": {\"$eq\": \"1\"}}";
//				final String cameraId = request.getParameter("cameraId");
			IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
			List<VisionDoubleEvent> alerts = cloudant.getAlerts(selector);
			if(alerts != null)
			{
				alertResponse.setAlerts(alerts);
			}
			builder = Response.ok(alertResponse, MediaType.APPLICATION_JSON);
			return builder.build();
		}catch(Exception e)
		{
			builder = Response.serverError();
			return builder.build();
		}
	}
	
	@POST
	@Path("filter")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlertsByFilter(@Context HttpServletRequest request , final String entity) throws WebApplicationException {
		AlertResponse alertResponse = new AlertResponse();
		ResponseBuilder builder = null;
		try {
//			GsonBuilder gsonBuilder = new GsonBuilder();
//			Gson gson = gsonBuilder.create();
//			AlertSelector alertEntity = gson.fromJson(entity, AlertSelector.class);
//			System.err.println("##################");
//			System.err.println(entity);
////			System.err.println(alertEntity.toString());
//			System.err.println("##################");
//			String selector="\"selector\": {\"rule\": {\"$eq\": \"1\"}}";
//				final String cameraId = request.getParameter("cameraId");
			IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
			List<VisionDoubleEvent> alerts = cloudant.getAlerts(entity);
			if(alerts != null)
			{
				alertResponse.setAlerts(alerts);
			}
			builder = Response.ok(alertResponse, MediaType.APPLICATION_JSON);
			return builder.build();
		}catch(Exception e)
		{
			builder = Response.serverError();
			return builder.build();
		}
	}
}
