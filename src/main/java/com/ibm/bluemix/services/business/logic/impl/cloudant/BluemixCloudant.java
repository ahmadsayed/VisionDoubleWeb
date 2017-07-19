package com.ibm.bluemix.services.business.logic.impl.cloudant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.ibm.bluemix.services.business.logic.IBluemixCloudant;
import com.ibm.bluemix.services.business.logic.impl.EventRulesEnum;
import com.ibm.mea.build.web.rest.dto.AlertSelector;

public class BluemixCloudant implements IBluemixCloudant{
	
	private static Logger LOGGER = Logger.getLogger(BluemixCloudant.class.getName());
	
	private Database getDB(){
		Database db = null;
		try {
			db = CloudantClientMgr.getDB();
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(e.getMessage());
		}
		return db;
	}
	
	public Response create(ICloudantObject obj) {
		LOGGER.info("Save frame info to cloudant db.");
		return this.getDB().save(obj);
	}
	
	public Response update(ICloudantObject user){
		return this.getDB().update(user);
	}
	
	public UserConfiguration getUserConfig(String camId){
		
		try{
//			String selector="\"selector\": {\"cameraId\": "+ camId +"}";
			String selector = "\"selector\": {\"$and\": [{ \"userConfig\": " + true + " },{ \"cameraId\": " + camId + " }]}";
			List<UserConfiguration> configs = this.getDB().findByIndex(selector, UserConfiguration.class);
			if(configs.size() > 0)
				return configs.get(0);
			return null;
		}catch(Exception e){
			return null;
		}
	}
	
	public List<VisionDoubleEvent> getAlerts(String selector){
		
		try{
//			String selector="\"selector\": {\"cameraId\": {\"$eq\": "+ camId +"}}";
//			String selector = "\"selector\": {\"$and\": [{ \"userId\": " + userId + " },{ \"siteId\": " + siteId + " },{ \"cameraId\": " + camId + " }]}";
			List<VisionDoubleEvent> configs = this.getDB().findByIndex(selector, VisionDoubleEvent.class);
			return configs;
		}catch(Exception e){
			return null;
		}
	}

