// Click "Add route" , sets uiState "addRoute"
// Click on start marker , sets uiState "addRoute2"
// Click on end marker , sets uiState "addRoute3"
// Adjust polyline
// Input distance and elevation gain
// Click submit , sets uiState "submittingRoute"

var routeStart = null;
var routeEnd = null;

var routeBeingEdited = null;
var routeIdBeingEdited = 0;
var newRoutePolyline = null;

function startEditRoute() {
	uiState = "editRoute";
	$("#addRouteFormDiv").css("display","block");
	$("#editMapActionsDiv").css("display","none");
	clearAddRouteUI();
	$("#editRouteStep1").css("display","block");
	$("#deleteRouteButtonSpan").css("display","inline");
}

function deleteEditedRoute() {
	if (uiState == "submittingEditedRoute" || routeIdBeingEdited == 0) {
		return;
	}
	
	uiState = "submittingEditedRoute";
	var tilesToUpdate = routeBeingEdited.tiles[0];
	ajaxPost({
		url : "http://192.241.227.45/api/routes/delete",
		data: { x : routeBeingEdited.id },
		success: function(data) {
			routeStart = null;
			routeEnd = null;
			newRoutePolyline.setEditable(false);
			routeIdBeingEdited = 0;
			routeBeingEdited = null;
			newRoutePolyline = null;
			clearAddRouteUI();
			uiState = "new";
			$("#addRouteFormDiv").css("display","none");
			$("#editMapActionsDiv").css("display","block");
			forceUpdateTiles(tilesToUpdate);
		}
	});
}

// Called by handleLickClick in mapData.js
function setRouteToEdit(routeId) {
	routeIdBeingEdited = routeId;
	uiState = "editRoute2";
	routeBeingEdited = routesByRouteId.get(routeId);
	routeStart = placesByPlaceId.get(routeBeingEdited.start);
	routeEnd = placesByPlaceId.get(routeBeingEdited.end);
	setupRouteEditing();
}

function startAddRoute() {
	uiState = "addRoute";
	$("#addRouteFormDiv").css("display","block");
	$("#editMapActionsDiv").css("display","none");
	clearAddRouteUI();	
	$("#addRouteStep1").css("display","block");
}

function clearAddRouteUI() {
	$("#addRouteStep1Status").html("...");
	$("#addRouteStep2Status").html("...");
	$("#editRouteStep1").css("display","none");
	$("#editRouteStep1Done").html("...");
	$("#addRouteStep1").css("display","none");
	$("#addRouteStep2").css("display","none");
	$("#addRouteStep3").css("display","none");
	$("#addRoute_distance").val("");
	$("#addRoute_elevation_gain").val("");
	$("#addRoute_reverse_gain").val("");
	$("#deleteRouteButtonSpan").css("display","none");
	routeStart = null;
	routeEnd = null;
}

function cancelAddRoute() {
	routeStart = null;
	routeEnd = null;
	if (uiState == "addRoute3") {
		newRoutePolyline.setMap(null);
	} else if (uiState == "editRoute2") {
		newRoutePolyline.setEditable(false);
		newRoutePolyline.setPath(routeBeingEdited.getLatLngPath());
		$("#addRoute_type").val(routeBeingEdited.type);
		handleRouteTypeSelection("add");
		$("#addRoute_subtype").val(routeBeingEdited.subtype);
		handleRouteSubTypeSelection("add");
	}
	routeIdBeingEdited = 0;
	routeBeingEdited = null;
	newRoutePolyline = null;
	clearAddRouteUI();	
	uiState = "new";
	$("#addRouteFormDiv").css("display","none");
	$("#editMapActionsDiv").css("display","block");
	
}

function setRouteStart(placeId) {
	routeStart = placesByPlaceId.get(placeId);
	$("#addRouteStep1Status").html(routeStart.name + " ("+routeStart.elevation+') <img src="images/checkmark.png">');
	uiState = "addRoute2";
	$("#addRouteStep2").css("display","block");
}

