$ ->
	$("#go").click ->
		url = $("#url").val().replace(/"/g, "").trim()
		if url.length > 0
			if url.indexOf("http://disc.org") == 0
				$("#warn").modal("show")
			else
				$.ajax
					url: "/"
					type: "POST"
					dataType: "json"
					contentType: "application/json"
					data: JSON.stringify([url])
					success: (resp) ->
						$("#result").val(resp)
						$resdiv = $("#resdiv")
						if $resdiv.css("display") == "none"
							$resdiv.toggle(300)
						else
							$resdiv.toggle(50)
							$resdiv.toggle(50)
						$.ajax
							url: "/count"
							type: "GET"
							contentType: "application/json"
							success: (count) ->
								$kt = $("#kt")
								$kt.empty()
								$kt.append(document.createTextNode("" + count))
							error: (jqXHR, errorText, error) ->
								#alert("darn, " + errorText)
					error: (jqXHR, errorText, error) ->
						#alert("oops, " + errorText)

	$("#redir").click ->
		url = $("#result").val()
		index = url.lastIndexOf("/")
		code = url.substring(index + 1)
		redirUrl = "/" + code
		$rform = $("#rform")
		$rform.get(0).setAttribute("action", redirUrl)
		$rform.submit()

	$("#url").keypress (e) ->
		if e.which == 13
			jQuery(this).blur()
			$("#go").focus().click()
			false
