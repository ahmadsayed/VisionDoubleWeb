package com.ibm.mea.build.web.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.UserConfiguration;

@Path("user")
public class UserResource {
	
	@GET
	@Path("me/{camId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@Context HttpServletRequest request, @PathParam("camId") String camId) throws WebApplicationException 
	{
		try {
			IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
			
			UserConfiguration cnfg = cloudant.getUserConfig(camId);
			if(cnfg != null)
			{
				User user = new User(cnfg.getUserName(), VisionDoubleUtils.getImageContent(cnfg.getProfileImageTitle()));
				return Response.status(Response.Status.OK).entity(user).build();
			} else{
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new WebApplicationException("Unable to load user configuration.")).build();
			}
		} catch(Exception e)
		{
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
		}
	}
}