var newRouteBaseForwardGain = 0;
var newRouteBaseReverseGain = 0;
var newRouteExtraGain = 0;

function updateNewRouteElevationGains() {
	if ($("#addRoute_extra_elevation").val() == "") {
		newRouteExtraGain = 0;
	} else {
	    newRouteExtraGain = parseInt($("#addRoute_extra_elevation").val());
	}
	$("#addRoute_total_elevation_forward").html(newRouteBaseForwardGain+newRouteExtraGain);
	$("#addRoute_total_elevation_reverse").html(newRouteBaseReverseGain+newRouteExtraGain);
}

function setRouteEnd(placeId) {
	routeEnd = placesByPlaceId.get(placeId);
	$("#addRouteStep2Status").html(routeEnd.name + " ("+routeEnd.elevation+') <img src="images/checkmark.png">');
	uiState = "addRoute3";
	routeIdBeingEdited = 0;
	setupRouteEditing();
}

// uiState can be addRoute3 or editRoute2. if routeIdBeingEdited>0 , then we're editing
function setupRouteEditing() {
	$("#addRouteStep3").css("display","block");
	
	if (routeStart.elevation <= routeEnd.elevation) {
		newRouteBaseForwardGain = routeEnd.elevation - routeStart.elevation;
		newRouteBaseReverseGain = 0;
	} else {
		newRouteBaseForwardGain = 0;
		newRouteBaseReverseGain = routeStart.elevation - routeEnd.elevation;
	}
	
	
	$("#addRoute_extra_elevation").keyup(updateNewRouteElevationGains);
	$("#addRoute_base_elevation_forward").html(newRouteBaseForwardGain);
	$("#addRoute_base_elevation_reverse").html(newRouteBaseReverseGain);
	
	if (routeIdBeingEdited > 0) {
		newRoutePolyline = linesByRouteId.get(routeIdBeingEdited);
		
		newRoutePolyline.setEditable(true);
		
		$("#addRoute_distance").val(routeBeingEdited.distance/100.0);
		$("#addRoute_extra_elevation").val(routeBeingEdited.elevationGain-newRouteBaseForwardGain);
		$("#addRoute_name").val(routeBeingEdited.name);
		$("#addRoute_type").val(routeBeingEdited.type);
		handleRouteTypeSelection("add");
		$("#addRoute_subtype").val(routeBeingEdited.subtype);
		handleRouteSubTypeSelection("add");
	} else {	
		newRoutePolyline= new google.maps.Polyline({
		    path: [routeStart.getLatLng(), routeEnd.getLatLng()],
		    strokeColor: "#FF0000",
		    strokeOpacity: 1.0,
		    strokeWeight: 2,
		    map: map,
		    editable: true
		  });
		
		$("#addRoute_extra_elevation").val("0");
		$("#addRoute_name").val("");		
		$("#addRoute_type").val("trail");
		handleRouteTypeSelection("add");
		$("#addRoute_subtype").val("good");
		handleRouteSubTypeSelection("add");
	}
	updateNewRouteLength();
	updateNewRouteElevationGains();
	
	google.maps.event.addListener(newRoutePolyline.getPath(), 'insert_at', validateEndpoints);
	google.maps.event.addListener(newRoutePolyline.getPath(), 'set_at', validateEndpoints);
	google.maps.event.addListener(newRoutePolyline.getPath(), 'remove_at', validateEndpoints);
	
	google.maps.event.addListener(newRoutePolyline, 'rightclick', function(evt) {
		length = newRoutePolyline.getPath().getLength();
		if (evt.vertex > 0 && evt.vertex < (length - 1)) {
			newRoutePolyline.getPath().removeAt(evt.vertex);
			updateNewRouteLength();
		}
	});
}

