$(document).ready ->
	# Basic Examples
	$.get '/trailers/get', (data) ->
		$('#progress').append "Successfully got the page."

#	$.post '/',
#		userName: 'John Doe'
#		favoriteFlavor: 'Mint'
#		(data) -> $('#progress').append "Successfully posted to the page."
#
#	# Advanced Settings
#	$.ajax '/',
#		type: 'GET'
#		dataType: 'html'
#		error: (jqXHR, textStatus, errorThrown) ->
#			$('#progress').append "AJAX Error: #{textStatus}"
#		success: (data, textStatus, jqXHR) ->
#			$('#progress').append "Successful AJAX call: #{data}"