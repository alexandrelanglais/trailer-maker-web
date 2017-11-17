$ ->
  'use strict'
  # Change this to the location of your server-side upload handler:
  url = '/upload'
  $('#fileupload').fileupload(
    url: url
    dataType: 'json'
    done: (e, data) ->
      $.each data.result.files, (index, file) ->
        $('<p/>').text(file.name).appendTo '#files'
        return
      return
    progressall: (e, data) ->
      progress = parseInt(data.loaded / data.total * 100, 10)
      $('#progress .progress-bar').css 'width', progress + '%'
      return
  ).prop('disabled', !$.support.fileInput).parent().addClass if $.support.fileInput then undefined else 'disabled'
  return