function submitAddRouteForm() {
	if (uiState != "addRoute3" && uiState != "editRoute2") { return; }
	
	uiState = "submittingRoute";
	
	path = new Array();
	
	for (var i = 0; i < newRoutePolyline.getPath().getLength(); i++) {
		path.push({
			latitude : newRoutePolyline.getPath().getAt(i).lat(),
			longitude : newRoutePolyline.getPath().getAt(i).lng()
			});
	}
	
	newRoute = new Route(
			routeIdBeingEdited, 
			routeStart.id, 
			routeEnd.id,
			Math.round($("#addRoute_distance").val() * 100), 
			newRouteBaseForwardGain + newRouteExtraGain,
			newRouteBaseReverseGain + newRouteExtraGain,
			$("#addRoute_type").val(),
			$("#addRoute_subtype").val(),
			$("#addRoute_name").val(),
			path,
			siteCredentials.id,
			1); 
		
	ajaxPost({
		url : "http://192.241.227.45/api/routes" + (routeIdBeingEdited == 0 ? "" : "/"+routeIdBeingEdited),
		data: newRoute,
		success: function(route) {
			routeStart = null;
			routeEnd = null;
			if (routeIdBeingEdited == 0) {
				newRoutePolyline.setMap(null);
			} else {
				// if we're editing a route, don't delete the line, because it belongs on the map
				newRoutePolyline.setEditable(false);
			}
			routeIdBeingEdited = 0;
			routeBeingEdited = null;
			newRoutePolyline = null;
			clearAddRouteUI();
			uiState = "new";
			$("#addRouteFormDiv").css("display","none");
			$("#editMapActionsDiv").css("display","block");
			forceUpdateTiles(newRoute.tiles[0]);
		}
	});
}

function getRouteLengthInMiles(p) {
	return google.maps.geometry.spherical.computeLength(p.getPath()) / 1609.344;;
}

function updateNewRouteLength() {
	miles = getRouteLengthInMiles(newRoutePolyline);
	$("#addRoute_suggest_distance").html("At least " + (Math.round(miles*100)/100));
}

function validateEndpoints() {
	path = newRoutePolyline.getPath();
	if (! path.getAt(0).equals(routeStart.getLatLng())) {
		path.setAt(0,routeStart.getLatLng());
	}
	if (! path.getAt(path.getLength()-1).equals(routeEnd.getLatLng())) {
		path.setAt(path.getLength()-1, routeEnd.getLatLng());
	}
	updateNewRouteLength();
}

var subtypesByType = new Hashtable();
subtypesByType.put("road",
		[ {text: "Paved Road", val : "paved" },
		  {text: "Easy Dirt Road (2WD)", val : "easy2wd" },
		  {text: "Rough Dirt Road (2WD)", val : "hard2wd" },
		  {text: "High Clearance Only Dirt Road (4WD)", val : "4wd" },
		  {text: "Closed or Gated Road", val : "closed" } ]);
subtypesByType.put("trail",
		[ {text: "Wheelchair Accessible Trail", val : "wide" },
		  {text: "Hiking Trail", val : "good"},
		  {text: "Unofficial or Unmaintained Trail", val : "minor" },
		  {text: "Overgrown or Abandoned Trail", val : "poor" } ]);
subtypesByType.put("open",
		[ {text: "Easy Bushwhack", val : "easy" },
		  {text: "Moderate Bushwhack", val : "moderate" },
		  {text: "Difficult Bushwhack", val : "hard" },
		  {text: "Class 1 (easy walking)", val : "1" },
		  {text: "Class 2 (steeper walking)", val : "2" },
		  {text: "Class 3 (rock scramble)", val : "3" },
		  {text: "Class 4 (difficult scramble)", val : "4" },
		  {text: "Class 5 (technical rock climb)", val : "5" } ]);

subtypeStyles = new Hashtable();

subtypeStyles.put("paved", { strokeColor : "#000", strokeOpacity : 1 });
subtypeStyles.put("easy2wd", { strokeColor : "#964B00", strokeOpacity : 1 });
subtypeStyles.put("hard2wd", { strokeColor : "#964B00", strokeOpacity : 1 });
subtypeStyles.put("4wd", { strokeColor : "#0F0", strokeOpacity : 0,
	icons: [{icon:{path: 'M 0,-1 0,0',strokeOpacity: 1,strokeColor: "#964B00",scale: 4}, offset: '0',repeat: '13px' }] });
