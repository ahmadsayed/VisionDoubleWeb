/**
 * Copyright IBM Corp. 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.mea.build.web.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.IVisionDouble;
import com.ibm.bluemix.services.business.logic.impl.BusinessLogicFactory;
import com.ibm.bluemix.services.business.logic.impl.cloudant.VisionDoubleEvent;
import com.ibm.mea.build.web.rest.dto.AlertEntity;
import com.ibm.mea.build.web.rest.dto.AlertSelector;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.util.GsonSingleton;

@Path("conversation")
public class ConversationResource {

  private static String API_VERSION;
  private static String PASSWORD = "v4OtU1lsfGmW";
  private static String URL = "https://gateway.watsonplatform.net/conversation/api";
  private static String USERNAME = "6d5c6366-6834-45fb-afcd-e71e26207c2d";
  private static String workspaceId = "880c6596-6a0f-42a6-97ce-dfb8f3f73694";
  
  private MessageRequest buildMessageFromPayload(InputStream body) {
    StringBuilder sbuilder = null;
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(body, "UTF-8"));
      sbuilder = new StringBuilder();
      String str = reader.readLine();
      while (str != null) {
        sbuilder.append(str);
        str = reader.readLine();
        if (str != null) {
          sbuilder.append("\n");
        }
      }
      return GsonSingleton.getGson().fromJson(sbuilder.toString(), MessageRequest.class);
    } catch (IOException e) {
    	e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
    	  e.printStackTrace();
      }
    }
    return null;
  }

  private MessageResponse getWatsonResponse(MessageRequest conversationRequest, String id) throws Exception {

    ConversationService service = new ConversationService(API_VERSION != null ? API_VERSION : ConversationService.VERSION_DATE_2016_09_20);
    if (USERNAME != null || PASSWORD != null) {
      service.setUsernameAndPassword(USERNAME, PASSWORD);
    }
    if (URL != null) {
      service.setEndPoint(URL);
    }
    
    IBluemixCloudant cloudant = BusinessLogicFactory.getBluemixCloudantBusiness();
	
    MessageResponse conversationResponse = service.message(id, conversationRequest).execute();
    if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_RETRIEVE_EVENTS))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	
    	AlertSelector alertSelector = new AlertSelector();
    	AlertEntity alertEntity = new AlertEntity();
    	
    	if(conversationResponse.getContext().get(Constants.CONTEXT_FROM_DATE_PROPERTY_KEY) != null 
    			&& conversationResponse.getContext().get(Constants.CONTEXT_TO_DATE_PROPERTY_KEY) != null) {
    		Date fromDate = new Date();
    		Date toDate = new Date();
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    		fromDate = dateFormat.parse(conversationResponse.getContext().get(Constants.CONTEXT_FROM_DATE_PROPERTY_KEY).toString());
    		toDate	= dateFormat.parse(conversationResponse.getContext().get(Constants.CONTEXT_TO_DATE_PROPERTY_KEY).toString());
    		
    		alertEntity.setFrameFromDate(fromDate.getTime());
    		alertEntity.setFrameToDate(toDate.getTime());
    	}
    	
    	alertEntity.setPeople(true);
    	if(conversationResponse.getContext().get(Constants.CONTEXT_STATE_PROPERTY_KEY).equals("unknown")) {
    		alertEntity.setKnown(false);
    	} else {
    		alertEntity.setKnown(true);
    	}
    	
    	alertSelector.setSelector(alertEntity);
    	List<VisionDoubleEvent> events = cloudant.getEventsBySelector(alertSelector, getCamId(conversationResponse));
    	
    	conversationResponse.getOutput().put("payload", events);
    	conversationResponse.getOutput().remove("text");
    	String responseTxt = "";
    	if(events != null && events.size() > 0)
    	{
        	conversationResponse.getContext().put("EVENTS_SIZE", events.size());
    		conversationResponse.getContext().put("USER_INPUT", "OPTIONS_BUTTONS");
    		responseTxt = "Here are all the arrival events for" + " "
    				+ conversationResponse.getContext().get(Constants.CONTEXT_STATE_PROPERTY_KEY) + " " 
    				+ conversationResponse.getContext().get(Constants.CONTEXT_OBJECT_PROPERTY_KEY);
    		if(conversationResponse.getContext().get(Constants.CONTEXT_FROM_DATE_PROPERTY_KEY) != null && conversationResponse.getContext().get(Constants.CONTEXT_TO_DATE_PROPERTY_KEY) != null)
    			responseTxt += (" from " + conversationResponse.getContext().get(Constants.CONTEXT_FROM_DATE_PROPERTY_KEY) + " to " + conversationResponse.getContext().get(Constants.CONTEXT_TO_DATE_PROPERTY_KEY));
    		responseTxt += ". Can you identify any of the people? Select the event you identify from the list above to identify them for later, otherwise Start over";
    	} else{
        	conversationResponse.getContext().put("EVENTS_SIZE", 0);
    		responseTxt = "no arrival events captured for" + " "
    				+ conversationResponse.getContext().get(Constants.CONTEXT_STATE_PROPERTY_KEY) + " " 
    				+ conversationResponse.getContext().get(Constants.CONTEXT_OBJECT_PROPERTY_KEY);
    		if(conversationResponse.getContext().get(Constants.CONTEXT_FROM_DATE_PROPERTY_KEY) != null && conversationResponse.getContext().get(Constants.CONTEXT_TO_DATE_PROPERTY_KEY) != null)
    			responseTxt += (" from " + conversationResponse.getContext().get(Constants.CONTEXT_FROM_DATE_PROPERTY_KEY) + " till " 
    				+ conversationResponse.getContext().get(Constants.CONTEXT_TO_DATE_PROPERTY_KEY)) ;
    	}
    	conversationResponse.getOutput().put("text", responseTxt);
    	List<String> options = new ArrayList<String>();
		options.add("Start over");
		conversationResponse.getContext().put("OPTIONS_BUTTONS", options);
		conversationResponse.getContext().put("USER_INPUT", "OPTIONS_BUTTONS");
	} else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_RETRIEVE_USER_DEFINED_OBJECTS))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	
    	List<String> associates = cloudant.getUserAssociates(getCamId(conversationResponse)); // should pass user id, as of now there is no user management.

    	if(conversationResponse.getContext().get(Constants.CONTEXT_STATE_PROPERTY_KEY) != null 
    			&& conversationResponse.getContext().get(Constants.CONTEXT_STATE_PROPERTY_KEY).equals("unknown"))
    		associates.add("New Associate");
    	
    	if(associates.size() > 0)
		{
			conversationResponse.getContext().put("ASSOCIATES", associates);
			conversationResponse.getContext().put("OPTIONS_BUTTONS", associates);
		} else
		{
			List<String> options = new ArrayList<String>();
			options.add("Start over");
			conversationResponse.getContext().put("OPTIONS_BUTTONS", options);
			conversationResponse.getOutput().remove("text");
			conversationResponse.getOutput().put("text", "Currently, there are no associates.");
		}
    	
	} else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_CLASSIFY_AS))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	String selectedFrameId = conversationResponse.getContext().get("EVENT_SELECTOR").toString();
    	String associateName = conversationResponse.getInputText();
    	
    	IVisionDouble visionDouble = BusinessLogicFactory.getVisionDouble();
		int noEventsForAssociate = visionDouble.classifyAs(selectedFrameId, associateName, getCamId(conversationResponse));
    	
		conversationResponse.getOutput().remove("text");
		String responseTxt = "Noted, ";
    	if(noEventsForAssociate < Constants.IMAGES_NO_TO_TRAIN)
    	{
    		 String message = "Since \"" + conversationResponse.getInputText() + "\" is not yet one of the people I am already trained to identify, " +
    				"as I only have " + noEventsForAssociate + " images for them so far, I will flag the image for later training when we complete at least 10 images for \"" + 
    				conversationResponse.getInputText() + "\"";
    		 responseTxt += message;
    	} else {
    		// Visual recognition service needs two or more training sets to be able t train, so we need to validate that this user has more than one associate with more than 10 images
    		if(visionDouble.ableToTrain(getCamId(conversationResponse)))
    		{
    			visionDouble.startTrainForAssociate(associateName, getCamId(conversationResponse));
    			responseTxt += "Since I now have " + noEventsForAssociate + " images for \"" + conversationResponse.getInputText() + "\", I will send the image immediately for future identification.";
    		} else { // TODO_HA Same text as if I started the trainer for now.
    			responseTxt += "Although I have " + noEventsForAssociate + " images for " + associateName + ", I still can't be trained with them until I have an additional set of images for a different associate. So, I will keep them for later.";
    		}
    	}
    	conversationResponse.getOutput().put("text", responseTxt);
	} else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_CLASSIFY_NEW_ASSOCIATE))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	String selectedFrameId = conversationResponse.getContext().get("EVENT_SELECTOR").toString();
    	String newAssociateName = conversationResponse.getInputText();
    	
    	IVisionDouble visionDouble = BusinessLogicFactory.getVisionDouble();
    	visionDouble.classifyNewAssociate(selectedFrameId, newAssociateName, getCamId(conversationResponse));
	} else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_RETRIEVE_EVENTS_FOR_ASSOCIATE))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	String selectedAssociate = conversationResponse.getContext().get("SELECTED_ASSOCIATE").toString();
    	Object fromDate = conversationResponse.getContext().get("FROM_DATE");
    	Object toDate = conversationResponse.getContext().get("To_DATE");
    	
    	List<VisionDoubleEvent> events = cloudant.getAssociateEvents(selectedAssociate, fromDate, toDate);
    	conversationResponse.getOutput().put("payload", events);
    	

    	conversationResponse.getContext().remove("ASSOCIATES");
    	conversationResponse.getContext().remove("SELECTED_ASSOCIATE");
    	conversationResponse.getContext().remove("FROM_DATE");
    	conversationResponse.getContext().remove("To_DATE");
	}
	else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_RETRIEVE_ASSOCIATE_RULE))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	
    	String selectedAssociate = conversationResponse.getContext().get("SELECTED_ASSOCIATE").toString();
    	String associateRule = cloudant.getAssociateRule(selectedAssociate, getCamId(conversationResponse));
    	
    	String outputText = "";
    	if(associateRule != null)
    		outputText = "Currently " + selectedAssociate + " rule is '" + associateRule + "'. How would you like to handle arrivals of '" + selectedAssociate + "'.";
    	else
    		outputText = "Currently no rule is configured for '" + selectedAssociate + "'. How would you like to handle arrivals of '" + selectedAssociate + "'.";
    	conversationResponse.getOutput().put("text", outputText);
	}
	else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_CONFIGURE_RULE))
	{
    	conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	
    	String selectedAssociate = conversationResponse.getContext().get("SELECTED_ASSOCIATE").toString();
    	String rule = conversationResponse.getInputText();
    	
    	cloudant.confgureRule(selectedAssociate, rule, getCamId(conversationResponse));
	}
	else if (conversationResponse.getContext().containsKey(Constants.CONTEXT_ACTION_PROPERTY_NAME)
    		&& conversationResponse.getContext().get(Constants.CONTEXT_ACTION_PROPERTY_NAME).equals(Constants.CONTEXT_ACTION_REMOVE_RULE))
	{
		conversationResponse.getContext().remove(Constants.CONTEXT_ACTION_PROPERTY_NAME);
    	
    	String selectedAssociate = conversationResponse.getContext().get("SELECTED_ASSOCIATE").toString();
    	
    	cloudant.removeRule(selectedAssociate, getCamId(conversationResponse));
	}
    return conversationResponse;
  }
  
  private String getCamId(MessageResponse conversationResponse) {
	  return "double-vision-cam";
  }

  @POST @Path("converse") 
  @Consumes(MediaType.APPLICATION_JSON) 
  @Produces(MediaType.APPLICATION_JSON) 
  public Response postMessage(InputStream body) {
System.out.println("Start message handling...");
    HashMap<String, Object> errorsOutput = new HashMap<String, Object>();
    MessageRequest request = buildMessageFromPayload(body);

    if (request == null) {
      throw new IllegalArgumentException("");
    }

    MessageResponse response = null;

    try {
      response = getWatsonResponse(request, workspaceId);  	
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.BAD_REQUEST).entity(e).build();
      //return Response.ok(new Gson().toJson(errorsOutput, HashMap.class)).type(MediaType.APPLICATION_JSON).build();
    }
    return Response.ok(new Gson().toJson(response, MessageResponse.class)).type(MediaType.APPLICATION_JSON).build();
  }
  
}
