(function () {
    'use strict';

    angular.module('preview.event.image', ['dialog.service'])

    .directive('eventImage', function ($parse, $sce, dialogService) {
        return {
        	'templateUrl': 'app/modules/event-image.html',
            'restrict': 'E',
            'link': function (scope, element, attr) {
            	scope.imageLoading = true;
            	return dialogService.getEventImage(attr.frameId).then(function (response) {
            		scope.imageContent = response.data.image;
            		scope.imageLoading = false;
                });
            }
        };
    });
}());