	public List<VisionDoubleEvent> getEventsBySelector(AlertSelector selector, String camId){
		
		try{
			 
			String selectorStr = "{\"selector\": {\"$and\": [";
			
			if(selector.getSelector().getRule() != null && selector.getSelector().getRule().getOperand() != null) {
				selectorStr += "{\"rule\": {\"" + selector.getSelector().getRule().getOperand() 
						+ "\": \"" + selector.getSelector().getRule().getValue() + "\"}},";
			}
			if(selector.getSelector().getFrameFromDate() != null && selector.getSelector().getFrameToDate() != null) {
				selectorStr += "{\"$and\": [ {\"frameTime\": {\"$gte\" : " +selector.getSelector().getFrameFromDate() + "}} , {\"frameTime\": {\"$lte\" : " + selector.getSelector().getFrameToDate() + "}}]},";
			}
			
			if(selector.getSelector().isPeople()) {
				selectorStr += "{\"visualRecognitionClasses\": { \"$elemMatch\" : {\"class\": {\"$eq\": \"person\" } }}},";
			}
			
			List<String> associates = getUserAssociates(camId);
			if(associates != null && associates.size() > 0)
			{
				String associatesStr = "";
				for(String associate : associates) {
					associatesStr +="\"" + associate + "\",";
				}
				associatesStr = associatesStr.substring(0, associatesStr.length()-1);
				if(selector.getSelector().isKnown()) {
					selectorStr += "{\"faceIdentificationClasses\": { \"$elemMatch\" : {\"class\": {\"$in\": [" + associatesStr +
							"] } }}},";
				} else {
					selectorStr += "{\"$not\": {\"faceIdentificationClass\": { \"$elemMatch\" : {\"class\": {\"$in\": [" + associatesStr +
							"] } }}}},";
				}
			}
			
			selectorStr += "{ \"classifiedAs\": { \"$exists\": false } },{\"$not\": { \"userConfig\": " + true + " }},";
			
			selectorStr = selectorStr.substring(0, selectorStr.length()-1);
			selectorStr += "]}}";
			System.err.println("####################");
			System.err.println(selectorStr);
			System.err.println("####################");
			List<VisionDoubleEvent> configs = this.getDB().findByIndex(selectorStr, VisionDoubleEvent.class);
			if(configs == null) {
				return new ArrayList<VisionDoubleEvent>();
			}
			return configs;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getUserAssociates(String camId) {
		List<String> associates = new ArrayList<String>();
		try{
			String selector = "\"selector\": {\"$and\": [{ \"userConfig\": " + true + " },{ \"cameraId\": " + camId + " }]}";
			List<UserConfiguration> configs = this.getDB().findByIndex(selector, UserConfiguration.class);
			if(configs.size() > 0 && configs.get(0).getAssociates() != null)
			{
				for (Associate associate : configs.get(0).getAssociates()) {
					associates.add(associate.getAssociateName());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return associates;
	}

	@Override
	public VisionDoubleEvent getEvent(String selectedFrameId) {
		try{
			String selector = "\"selector\": {\"$and\": [{ \"userConfig\": " + false + " },{ \"frameId\": " + selectedFrameId + " }]}";
			List<VisionDoubleEvent> event = this.getDB().findByIndex(selector, VisionDoubleEvent.class);
			if(event.size() > 0)
				return event.get(0);
			return null;
		}catch(Exception e){
			return null;
		}
	}

	@Override
	public List<VisionDoubleEvent> getEventsForAssociate(String associateName) {
		String selector = "{\"selector\": {\"$and\": [{ \"userConfig\": false },{\"$or\": [{ \"classifiedAs\": \"" + associateName + "\" }," +
				"{\"faceIdentificationClass\": { \"$elemMatch\" : {\"class\": {\"$in\": [ \"" + associateName + "\"] } }}}]}]}}";
		return this.getDB().findByIndex(selector, VisionDoubleEvent.class);
	}
	
	@Override
	public List<VisionDoubleEvent> getAssociateEvents(String selectedAssociate, Object fromDate, Object toDate) {
		try{
			String selectorStr = "{\"selector\": {\"$and\": [";
			
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    		
			if(fromDate != null && toDate != null) {
				selectorStr += "{\"$and\": [ {\"frameTime\": {\"$gte\" : " + dateFormat.parse(fromDate.toString()).getTime() + "}} , {\"frameTime\": {\"$lte\" : " + dateFormat.parse(toDate.toString()).getTime() + "}}]},";
			}
			
			selectorStr += "{\"visualRecognitionClasses\": { \"$elemMatch\" : {\"class\": {\"$eq\": \"person\" } }}},";
			
			selectorStr += "{\"$or\": [{\"faceIdentificationClass\": { \"$elemMatch\" : {\"class\": {\"$in\": [ \"" + selectedAssociate + "\"] } }}},{\"$and\": [";
			selectorStr += "{\"$not\": {\"faceIdentificationClass\": { \"$elemMatch\" : {\"class\": {\"$in\": [ \"" + selectedAssociate + "\"] } }}}},{\"classifiedAs\": \"" + selectedAssociate + "\"}]}]},";
			
			selectorStr += "{\"$not\": { \"userConfig\": " + true + " }},";
			
			selectorStr = selectorStr.substring(0, selectorStr.length()-1);
			selectorStr += "]}}";
			
			System.err.println("####################");
			System.err.println(selectorStr);
			System.err.println("####################");
			List<VisionDoubleEvent> configs = this.getDB().findByIndex(selectorStr, VisionDoubleEvent.class);
			if(configs == null) {
				return new ArrayList<VisionDoubleEvent>();
			}
			return configs;
		}catch(Exception e){
			return new ArrayList<VisionDoubleEvent>();
		}
	}

//	@Override
//	public List<VisionDoubleEvent> getAssociateEvents(String selectedAssociate, Object fromDate, Object toDate) {
//		try{
//			String selectorStr = "{\"selector\": {\"$and\": [";
//			
//    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//    		
//			if(fromDate != null && toDate != null) {
//				selectorStr += "{\"$and\": [ {\"frameTime\": {\"$gte\" : " + dateFormat.parse(fromDate.toString()).getTime() + "}} , {\"frameTime\": {\"$lte\" : " + dateFormat.parse(toDate.toString()).getTime() + "}}]},";
//			}
//			
//			selectorStr += "{\"visualRecognitionClasses\": { \"$elemMatch\" : {\"class\": {\"$eq\": \"person\" } }}},";
//			
//			selectorStr += "{\"faceIdentificationClass\": { \"$elemMatch\" : {\"class\": {\"$in\": [ \"" + selectedAssociate + "\"] } }}},";
//			
//			selectorStr += "{\"$not\": { \"userConfig\": " + true + " }},";
//			
//			selectorStr = selectorStr.substring(0, selectorStr.length()-1);
//			selectorStr += "]}}";
//			
//			
//			
//			
//			System.err.println("####################");
//			System.err.println(selectorStr);
//			System.err.println("####################");
//			List<VisionDoubleEvent> configs = this.getDB().findByIndex(selectorStr, VisionDoubleEvent.class);
//			if(configs == null) {
//				return new ArrayList<VisionDoubleEvent>();
//			}
//			return configs;
//		}catch(Exception e){
//			return new ArrayList<VisionDoubleEvent>();
//		}
//	}

	@Override
	public void confgureRule(String selectedAssociate, String rule, String camId) {
		UserConfiguration cnfg = getUserConfig(camId);
		cnfg.getEventsRules().remove(selectedAssociate);
		cnfg.getEventsRules().put(selectedAssociate, EventRulesEnum.getAlertRulesEnumByText(rule).getCode());
		update(cnfg);
	}

	@Override
	public void removeRule(String selectedAssociate, String camId) {
		UserConfiguration cnfg = getUserConfig(camId);
		cnfg.getEventsRules().remove(selectedAssociate);
		update(cnfg);
	}

	@Override
	public String getAssociateRule(String selectedAssociate, String camId) {
		UserConfiguration cnfg = getUserConfig(camId);
		if(cnfg.getEventsRules().get(selectedAssociate) != null)
			return EventRulesEnum.getAlertRulesEnumByCode(cnfg.getEventsRules().get(selectedAssociate)).getText();
		return null;
	}
}
