$ ->
  $('#btnMakeTrailer').prop('disabled', true)

  startMonitoring = (videoref) ->
    $.ajax
      url: '/progress/' + videoref
      type: 'post'
      dataType: 'json'
      contentType: "application/json; charset=utf-8"
      success: (data, s) =>
        $('#inputs').html('')
        if !data.complete
          $('<div class="col"/>').text(data.line).appendTo '#inputs'
          setTimeout ( -> startMonitoring videoref), 500
        else
          window.location.href = '/download/' + data.line.substr(data.line.indexOf(':') + 1)
      error: (data, s, e) ->
        setTimeout ( -> startMonitoring videoref), 500
        console.log(data.responseText)

  url = '/upload'
  $('#fileupload').fileupload(
    url: url
    dataType: 'json'
    done: (e, data) ->
        $('#noFileError').text('')
        if data.result.name == ''
            $('#files').html('')
            $('<p class="error"/>').text(data.result.description).appendTo '#files'
            $('#btnMakeTrailer').prop('disabled', true)
        else
            $('#files').html('')
            $('<p class="success"/>').text(data.result.description).appendTo '#files'
            $('#btnMakeTrailer').prop('disabled', false)

        $('#videoref').val(data.result.name)
        return
    progressall: (e, data) ->
      progress = parseInt(data.loaded / data.total * 100, 10)
      $('#progress .progress-bar').css 'width', progress + '%'
      return
  ).prop('disabled', !$.support.fileInput).parent().addClass if $.support.fileInput then undefined else 'disabled'


  $('#makeTrailer').on('submit', (e) ->
    e.preventDefault
    if $('#videoref').val() == ''
        $('#noFileError').text('Please upload a file')
        return false

    indexed_array = {}
    $.map($('#makeTrailer').serializeArray(), (n, i) ->
            indexed_array[n['name']] = n['value'];
    )
    console.log(JSON.stringify(indexed_array))
    $.ajax
      url: '/trailers/mta'
      type: 'post'
      dataType: 'json'
      contentType: "application/json; charset=utf-8"
      data: JSON.stringify(indexed_array)
      success: (data, s) ->
        $('#inputs').html('')
        $('<div class="col"/>').text("Your trailer is being prepared").appendTo '#inputs'
        $('#btnMakeTrailer').prop('disabled', true)
        setTimeout ( -> startMonitoring data.videoref), 1000
      error: (data, s, e) ->
        $('#inputs').html(data.responseText)
        console.log(data.responseText)

    return false
  )


