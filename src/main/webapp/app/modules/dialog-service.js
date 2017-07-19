/* Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function () {
    'use strict';

    angular.module('dialog.service', [])

    /**
     * @name dialogService
     * @module dialog/service
     * @description
     *
     * Implements the dialogService interface using the Watson Theaters App API to interface with the
     * Watson Dialog Service (WDS) and themoviedb.org's movie API.
     */
    .service('dialogService', function (_, $http, $q) {
        
        var welcomeMessage;
        var index = 0;
        var conversation = [];
        var userName;
        var conversationContext;

        /**
         * Gets all entries (responses) in the conversation so far.
         *
         * @public
         * @return {object[]} All entries in the conversation.
         */
        var getConversation = function () {
            return conversation;
        };
        
        var buildRequestBody = function(inputMsg, userFilterSelections){
        	var text = {text : inputMsg};
        	if(userFilterSelections)
        		conversationContext["USER_FILTER_SELECTIONS"] = userFilterSelections;
            var requestBody = {input: text, context: conversationContext};
            
            return requestBody;
        };
        
        var me = function () {
       	 return $http.get('rest/user/me/double-vision-cam').then(function (response) {
       		 	userName = response.data.userName;
       			return response.data;
			}, function (errorResponse) {
	       	 	console.log("Unable to load user data.");
	       	 	return {"userName":"John Doe","profileImageTitle":"Profile_John Doe.png"};
            });
        };
        
        var startOver = function (userName) {
	       	 conversation.push({
	            'message': "Start over",
                'isVisible': true,
	            'index': index++
	       	 });
	           	
	   		 conversationContext = {};
	       	 conversationContext["USER_NAME"] = userName.split(" ")[0];
	       	 conversationContext["SELECTED_CAMERA"] = "double-vision-cam";
	       	 
	       	 var input = buildRequestBody("");
	       	 return $http.post('rest/conversation/converse', input).then(function (response) {
	       		 conversationContext = response.data.context;
	       		 
	       		 conversation.forEach(function (segment) {
	                    if (segment.index === index - 1) {
	                        segment.responses = response.data.output.text;
	                        segment.choices = conversationContext.OPTIONS_BUTTONS;
	                        segment.userInputType = conversationContext.USER_INPUT;
	                    }
	                });
	        	}, function (errorResponse) {
	                 var data = errorResponse;
	                 if (errorResponse) {
	                     data = data.data;
	                     return {
	                         'welcomeMessage': data.userErrorMessage
	                     };
	                }
	            });
        };
        
        var getEventImage = function(frameId){
        	return $http.get('rest/events/image/'+frameId).then(function (response) {
        			return response;
	        	}, function (errorResponse) {
	                 return "";
	            });
        };
        
        var initChat = function (userName) {
             if (conversationContext) {
                 return $q.when({
                     'conversationContext': conversationContext
                 });
             }
             else {
            	 conversation.push({
                     'message': input,
                     'index': index++
                 });
            	 if(userName)
        		 {
            		 conversationContext = {};
                	 conversationContext["USER_NAME"] = userName.split(" ")[0];
                	 conversationContext["SELECTED_CAMERA"] = "double-vision-cam";
        		 }
            	 var input = buildRequestBody("");
            	 return $http.post('rest/conversation/converse', input).then(function (response) {
            		 conversationContext = response.data.context;
            		 
            		 conversation.forEach(function (segment) {
                         if (segment.index === index - 1) {
//                        	 var welcomeOpening = [].concat(response.data.output.text);
                             segment.responses = response.data.output.text;
                             segment.choices = conversationContext.OPTIONS_BUTTONS;
                             segment.userInputType = conversationContext.USER_INPUT;
//                             segment.userInputType = response.data.context.USER_INPUT;
//                             segment.userInputType = 'TEXT_FIELD';
                         }
                     });
             	}, function (errorResponse) {
                      var data = errorResponse;
                      if (errorResponse) {
                          data = data.data;
                          return {
                              'welcomeMessage': data.userErrorMessage
                          };
                     }
                 });
             }
         };
         
         var getResponse = function (question, userFilterSelections) {
         	var requestBody = buildRequestBody(question, userFilterSelections);
             return $http.post('rest/conversation/converse', requestBody).then(function (response) {
             	
            	 conversationContext = response.data.context;
                 var watsonResponse = null;
                 var events = null;
                 var segment = null;
                 var choices = null;
                 var multiSelect = null;
                 
                 
                 if ($.isArray(response.data.output.payload)) {
                     events = response.data.output.payload;
                 }
                 if ($.isArray(conversationContext.OPTIONS_BUTTONS)) {
                	 choices = conversationContext.OPTIONS_BUTTONS;
                 }
                 
                 if ($.isArray(conversationContext.OPTIONS_MULTI_SELECT)) {
                	 multiSelect = conversationContext.OPTIONS_MULTI_SELECT;
                 }
                 
                 if ($.isArray(response.data.output.text)) {
                 	watsonResponse = response.data.output.text.join('<br />').replace("{}", " ");;
                 } else{
                 	watsonResponse = response.data.output.text;
                 }
                     
                 segment = {
                         'message': question,
                         'responses': watsonResponse,
                         'events': events,
                         'choices': choices,
                         'multiSelect': multiSelect,
                         'userInputType': conversationContext.USER_INPUT
//                         'userInputType': 'TEXT_FIELD'
                     };
                 return segment;
             }, function (error) {
                 var response = error.data.userErrorMessage;
                 if (!response) {
                     response = 'Failed to get valid response from the Dialog service. Please refresh your browser';
                 }
                 return {
                     'message': question,
                     'responses': response
                 };
             });
         };

        /**
         * A (public) utility method that ensures initChat is called and returns before calling the getResponse API.
         *
         * @public
         * @return {object[]} An array of chat segments.
         */
        var query = function (input, userFilterSelections, visible) {
        	conversation.push({
                'message': input,
                'isVisible': visible,
                'index': index++
            });
            return initChat().then(function () {
                var response = $q.when();
                response = response.then(function (res) {
                    if (res) {
                        conversation.push(res);
                    }
                    return getResponse(input, userFilterSelections);
                });
                return response;
            }, function (error) {
                var segment = {};
                segment.responses = 'Error received from backend system. Please refresh the browser to start again.';
                conversation.push(segment);
            }).then(function (lastRes) {
                if (lastRes) {
                    conversation.forEach(function (segment) {
                        if (segment.index === index - 1) {
                            segment.responses = lastRes.responses;
                            segment.events = lastRes.events;
                            segment.choices = lastRes.choices;
                            segment.multiSelect= lastRes.multiSelect;
                            segment.userInputType = lastRes.userInputType;
                        }
                    });
                }
                return conversation;
            });
        };

        return {
            'getConversation': getConversation,
            'initChat': initChat,
            'startOver': startOver,
            'getEventImage': getEventImage,
            'me': me,
            'query': query
        };
    });
}());
