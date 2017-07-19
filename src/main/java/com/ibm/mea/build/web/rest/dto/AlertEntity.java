package com.ibm.mea.build.web.rest.dto;

public class AlertEntity {
	private AlertFieldValue camId;
	private AlertFieldValue frameId;
	private AlertFieldValue visualEvent;
	private AlertFieldValue frameDate;
	private Long frameFromDate;
	private Long frameToDate;
	private boolean known;
	private boolean people;
	
	private AlertFieldValue rule;
	/**
	 * @return the camId
	 */
	public AlertFieldValue getCamId() {
		return camId;
	}
	/**
	 * @param camId the camId to set
	 */
	public void setCamId(AlertFieldValue camId) {
		this.camId = camId;
	}
	/**
	 * @return the frameId
	 */
	public AlertFieldValue getFrameId() {
		return frameId;
	}
	/**
	 * @param frameId the frameId to set
	 */
	public void setFrameId(AlertFieldValue frameId) {
		this.frameId = frameId;
	}
	/**
	 * @return the visualEvent
	 */
	public AlertFieldValue getVisualEvent() {
		return visualEvent;
	}
	/**
	 * @param visualEvent the visualEvent to set
	 */
	public void setVisualEvent(AlertFieldValue visualEvent) {
		this.visualEvent = visualEvent;
	}
	/**
	 * @return the frameDate
	 */
	public AlertFieldValue getFrameDate() {
		return frameDate;
	}
	/**
	 * @param frameDate the frameDate to set
	 */
	public void setFrameDate(AlertFieldValue frameDate) {
		this.frameDate = frameDate;
	}
	/**
	 * @return the rule
	 */
	public AlertFieldValue getRule() {
		return rule;
	}
	/**
	 * @param rule the rule to set
	 */
	public void setRule(AlertFieldValue rule) {
		this.rule = rule;
	}
	/**
	 * @return the frameFromDate
	 */
	public Long getFrameFromDate() {
		return frameFromDate;
	}
	/**
	 * @param frameFromDate the frameFromDate to set
	 */
	public void setFrameFromDate(Long frameFromDate) {
		this.frameFromDate = frameFromDate;
	}
	/**
	 * @return the frameToDate
	 */
	public Long getFrameToDate() {
		return frameToDate;
	}
	/**
	 * @param frameToDate the frameToDate to set
	 */
	public void setFrameToDate(Long frameToDate) {
		this.frameToDate = frameToDate;
	}
	public boolean isKnown() {
		return known;
	}
	public void setKnown(boolean known) {
		this.known = known;
	}
	public boolean isPeople() {
		return people;
	}
	public void setPeople(boolean people) {
		this.people = people;
	}
}