subtypeStyles.put("closed", { strokeColor : "#F00", strokeOpacity : 1,
	icons: [{icon:{path: 'M 0,-1 0,0',strokeOpacity: 1,strokeColor: "#964B00",scale: 4}, offset: '0',repeat: '13px' }] });


subtypeStyles.put("wide", { strokeColor : "#F00", strokeOpacity : 1 });
subtypeStyles.put("good", { strokeColor : "#F00", strokeOpacity : 1 });
subtypeStyles.put("minor", { strokeColor : "#F00", strokeOpacity: 1, strokeWeight: 2 });
subtypeStyles.put("poor", { strokeColor : "#0F0", strokeOpacity : 1, strokeWeight: 2,
	icons: [{icon:{path: 'M 0,-1 0,0',strokeOpacity: 1,strokeColor: "#F00",scale: 4, strokeWeight: 2}, offset: '0',repeat: '13px' }] });

subtypeStyles.put("easy", { strokeWeight: 2, strokeColor : "#90EE90", strokeOpacity : 1 });
subtypeStyles.put("moderate", { strokeWeight: 2, strokeColor : "#0F0", strokeOpacity : 1 });
subtypeStyles.put("hard", { strokeWeight: 2, strokeColor : "#006400", strokeOpacity : 1 });

subtypeStyles.put("1", { strokeWeight: 2, strokeColor : "#00F", strokeOpacity : 1 });
subtypeStyles.put("2", { strokeWeight: 2, strokeColor : "#FF0", strokeOpacity : 1 });
subtypeStyles.put("3", { strokeWeight: 2, strokeColor : "#FF7F00", strokeOpacity : 1 });
subtypeStyles.put("4", { strokeWeight: 2, strokeColor : "#808", strokeOpacity : 1 });
subtypeStyles.put("5", { strokeWeight: 2, strokeColor : "#000", strokeOpacity : 1 });

//"add" if it's the add/edit route , "a" or "b" if its one of the new routes in the splitRoute
function handleRouteTypeSelection(uiType) {
	var divPrefix = uiType == "add" ? "#addRoute_" : 
		(uiType == "a" ? "#splitRoute_a_" : "#splitRoute_b_"); 
	var subtypes = subtypesByType.get($(divPrefix+"type").val());
	
	var $el = $(divPrefix+"subtype");
	$el.empty(); // remove old options
	$.each(subtypes, function(idx, option) {
	  $el.append($("<option></option>")
	     .attr("value", option.val).text(option.text));
	});
	if (uiState == "addRoute3" || uiState == "editRoute2" || uiState == "splitRoute3") {
	    handleRouteSubTypeSelection(uiType);
	}
}

function getSubtypeStyleOptions(subtype) {
	var subtypeOptions = subtypeStyles.get(subtype);
	var filledOptions = {};
	if (subtypeOptions.strokeWeight == null) {
		filledOptions.strokeWeight = 4;
	} else {
		filledOptions.strokeWeight = subtypeOptions.strokeWeight;
	}
	if (subtypeOptions.strokeOpacity == null) {
		filledOptions.strokeOpacity = 1;
	} else {
		filledOptions.strokeOpacity = subtypeOptions.strokeOpacity;
	}
	filledOptions.strokeColor = subtypeOptions.strokeColor;
	if (subtypeOptions.icons == null) {
		filledOptions.icons = null;
	} else {
		filledOptions.icons = subtypeOptions.icons;
	}
	return filledOptions;
}

// "add" if it's the add/edit route , "a" or "b" if its one of the new routes in the splitRoute
function handleRouteSubTypeSelection(uiType) {
	if (uiType == "add") {
	    newRoutePolyline.setOptions(getSubtypeStyleOptions($("#addRoute_subtype").val()));
	} else if (uiType == "a") {
		splitRouteALine.setOptions(getSubtypeStyleOptions($("#splitRoute_a_subtype").val()));
	} else if (uiType == "b") {
		splitRouteBLine.setOptions(getSubtypeStyleOptions($("#splitRoute_b_subtype").val()));
	}
}

