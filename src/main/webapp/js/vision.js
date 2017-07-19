/**
 * Copyright 2016 IBM Corp. All Rights Reserved.
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
Dropzone.autoDiscover = false;

(function () {
  $(document).ready(function () {

    // a global count to generate IDs for the image nodes we will create
    var imageCount = 0;
    
    // the template to render an image and its analysis
    var thumbnailTemplate = Handlebars.compile($("#image-results").html().trim());

    // helper to turn 0..1 floats into percentages
    Handlebars.registerHelper('formatPercent', function (float) {
      return Math.round(float * 100);
    });

    // a placeholder for images being processed.
    // any new image is prepended to the result list.
    function addResultPending(imageId, imageTitle, imageUrl) {
      var context = {
        id: imageId,
        title: imageTitle,
        imageUrl: imageUrl,
        faces: [],
        keywords: [],
        pending: true
      };
      $("#results").prepend(thumbnailTemplate(context));
    }

    // when results are received, we replace the existing box for the image
    function onResultReceived(imageId, context) {
      $("#" + imageId).replaceWith(thumbnailTemplate(context));
    }

    // handle the drag and drop of image, one at a time
    $("#uploadZone").dropzone({
      parallelUploads: 1,
      maxFiles: 1,
      maxFilesize: 1, //MB
      uploadMultiple: false,
      acceptedFiles: "image/*",
      dictDefaultMessage: "Drop an image to analyze here",
      init: function () {

        this.on("maxfilesexceeded", function (file) {
          this.removeFile(file);
        });

        // once the thumbnail is generated, add the image to the result list as Pending
        this.on("thumbnail", function (file, dataUrl) {
          file.imageId = "image-" + (imageCount++);
          file.dataUrl = dataUrl;

          addResultPending(file.imageId, "...", file.dataUrl);
        });

        // when something went wrong while analyzing the image
        this.on("error", function (file, errorMessage) {
          var context = {
            id: file.imageId,
            title: errorMessage,
            imageUrl: file.dataUrl,
            faces: [],
            keywords: []
          }
          onResultReceived(file.imageId, context);
        });

        // analysis received, update the image box
        this.on("success", function (file, response) {
          var context = {
            id: file.imageId,
            title: file.name,
            imageUrl: file.dataUrl,
            faces: response.faces,
            keywords: response.keywords
          }
          onResultReceived(file.imageId, context);
        });

        // remove the image from the drop zone once processed
        this.on("complete", function (file) {
          this.removeFile(file);
        });
      }
    });

    // process an url (entered by the user or as a sample image)
    function processUrl(imageUrl) {
      var imageId = "image-" + (imageCount++);
      addResultPending(imageId, imageUrl, imageUrl);

      $.ajax({
          url: "api/analysis/url",
          type: "POST",
          data: {
            url: imageUrl
          }
        }).fail(function (err) {
          var context = {
            id: imageId,
            title: imageUrl,
            imageUrl: imageUrl,
            faces: [],
            keywords: []
          }
          onResultReceived(imageId, context);
        })
        .done(function (response) {
          var context = {
            id: imageId,
            title: imageUrl,
            imageUrl: imageUrl,
            faces: response.faces,
            keywords: response.keywords
          };
          onResultReceived(imageId, context, response);
        });
    }

    // when clicking a sample image, just pass its src for analysis
    $(".sample-image").on("click", function (e) {
      processUrl($(this).attr("src"));
    });

    // when submitting the form, prevent the default behavior (of page reload)
    // and submit for analysis
    $("#analyze-url-form").submit(function (e) {
      e.preventDefault();
      processUrl($("#image-url").val());
    });
  });
})();
