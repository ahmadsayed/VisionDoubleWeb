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

    /**
     * @name DialogController
     * @module dialog/controller
     * @description
     *
	 * Controls the state of the Dialog view. At any given point of time, the
	 * Dialog is in one of the following states:
	 *  - initial The "home" view displayed to the user when launching dialog -
	 * chatting The view displayed when user is typing a new response/question -
	 * preview The view is showing a movie preview - favorites When in small
	 * resolutions the favorites panel is displayed
     *
     */
	var DialogController = function(_, $rootScope, $scope, $location,
			$anchorScroll, $timeout , $http , $interval, gettextCatalog, growl, dialogService) {
        var self = this;
        var placeholderText = null;
        var states = {
            'intro': {
                'key': 'intro',
                'class': 'intro',
                'placeholder': 'Loading. Please wait...',
                'introText': ''
            },
            'chatting': {
                'key': 'chatting',
                'class': 'chatting',
                'placeholder': 'Start typing...',
                'introText': ''
            },
            'preview': {
                'key': 'preview',
                'class': 'preview',
                'placeholder': 'Start typing...',
                'introText': ''
            }
        };

        var setState = function (state) {
            self.state = _.cloneDeep(state);
        };

        self.alerts = [];
        self.alertDate = new Date();
		self.selectedRRDocuments = [];
		self.events = [];
		self.previewClosed = false;
		self.searchUrl = "http://localhost:9080/services/lighthouse/cognitive-search?query=portal";
        setState(states.intro);

        $interval(function () {
            self.loadAlerts();
        }, 5000);
        
        //gets the conversation array such that it can be tracked for additions
        self.conversation = dialogService.getConversation();
        self.question = null;

        if (!self.placeHolder) {
            //if we haven't received the placeholder, make a call to initChat API to get welcome message
            self.placeHolder = (function () {
            	dialogService.me().then(function (response) {
            		self.user = {
            				photo: response.profileImageTitle,
            				cn: response.userName
            		};
//            		var bgImage = "url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAWAAAAFgCAMAAACyv1N+AAADK2lUWHRvcGVuYmFkZ2VzAAAAAAB7IkBjb250ZXh0IjoiaHR0cHM6Ly93M2lkLm9yZy9vcGVuYmFkZ2VzL3YxIiwidHlwZSI6IkFzc2VydGlvbiIsImlkIjoiaHR0cHM6Ly93d3cueW91cmFjY2xhaW0uY29tL2FwaS92MS9vYmkvYmFkZ2VfYXNzZXJ0aW9ucy81Yzk0ZTFkYy1jOWQyLTQxMTYtODI5MS1hMWJiZDg4MWIyMDMiLCJ1aWQiOiI1Yzk0ZTFkYy1jOWQyLTQxMTYtODI5MS1hMWJiZDg4MWIyMDMiLCJyZWNpcGllbnQiOnsidHlwZSI6ImVtYWlsIiwiaWRlbnRpdHkiOiJzaGEyNTYkOTBkYjRhYjc2YTNlZTAxN2Q3NDNkMmNhYzU0MjNhOTIyNjQwNzlhODQ4MmFjM2UwY2ViZDg2ZjAwM2RiOGZiZCIsImhhc2hlZCI6dHJ1ZX0sImltYWdlIjoiaHR0cHM6Ly9hY2NsYWltLXByb2R1Y3Rpb24tYXBwLnMzLmFtYXpvbmF3cy5jb20vaW1hZ2VzLzhjN2E0ZDhiLTFjOWQtNDQzOS1iN2M4LWZhMzEwZmY3MjQxYy8xLnBuZyIsImV2aWRlbmNlIjoiaHR0cHM6Ly93d3cueW91cmFjY2xhaW0uY29tL2JhZGdlcy81Yzk0ZTFkYy1jOWQyLTQxMTYtODI5MS1hMWJiZDg4MWIyMDMiLCJpc3N1ZWRPbiI6IjIwMTYtMDYtMTNUMDA6MDA6MDAuMDAwWiIsImJhZGdlIjoiaHR0cHM6Ly93d3cueW91cmFjY2xhaW0uY29tL2FwaS92MS9vYmkvYmFkZ2VfY2xhc3Nlcy9jOThmZGM1Yy1iNWY1LTRjMDAtYmQ2MS0yZGIwZWZhYmVkZGUiLCJ2ZXJpZnkiOnsidHlwZSI6Imhvc3RlZCIsInVybCI6Imh0dHBzOi8vd3d3LnlvdXJhY2NsYWltLmNvbS9hcGkvdjEvb2JpL2JhZGdlX2Fzc2VydGlvbnMvNWM5NGUxZGMtYzlkMi00MTE2LTgyOTEtYTFiYmQ4ODFiMjAzIn19ySdMLQAAAaRQTFRFAAAAAIVxAIVxAIVxAIVxAIVxAIVxAIVxAIVxAIVxAIVxAIVx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\")";
            		var bgImage = "url(\"" + self.user.photo + "\")";
            		
            		self.avatarStyle = {
        				content: " ",
        				position: "absolute",
        				right: "0px; top: 0px",
        				height: "100%",
        				width: "55px",
        				display: "inline-block",
        				"background-image": bgImage,
        				"background-repeat": "no-repeat",
	    				"background-size": "100% 100%",
        				"background-position": "45%",
        				"border-radius": "70px"
            		};
            		
                    return dialogService.initChat(self.user.cn).then(function (response) {                    
                    	self.updateUI(response);
                    });
            	});                
            }());
        }
        
      //Watch the conversation array.. If a segment is added then update the state
        $scope.$watch(function () {
            return self.conversation;
        }, function () {
            // We have a new response, switch to 'answered' state
            if (!_.isEmpty(self.conversation)) {
            	
            	self.userInputType = self.conversation[self.conversation.length - 1].userInputType;
            	if(self.userInputType === 'TEXT_FIELD')
        		{
	            	$('#question').removeAttr('disabled');
	                $('#question').focus();
        		} else{
        			$('#question').attr('disabled', 'disabled');
        		}
            	if (self.conversation.length === 1) {
                   states.intro.introText = self.conversation[0].responses;
                    $('body').addClass('dialog-body-running');
                    if (self.state.key !== states.preview.key) {
                        setState(states.chatting);
                    }
                }
                if (self.conversation[self.conversation.length - 1].rrDocuments != null) {
					self.selectedRRDocuments = self.conversation[self.conversation.length - 1].rrDocuments;
					self.loadPreview();
				}
                
                if (self.conversation[self.conversation.length - 1].events != null 
                		&& self.conversation[self.conversation.length - 1].events[0] != null
                		&& self.conversation[self.conversation.length - 1].events[0].frameId != null) {
					self.events = self.conversation[self.conversation.length - 1].events;
					setState(states.preview);
		    		$('#preview-parent').show();
		    		self.previewClosed = false;
				}
            }
        }, true);
        
        self.changeStateToChatting = function () {
        	self.previewClosed = true;
        	setState(states.chatting);
        };
        
        self.submitOptions = function(){
        	self.userFilterSelections = self.selectedOptions;
        	
        	self.selectedOptions.forEach(function (option, index, array) {
        		self.selectedOptions[index] = "" + option + "";
        	});
        	
            self.question = self.selectedOptions.join(","); 
            $('#question').val(self.question);
            
            delete self.selectedOptions;
            
            self.submit();
        };
        
        self.submitLink = function (textToSubmit) {
        	if(textToSubmit === 'Start over'){
        		self.startOver();
        	} else{
	            $('#question').val(textToSubmit);
	            self.question = textToSubmit;
	            
	            self.userFilterSelections = [];
	            self.userFilterSelections.push(textToSubmit);
	            
	            self.submit();
        	}
        };
        
        self.submitEvent = function (event) {
            $('#question').val(event.frameId);
            self.question = event.frameId;
            self.userFilterSelections = [];
            self.userFilterSelections.push(event.frameId);
            
            self.submit();
            var filteredEvents = [];
            for (var index = 0 ; index < self.events.length ; index ++ ) {
            	if(self.events[index].frameId != event.frameId) {
            		filteredEvents.push(self.events[index]);
            	}
            }
            self.events = filteredEvents;
            if(self.events.length == 0) {
            	self. changeStateToChatting();
            }
        };
        
        self.submitDate = function () {
            $('#question').val(self.datepicker);
            self.question = self.datepicker;
            self.datepicker = "";
            
            self.submit();
        };
        
		self.startOver = function() {
			self.events = [];
			setState(states.chatting);
			return dialogService.startOver(self.user.cn).then(function (response) {                    
            	self.updateUI(response);
            });
		};
        
        /**
         * Submits the current question using dialogService
         */
        self.submit = function () {
            var timeout = null;
            
            if (!self.question || self.question.length === 0) {
                $('#question').focus();
                return;
            }
            if (self.conversation.length > 1 && self.conversation[self.conversation.length - 1].options) {
                self.conversation[self.conversation.length - 1].options = null;
            }
            $('#question').attr('disabled', '');
            timeout = $timeout(function () {
                    var scrollable = $('#scrollable-div');
                    if (scrollable[0]) {
                        scrollable[0].scrollTop = scrollable[0].scrollHeight;
                    }
                }, 500);

            dialogService.query(self.question, self.userFilterSelections, true).then(function (response) {
            	self.updateUI(response);
            	delete self.userFilterSelections;
            });
            delete self.question;
        };
        
        self.updateUI = function(response){
        	var child = null;
            var footer = null;
            var timeout = null;

        	$('#question').removeAttr('disabled');
            
        	$('#question').val('');
            if ($.isArray(response)) {
                response = response[response.length - 1];
                //If we are displaying movies on a mobile device (less than 750 tall) we do
                //not want to put focus into the field! (we don't want the keyboard popping up)
                if (!response.rrDocuments || $(window).height() > 750) {
                    $('#question').focus();
                }
            }
            //This is not a great hack, but the only fix I could find for compensating
            //for the width of the scrollbars. When the scrollbar appears it
            if ($('#scrollable-div').prop('clientHeight') < $('#scrollable-div').prop('scrollHeight')) {
                child = document.getElementById('resize-footer-col');
                child.style.display = 'table-cell';
                footer = document.getElementById('dialog-footer');
                footer.style.overflowY = 'scroll';
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(function () {
                    var scrollableDiv = $('#scrollable-div');
                    child.style.display = 'none';
                    if (scrollableDiv[0]) {
                        scrollableDiv[0].scrollTop = scrollableDiv[0].scrollHeight;
                    }
                 }, 500);
            }
            else {
                child = document.getElementById('resize-footer-col');
                child.style.display = 'table-cell';
                footer = document.getElementById('dialog-footer');
                footer.style.overflowY = 'hidden';
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(function () {
                    var scrollableDiv = $('#scrollable-div');
                    child.style.display = 'none';
                    if (scrollableDiv[0]) {
                        scrollableDiv[0].scrollTop = scrollableDiv[0].scrollHeight;
                    }
                }, 500);
            }
        };

        self.switchToChatting = function () {
            $location.path('chatting');
        };

        $scope.$on('$viewContentLoaded', function (next, current) {
            if (placeholderText) {
                $('#question').removeAttr('disabled');
                $('#question').focus();
            }
        });

		self.loadPreview = function() {
			setState(states.preview);
			$('#preview-parent').show();
			self.previewClosed = false;
			
		};
		

		self.loadAlerts = function() {
					$http({ method: 'POST', url: '/vision-double-web/rest/events/filter'
						, data : {"selector": 
						         {"frameTime" : 
					         		{
						       	    "$gt": self.alertDate.getTime()
						       	   }
						         	}
						         }})
		            .success(function(data, status) {
		                self.alerts = data;
		                
		                var alertData = self.alerts.alerts;
		                console.debug(alertData.length);
		                for(var index = 0 ; index < alertData.length ; index ++) {
		                	if(self.alertDate.getTime() < alertData[index].frameTime ) {
		                		self.alertDate = new Date(alertData[index].frameTime);
		                	}
		                	if(alertData[index].rule == "1") {
//		                		growl.warning("Warning: " + alertData[index].frameTitle);
		                		growl.warning("Unknown person captured from " + alertData[index].cameraId);
		                	} else {
		                		growl.info(alertData[index].faceIdentificationClasses[0].name + " captured from " + alertData[index].cameraId);
		                	}
		                }
		             })
		            .error(function(data, status) {
		                console.debug("Error");
		            });
				};
		    };

    angular.module('dialog.controller', [ 'gettext', 'lodash', 'ngRoute', 'ngSanitize', 'ngAnimate', 'angular-growl', 'dialog.service' ]).config(
            function ($routeProvider) {
                $routeProvider.when('/', {
                    'templateUrl': 'app/modules/dialog.html',
                    'reloadOnSearch': false
                }).when('/chatting', {
                    'templateUrl': 'app/modules/dialog.html',
                    'reloadOnSearch': false
                });
            }).controller('DialogController', DialogController);
}());