// Start of SplitRoute code

function startSplitRoute() {
	uiState = "splitRoute";
	$("#editMapActionsDiv").css("display","none");
	$("#splitRouteFormDiv").css("display","block");
	$("#splitRouteStep1").css("display","block");
	$("#splitRouteStep2").css("display","none");
	$("#splitRouteStep3").css("display","none");
	$("#splitRouteStep4").css("display","none");
}

var splitRouteId = 0;
var splitRoute = null;
var splitRouteStart = null;
var splitRouteEnd = null;
var splitRoutePlaceMarker = null;
var splitRouteALine = null;
var splitRouteBLine = null;

function handleSplitRouteSelectRoute(routeId) {
	uiState = "splitRoute2";
	splitRouteId = routeId;
	splitRoute = routesByRouteId.get(routeId);
	splitRouteStart = placesByPlaceId.get(splitRoute.start);
	splitRouteEnd = placesByPlaceId.get(splitRoute.end);
	
	$("#splitRouteStep2").css("display","block");
	
	google.maps.event.addListener(map, 'click', function(event) {
		if (uiState == "splitRoute2") {
		    splitRoutePlaceMarker = new google.maps.Marker({
		    	position: event.latLng, 
		    	map: map
		    });

		    $("#splitRouteStep1").css("display","none");
		    $("#splitRouteStep2").css("display","none");
		    $("#splitRouteStep3").css("display", "block");
		    uiState = "splitRoute3";
		    
		    splitRouteALine = new google.maps.Polyline({
			    path: [splitRouteStart.getLatLng(), event.latLng],
			    map: map,
			    editable: true
			  });
		    splitRouteBLine = new google.maps.Polyline({
			    path: [event.latLng, splitRouteEnd.getLatLng()],
			    map: map,
			    editable: true
			  });
						
			$("#splitRoute_a_type").val(splitRoute.type);
			handleRouteTypeSelection("a");
			$("#splitRoute_a_subtype").val(splitRoute.subtype);
			handleRouteSubTypeSelection("a");
			$("#splitRoute_b_type").val(splitRoute.type);
			handleRouteTypeSelection("b");
			$("#splitRoute_b_subtype").val(splitRoute.subtype);
			handleRouteSubTypeSelection("b");

			$("#splitRoute_a_names").html(splitRouteStart.name + " to New Place");
			$("#splitRoute_b_names").html("New Place to " + splitRouteEnd.name);
			
			$("#splitRoute_old_distance").html(splitRoute.distance/100.0);
			$("#splitRoute_old_gain").html(splitRoute.elevationGain);
			$("#splitRoute_old_reverse_gain").html(splitRoute.reverseGain);
			
			$("#splitRoute_name").keyup(updateSplitRouteFields);
			$("#splitRoute_elevation").keyup(updateSplitRouteFields);
			$("#splitRoute_a_extra_elevation").keyup(updateSplitRouteFields);
			$("#splitRoute_b_extra_elevation").keyup(updateSplitRouteFields);
			$("#splitRoute_a_distance").keyup(updateSplitRouteFields);
			$("#splitRoute_b_distance").keyup(updateSplitRouteFields);
			
			google.maps.event.addListener(splitRouteALine.getPath(), 'insert_at', validateSplitRouteEndpoints);
			google.maps.event.addListener(splitRouteALine.getPath(), 'set_at', validateSplitRouteEndpoints);
			google.maps.event.addListener(splitRouteALine.getPath(), 'remove_at', validateSplitRouteEndpoints);
			google.maps.event.addListener(splitRouteBLine.getPath(), 'insert_at', validateSplitRouteEndpoints);
			google.maps.event.addListener(splitRouteBLine.getPath(), 'set_at', validateSplitRouteEndpoints);
			google.maps.event.addListener(splitRouteBLine.getPath(), 'remove_at', validateSplitRouteEndpoints);
			
			
			google.maps.event.addListener(splitRouteALine, 'rightclick', function(evt) {
				length = newRoutePolyline.getPath().getLength();
				if (evt.vertex > 0 && evt.vertex < (length - 1)) {
					newRoutePolyline.getPath().removeAt(evt.vertex);
					updateSplitRouteFields();
				}
			});
			
		    google.maps.event.clearListeners(map, 'click');
		}
	});
}

