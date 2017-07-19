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

    angular.module('dialog.preview', ['preview.event.image'])

    /**
     * @name preview
     * @module module/preview
     * @description
     *
     * Renders the preview panel within the UI. When a movie is clicked within the list
     * of movie results the controller's "selectedMovie" property is updated. Once selectedMovie
     * contains a movie this directive is invoked. This directive is responsible for rendering
     * the entire preview pane (movie, name, description etc.).
     *
     * @param {object}
     *            content - a reference to movie object
     */
    .directive('preview', function ($parse, $sce) {
        return {
        	'templateUrl': 'app/modules/dialog-preview.html',
            'restrict': 'E',
            'link': function (scope, element, attr) {
                var closeButton = null;
                var resizeContents = function () {
                    var docHeight = $(window).height();
                    var headerHeight = $('#dialog-header').outerHeight(true);
                    var previewParentHeight = $('#preview-parent')[0].scrollHeight;
                    var innerHeaderHeight = $('.dialog-drawer-toggle').outerHeight(true);
                    var previewAvailHeight = 0;
                    if (previewParentHeight === docHeight) {
                        //mobile
                        previewAvailHeight = docHeight - (innerHeaderHeight + 5);
                    }
                    else {
                        //desktop
                        previewAvailHeight = docHeight - (headerHeight + innerHeaderHeight);
                    }
                    if (docHeight < (headerHeight + previewParentHeight)) {
                        //we need to scroll the preview panel
                        $('.dialog-preview-scroll').height(previewAvailHeight);
                    }
                };
                scope.hideReleaseDate = true;
                scope.hideCertification = true;
                scope.playerClass = '';

                closeButton = $('.dialog-drawer-toggle');
                closeButton.bind('touchstart click', function (e) {
                	$('#preview-parent').hide();
                    $(window).off('resize', resizeContents());
                    e.preventDefault();
                    e.stopPropagation();
                });
                $(window).resize(resizeContents());
            }
        };
    });
}());
