

function createChoiceDialog(dialogDescription, dialogChoices, handleResponse) {
	var newDialog = $("<div></div>").attr("id","newModalDialog");
	newDialog.html(dialogDescription);
	for (var i = 0; i < dialogChoices.length; i++) {
		(function() {
			var dialogChoice = dialogChoices[i];
			var newChoice = $("<div></div>")
					.html(dialogChoice.text)
			        .button()
			        .css("display","block")
			        .click(function (event) {
			            newDialog.dialog("close");
			            newDialog.remove();
			        	handleResponse(dialogChoice.id);
			        }).appendTo(newDialog);
		})();
	}
	newDialog.dialog({
      autoOpen: true,
      modal: true,
      open: function() { $(".ui-dialog-titlebar-close", $(this).parent()).hide(); }
    });
}


// Specify options "url", "success"
function ajaxGet(options) {
	$.ajax({
		async : true,
		type : "GET",
		headers : {"Authorization" : loggedIn ? (siteCredentials.id + " " + siteCredentials.hash) : "0"},
		contentType : "application/json",
		dataType : "json",
		data : options.data,
		url : options.url,
		requestDetails : options.requestDetails,
		timeout : 5000,
		error : function(jqXHR, textStatus) {
			if (options.error != null) {
				options.error(textStatus);
			} else {
				alert(JSON.stringify(jqXHR) + " : " + textStatus);
			}
		},
		success : function(data) {
			if (data.success) {
				options.success(data.response);
			} else {
				if (options.error != null) {
					options.error(data.message);
				} else {
					alert(data.message);
				}
			}
			
		}
	});
}

// Specify url, data, success
function ajaxPost(options) {
	$.ajax({
		async : true,
		type : "POST",
		headers : {"Authorization" : loggedIn ? (siteCredentials.id + " " + siteCredentials.hash) : "0"},
		data : JSON.stringify(options.data),
		contentType : "application/json",
		dataType : "json",
		url : options.url,
		timeout : 5000,
		error : function(jqXHR, textStatus) {
			if (options.error != null) {
				options.error(textStatus);
			} else {
				alert(JSON.stringify(jqXHR) + " : " + textStatus);
			}
		},
		success : function(data) {
			if (data.success) {
				options.success(data.response);
			} else {
				if (options.error != null) {
					options.error(data.message);
				} else {
					alert(data.message);
				}
			}
			
		}
	});
}

function join(vals) {
	str = "";
	for (var i = 0; i < vals.length; i++) {
		if (i > 0) str = str + ",";
		str = str + vals[i];
	}
	return str;
}

function arePositionsEqual(x,y) {
	if (Math.abs(x.lat() - y.lat()) > 0.000001) return false;
	if (Math.abs(x.lng() - y.lng()) > 0.000001) return false;
	return true;
}

function areLinesEqual(x,y) {
	if (x.length != y.length) return false;
	for (var i = 0; i < x.length; i++) {
		if (!arePositionsEqual(x[i],y[i])) return false;
	}
	return true;
}

// x is an MVCArray of LatLng's 
function getJavaPathFromPolylinePath(x) {
	var path = new Array();
	
	for (var i = 0; i < x.getLength(); i++) {
		path.push({
			latitude : x.getAt(i).lat(),
			longitude : x.getAt(i).lng()
			});
	}
	return path;
}

function setAlert(str) {
	$("#alertMessage").css("display","block");
	$("#alertMessage").html(str);
}

function removeAlerts() {
	$("#alertMessage").css("display","none");
}

function setURL(title, url) {
	history.replaceState({},title,url);
}

if (!String.prototype.startsWith) {
	  Object.defineProperty(String.prototype, 'startsWith', {
	    enumerable: false,
	    configurable: false,
	    writable: false,
	    value: function (searchString, position) {
	      position = position || 0;
	      return this.indexOf(searchString, position) === position;
	    }
	  });
	}

if(!('contains' in String.prototype))
	  String.prototype.contains = function(str, startIndex) { return -1 !== String.prototype.indexOf.call(this, str, startIndex); };