function validateSplitRouteEndpoints() {
	path = splitRouteALine.getPath();
	if (! path.getAt(0).equals(splitRouteStart.getLatLng())) {
		path.setAt(0,splitRouteStart.getLatLng());
	}
	if (! path.getAt(path.getLength()-1).equals(splitRoutePlaceMarker.getPosition())) {
		path.setAt(path.getLength()-1, splitRoutePlaceMarker.getPosition());
	}
	path = splitRouteBLine.getPath();
	if (! path.getAt(0).equals(splitRoutePlaceMarker.getPosition())) {
		path.setAt(0,splitRoutePlaceMarker.getPosition());
	}
	if (! path.getAt(path.getLength()-1).equals(splitRouteEnd.getLatLng())) {
		path.setAt(path.getLength()-1, splitRouteEnd.getLatLng());
	}
	updateSplitRouteFields();
}

function updateSplitRouteFields() {
	
	if (!( $("#splitRoute_name").val() == "" || $("#splitRoute_elevation").val() == "")) {
		if ($("#splitRouteStep4").css("display") == "none") {
			$("#splitRouteStep4").css("display","block");
		}
	}
	
	var startElevation = splitRouteStart.elevation;
	var middleElevation = $("#splitRoute_elevation").val();
	var endElevation = splitRouteEnd.elevation;
	
	miles = getRouteLengthInMiles(splitRouteALine);
	$("#splitRoute_a_suggest_distance").html("At least " + (Math.round(miles*100)/100));
	miles = getRouteLengthInMiles(splitRouteBLine);
	$("#splitRoute_b_suggest_distance").html("At least " + (Math.round(miles*100)/100));
	
	aDist = $("#splitRoute_a_distance").val();
	if (aDist == "") aDist = "0";
	bDist = $("#splitRoute_b_distance").val();
	if (bDist == "") bDist = "0";
	aDist = parseFloat(aDist);
	bDist = parseFloat(bDist);
	
	$("#splitRoute_new_distance").html(aDist+bDist);
	
	var extraGainA = 0;
	if ($("#splitRoute_a_extra_elevation").val() != "") {
		extraGainA = parseInt($("#splitRoute_a_extra_elevation").val());
	}
	var extraGainB = 0;
	if ($("#splitRoute_b_extra_elevation").val() != "") {
		extraGainB = parseInt($("#splitRoute_b_extra_elevation").val());
	}
	
	if (middleElevation > 0 && middleElevation < 1000000) {
		if (middleElevation >= startElevation) {
			$("#splitRoute_a_base_elevation_forward").html(middleElevation-startElevation);
			$("#splitRoute_a_total_elevation_forward").html(middleElevation-startElevation+extraGainA);
			$("#splitRoute_a_base_elevation_reverse").html("0");
			$("#splitRoute_a_total_elevation_reverse").html(extraGainA);
		} else {
			$("#splitRoute_a_base_elevation_forward").html("0");
			$("#splitRoute_a_total_elevation_forward").html(extraGainA);
			$("#splitRoute_a_base_elevation_reverse").html(startElevation-middleElevation);
			$("#splitRoute_a_total_elevation_reverse").html(startElevation-middleElevation+extraGainA);
		}
		if (endElevation >= middleElevation) {
			$("#splitRoute_b_base_elevation_forward").html(endElevation-middleElevation);
			$("#splitRoute_b_total_elevation_forward").html(endElevation-middleElevation+extraGainB);
			$("#splitRoute_b_base_elevation_reverse").html("0");
			$("#splitRoute_b_total_elevation_reverse").html(extraGainB);
		} else {
			$("#splitRoute_b_base_elevation_forward").html("0");
			$("#splitRoute_b_total_elevation_forward").html(extraGainB);
			$("#splitRoute_b_base_elevation_reverse").html(middleElevation-endElevation);
			$("#splitRoute_b_total_elevation_reverse").html(middleElevation-endElevation+extraGainB);
		}		
		
		$("#splitRoute_new_gain").html(
				parseInt($("#splitRoute_a_total_elevation_forward").html()) +
				parseInt($("#splitRoute_b_total_elevation_forward").html()));
		$("#splitRoute_new_reverse_gain").html(
				parseInt($("#splitRoute_a_total_elevation_reverse").html()) +
				parseInt($("#splitRoute_b_total_elevation_reverse").html()));
	}
}

