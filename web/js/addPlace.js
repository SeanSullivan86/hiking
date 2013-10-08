// Click "Add Place" (sets uiState "addPlace")
// Step 1 : Click on location on map (sets uiState "addPlace2")
// Step 2 : Fill in details
// Step 3 : Submit (sets uiState "submittingPlace")
// Step 3b : Cancel (sets uiState "new")

var addPlaceLatLng = null;
var addPlaceMarker = null;

function openEditMapMenu() {
	setURL("Edit Map","/editMap");
	$("#editMenuDiv").css("display","block");
	$("#editMapActionsDiv").css("display","block");
}
function closeEditMapMenu() {
	$("#editMenuDiv").css("display","none");
	cancelAddPlace();
	cancelEditPlace();
	cancelAddRoute();
	cancelSplitRoute();
}

// Precondition : In uiState "new"
function startAddPlace() {
	uiState = "addPlace";
	$("#addPlaceFormDiv").css("display","block");
	$("#editMapActionsDiv").css("display","none");
	$("#addPlaceLocation").html(".....");
	$("#addPlace_name").val("");
	$("#addPlace_elevation").val("");
	$("#addPlaceStep1Done").html("");
	$("#addPlaceStep2").css("display","none");
	addPlaceLatLng = null;
	addPlaceMarker = null;
	
	google.maps.event.addListener(map, 'click', function(event) {
		if (uiState == "addPlace") {
		    addPlaceLatLng = event.latLng;
		    $("#addPlaceLocation").html(addPlaceLatLng.lat() + " , " + addPlaceLatLng.lng());
		    addPlaceMarker = new google.maps.Marker({position: event.latLng, map: map});

		    $("#addPlaceStep1Done").html('<img src="images/checkmark.png">');
		    $("#addPlaceStep2").css("display", "block");
		    uiState = "addPlace2";
		}
		    
	});
}

function cleanUpAddPlaceUI() {
	google.maps.event.clearListeners(map, 'click');
}

function submitAddPlaceForm() {
	if (uiState != "addPlace2") { return; }
	
	uiState = "submittingPlace";
	
	var newPlace = new Place(0, 
			$("#addPlace_name").val(),
			addPlaceLatLng.lat(), 
			addPlaceLatLng.lng(), 
			$("#addPlace_elevation").val(), 
			$("#addPlace_type").val(),
			getTileFromLatLng(addPlaceLatLng),
			siteCredentials.id,
			1);
	
	addPlaceMarker.setMap(null);

	ajaxPost({
		url : "http://192.241.227.45/api/places",
		data: newPlace,
		success: function(data) {
			forceUpdateTiles(new Array(data.places[0].tile));
			cleanUpAddPlaceUI();
			$("#addPlaceFormDiv").css("display","none");
			$("#editMapActionsDiv").css("display","block");
			uiState = "new";
		}
	});
}

function cancelAddPlace() {
	if (addPlaceMarker instanceof google.maps.Marker) {
		addPlaceMarker.setMap(null);
	}
	addPlaceMarker = null;
	cleanUpAddPlaceUI();
	$("#addPlaceFormDiv").css("display","none");
	$("#editMapActionsDiv").css("display","block");
	uiState = "new";
}


// Start Edit Place Code

var editPlaceMarker = null;
var placeBeingEdited = null;

function startEditPlace() {
	uiState = "editPlace";
	$("#editPlaceFormDiv").css("display","block");
	$("#editMapActionsDiv").css("display","none");
	$("#editPlaceStep1Done").html("");
	$("#editPlaceStep2").css("display","none");
}

function setPlaceToEdit(placeId) {
	uiState = "editPlace2";
    $("#editPlaceStep1Done").html('<img src="images/checkmark.png">');
    $("#editPlaceStep2").css("display", "block");
	placeBeingEdited = placesByPlaceId.get(placeId);
	
	$("#editPlace_name").val(placeBeingEdited.name);
	$("#editPlace_elevation").val(placeBeingEdited.elevation);
	$("#editPlace_type").val(placeBeingEdited.type);
	
	editPlaceMarker = new google.maps.Marker({
		position: placeBeingEdited.getLatLng(),
		draggable: true,
		map: map});
}

function submitEditPlace() {
	var updatedPlace = new Place(placeBeingEdited.id, 
			$("#editPlace_name").val(),
			editPlaceMarker.getPosition().lat(), 
			editPlaceMarker.getPosition().lng(), 
			$("#editPlace_elevation").val(), 
			$("#editPlace_type").val(),
			getTileFromLatLng(
					new google.maps.LatLng(
							editPlaceMarker.getPosition().lat(), 
							editPlaceMarker.getPosition().lng())),
			siteCredentials.id,
			1);
	
	uiState = "submittingEditedPlace";
	
	ajaxPost({
		url : "http://192.241.227.45/api/places/"+updatedPlace.id,
		data: updatedPlace,
		success: function(data) {
			forceUpdateTiles(new Array(data.places[0].tile));
			editPlaceMarker.setMap(null);
			editPlaceMarker = null;
			placeBeingEdited = null;
			$("#editPlaceFormDiv").css("display","none");
			$("#editMapActionsDiv").css("display","block");
			$("#editPlaceStep1Done").html("");
			$("#editPlaceStep2").css("display","none");
			uiState = "new";
		}
	});
}

function deleteEditPlace() {
	
	var connectedRoutes = 0;
	routesByRouteId.each(function(routeId, route) {
		if (route.start == placeBeingEdited.id || route.end == placeBeingEdited.id) {
			connectedRoutes++;
		}
	});
	
	if (connectedRoutes > 0) {
		setAlert("You may not delete a place while it still has routes connected to it.");
		return;
	}
	
	removeAlerts();
	
	uiState = "submittingEditedPlace";
	var tileToUpdate = placeBeingEdited.tile;
	ajaxPost({
		url : "http://192.241.227.45/api/places/delete",
		data: { x : placeBeingEdited.id },
		success: function(data) {
			forceUpdateTiles(tileToUpdate);
			editPlaceMarker.setMap(null);
			editPlaceMarker = null;
			placeBeingEdited = null;
			$("#editPlaceFormDiv").css("display","none");
			$("#editMapActionsDiv").css("display","block");
			$("#editPlaceStep1Done").html("");
			$("#editPlaceStep2").css("display","none");
			uiState = "new";
		}
	});
	
}

function cancelEditPlace() {
	if (editPlaceMarker instanceof google.maps.Marker) {
		editPlaceMarker.setMap(null);
	}
	editPlaceMarker = null;
	placeBeingEdited = null;
	$("#editPlaceFormDiv").css("display","none");
	$("#editMapActionsDiv").css("display","block");
	$("#editPlaceStep1Done").html("");
	$("#editPlaceStep2").css("display","none");
	
	removeAlerts();
	uiState = "new";
}

