/**
 * (C) Copyright IBM Corp. 2016. All Rights Reserved.
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

public class Constants {

	public static final String CONTEXT_ACTION_PROPERTY_NAME = "ACTION";

	public static final String CONTEXT_ACTION_RETRIEVE_EVENTS = "RETRIEVE_EVENTS";
	public static final String CONTEXT_ACTION_RETRIEVE_USER_DEFINED_OBJECTS = "RETRIEVE_USER_DEFINED_OBJECTS";
	public static final String CONTEXT_ACTION_CLASSIFY_AS = "CLASSIFY_AS";
	public static final String CONTEXT_ACTION_CLASSIFY_NEW_ASSOCIATE = "CLASSIFY_NEW_ASSOCIATE";
	public static final String CONTEXT_ACTION_RETRIEVE_EVENTS_FOR_ASSOCIATE = "RETRIEVE_EVENTS_FOR_ASSOCIATE";
	public static final String CONTEXT_ACTION_REMOVE_RULE = "REMOVE_RULE";
	public static final String CONTEXT_ACTION_CONFIGURE_RULE = "CONFIGURE_RULE";
	public static final String CONTEXT_ACTION_RETRIEVE_ASSOCIATE_RULE = "RETRIEVE_ASSOCIATE_RULE";
	
	public static final String CONTEXT_OBJECT_PROPERTY_KEY = "OBJECT";
	public static final String CONTEXT_STATE_PROPERTY_KEY = "STATE";
	public static final String CONTEXT_FROM_DATE_PROPERTY_KEY = "FROM_DATE";
	public static final String CONTEXT_TO_DATE_PROPERTY_KEY = "TO_DATE";
	
	public static final int IMAGES_NO_TO_TRAIN = 10;
	public static final int MIN_NO_ASSOCIATES_TO_TRAIN = 2;
}