function submitSplitRoute() {
	if (uiState != "splitRoute3") return;
	
	uiState = "submittingSplitRoute";
	
	var newRouteA = new Route(
			0, // id
			splitRouteStart.id, // start 
			0, // end
			Math.round($("#splitRoute_a_distance").val() * 100), 
			parseInt($("#splitRoute_a_total_elevation_forward").html()),
			parseInt($("#splitRoute_a_total_elevation_reverse").html()),
			$("#splitRoute_a_type").val(),
			$("#splitRoute_a_subtype").val(),
			splitRoute.name,
			getJavaPathFromPolylinePath(splitRouteALine.getPath()),
			siteCredentials.id,
			1); 

	var newRouteB = new Route(
			0, // id
			0, // start 
			splitRouteEnd.id, // end
			Math.round($("#splitRoute_b_distance").val() * 100), 
			parseInt($("#splitRoute_b_total_elevation_forward").html()),
			parseInt($("#splitRoute_b_total_elevation_reverse").html()),
			$("#splitRoute_b_type").val(),
			$("#splitRoute_b_subtype").val(),
			splitRoute.name,
			getJavaPathFromPolylinePath(splitRouteBLine.getPath()),
			siteCredentials.id,
			1);
	
	var newPlace = new Place(0, 
			$("#splitRoute_name").val(),
			splitRoutePlaceMarker.getPosition().lat(), 
			splitRoutePlaceMarker.getPosition().lng(),
			$("#splitRoute_elevation").val(), 
			$("#splitRoute_type").val(),
			getTileFromLatLng(splitRoutePlaceMarker.getPosition()),
			siteCredentials.id,
			1);
	
	ajaxPost({
		url : "http://192.241.227.45/api/routes/split",
		data: {
			a : newRouteA,
			b : newRouteB,
			o : splitRoute,
			p : newPlace
		},
		success: function(route) {
			var tilesToUpdate = [splitRouteStart.tile, splitRouteEnd.tile, newPlace.tile];
			splitRouteId = 0;
			splitRoute = null;
			splitRouteStart = null;
			splitRouteEnd = null;
			splitRoutePlaceMarker.setMap(null);
			splitRouteALine.setMap(null);
			splitRouteBLine.setMap(null);
			splitRoutePlaceMarker = null;
			splitRouteALine = null;
			splitRouteBLine = null;
			
			uiState = "new";
			$("#splitRouteFormDiv").css("display","none");
			$("#editMapActionsDiv").css("display","block");
			forceUpdateTiles(tilesToUpdate);
		}
	});
}

function cancelSplitRoute() {
	if (splitRoutePlaceMarker instanceof google.maps.Marker) splitRoutePlaceMarker.setMap(null);
	if (splitRouteALine instanceof google.maps.Polyline) splitRouteALine.setMap(null);
	if (splitRouteBLine instanceof google.maps.Polyline) splitRouteBLine.setMap(null);
	splitRoutePlaceMarker = null;
	splitRouteALine = null;
	splitRouteBLine = null;
	splitRouteId = 0;
	splitRoute = null;
	splitRouteStart = null;
	splitRouteEnd = null;
	uiState = "new";
	$("#splitRouteFormDiv").css("display","none");
	$("#editMapActionsDiv").css("display","block");
}


