function TripPlan() {
	this.dayCount = 1;
	this.days = new Array(new TripDay(1));
	this.distance = 0;
	this.elevationGain = 0;
	this.addCamp = __addCampToPlan;
	this.addSegment = __addSegmentToPlan;
	this.removeSegment = __removeSegmentFromPlan;
	this.getEqualityString = __getEqualityString;
}

function __addCampToPlan() {
	this.dayCount++;
	this.days.push(new TripDay(this.dayCount));
}

function __addSegmentToPlan(segment) {
	this.days[this.dayCount - 1].segments.push(segment);
	this.days[this.dayCount - 1].distance += segment.distance;
	this.days[this.dayCount - 1].elevationGain += segment.elevationGain;
	this.distance += segment.distance;
	this.elevationGain += segment.elevationGain;
}

function __removeSegmentFromPlan() {
	if (this.days[this.dayCount - 1].segments.length == 0) {
		// remove camp
		this.days.pop();
		this.dayCount--;
	} else {
		removedSegment = this.days[this.dayCount - 1].segments.pop();
		this.days[this.dayCount - 1].distance -= removedSegment.distance;
		this.days[this.dayCount - 1].elevationGain -= removedSegment.elevationGain;
		this.distance -= removedSegment.distance;
		this.elevationGain -= removedSegment.elevationGain;
	}
}

function __getEqualityString() {
	var firstSegment = this.days[0].segments[0];
	var firstRoute = routesByRouteId.get(firstSegment.routeId);
	var equalityString = firstSegment.direction == 1 ? firstRoute.start
			: firstRoute.end;

	for ( var i = 0; i < this.dayCount; i++) {
		if (i > 0) {
			equalityString += ",x";
		}
		var day = this.days[i];
		for ( var j = 0; j < day.segments.length; j++) {
			equalityString += "," + day.segments[j].routeId + ","
					+ day.segments[j].mode;
		}
	}
	return equalityString;
}

function TripDay(daynum) {
	this.daynum = daynum; // 1, 2, 3...
	this.segments = new Array();
	this.distance = 0;
	this.elevationGain = 0;
}

function TripSegment(routeId, direction, modeId) {
	this.routeId = routeId; // routeId
	this.direction = direction; // 0 = backwards, 1 = forwards
	var route = routesByRouteId.get(routeId);
	this.distance = route.distance;
	this.elevationGain = direction == 1 ? route.elevationGain
			: route.reverseGain;
	this.mode = modeId;
}

var plansByPlanId = new Hashtable();

var existingPlanId = 0;
var currentPlanId = 0;
var currentPlanVertices = new Array();
var currentPlanLines = new Array();
var currentPlan = null;
var currentPlanCampMarkers = new Array();
var currentPlanLastVertexMarker = null;
var currentPlanNeighboringLines = new Array();
var currentPlanRouteIds = new Array();

function createPlan(json) {
	var newPlan = new TripPlan();
	newPlan.isMappable = json.isMappable;
	newPlan.name = json.name;
	newPlan.id = json.id;
	newPlan.startingPoint = json.startingPoint;
	newPlan.endingPoint = json.endingPoint;

	for ( var i = 0; i < json.segments.length; i++) {
		var jsonSegment = json.segments[i];
		while (jsonSegment.day > newPlan.dayCount) {
			newPlan.addCamp();
		}
		newPlan.addSegment(new TripSegment(jsonSegment.routeId,
				jsonSegment.direction, jsonSegment.mode));
	}
	return newPlan;
}

function openPlanMenu() {
	$("#planRouteDiv").css("display", "block");
	$("#currentPlanDiv").empty();

	if (currentPlanId > 0) {

		fetchAndDisplayPlan(currentPlanId);
	} else {
		uiState = "planningRoute";
		setURL("Planning Trip", "/planningTrip");
		$("#saveTripPlanDiv").css("display", "block");
		showResetSaveTripPlanDiv();
		$("#planRouteActionsDiv").css("display", "block");
		$("#createNewTripPlanDiv").css("display", "none");
		startPlanRoute();
	}
}

function closePlanMenu() {
	$("#planRouteDiv").css("display", "none");
	clearTripPlanMapUI();
}

function drawCurrentPlan() {
	for ( var i = 0; i < currentPlan.days.length; i++) {
		var day = currentPlan.days[i];
		for ( var j = 0; j < day.segments.length; j++) {
			var segment = day.segments[j];
			var route = routesByRouteId.get(segment.routeId);
			currentPlanLines.push(new google.maps.Polyline({
				path : route.getLatLngPath(),
				strokeColor : "#00F",
				strokeOpacity : 0.5,
				strokeWeight : 8,
				zIndex : 5,
				map : map
			}));
			if (j == 0 && i > 0) {
				var startPlace = placesByPlaceId
						.get(segment.direction == 1 ? route.start : route.end);
				currentPlanCampMarkers.push(new google.maps.Marker({
					position : startPlace.getLatLng(),
					icon : {
						path : google.maps.SymbolPath.CIRCLE,
						scale : 10,
						fillColor : "#0F0",
						fillOpacity : 0.6,
						strokeOpacity : 0
					},
					map : map
				}));
			}
		}
	}

	// TODO : Set map center/zoom on the newly drawn trip plan
}

// Called when another part of the website wants to show a trip plan
function displayPlan(planId) {
	chooseMenu("plan");
	fetchAndDisplayPlan(planId);
}

var currentTripId = 0;
var currentTrip = null;
var tripsByTripId = new Hashtable();

function displayTrip(tripId) {
	setURL("Trip " + tripId, "/trips/" + tripId);
	$("#saveTripPlanDiv").css("display", "none");
	$("#planRouteActionsDiv").css("display", "none");
	$("#createNewTripPlanDiv").css("display", "block");

	clearTripPlanMapUI();
	
	currentTripId = tripId;
	ajaxGet({
		url : "http://192.241.227.45/api/trips/trips/" + tripId,
		success : function(tripData) {
			var jsonTrip = tripData.trips[0];
			var jsonPlan = jsonTrip.plan;

			receiveMapDataIncompleteTiles(tripData);

			currentPlan = createPlan(jsonPlan);
			currentPlanId = currentPlan.id;
			plansByPlanId.put(currentPlan.id, currentPlan);

			drawCurrentPlan();
			updateCurrentPlanTable();

			currentTrip = jsonTrip;
			tripsByTripId.put(jsonTrip.id, jsonTrip);
			
			showCurrentTripDetails();
			
			map.fitBounds(getCurrentPlanBounds());
		}
	});
}

function showCurrentTripDetails() {
	var tripDiv = $("#currentTripDiv").empty();
	var tripDate = moment.utc(currentTrip.tripDate*1000).format("YYYY-MM-DD");
	$("<div></div>").html("Trip Date : " + tripDate).appendTo(tripDiv);
	var members = $("<div>Trip Members : </div>");
	for (var i = 0; i < currentTrip.tripMembers.length; i++) {
		if (i > 0) { members.append(" , ") }
		var member = currentTrip.tripMembers[i];
		if (member.user > 0) {
			members.append('<a href="javascript:selectUser('+member.user+')">'+member.name+"</a>");
		} else {
			members.append(member.name);
		}
	}
	members.appendTo(tripDiv);
}

function getCurrentPlanBounds() {
	var planBounds = new google.maps.LatLngBounds();

	for ( var i = 0; i < currentPlanLines.length; i++) {
		var linePath = currentPlanLines[i].getPath();
		for ( var j = 0; j < linePath.getLength(); j++) {
			planBounds.extend(linePath.getAt(j));
		}
	}
	return planBounds;
}

function fetchAndDisplayPlan(planId) {
	uiState = "viewingTripPlan";
	setURL("Trip Plan " + planId, "/plans/" + planId);
	$("#saveTripPlanDiv").css("display", "none");
	$("#planRouteActionsDiv").css("display", "none");
	$("#createNewTripPlanDiv").css("display", "block");

	clearTripPlanMapUI();

	currentPlanId = planId;

	if (plansByPlanId.containsKey(planId)) {
		currentPlan = plansByPlanId.get(planId);
		drawCurrentPlan();
		updateCurrentPlanTable();
		map.fitBounds(getCurrentPlanBounds());
		return;
	}

	ajaxGet({
		url : "http://192.241.227.45/api/trips/plans/" + planId,
		success : function(planData) {
			var jsonPlan = planData.plans[0];

			receiveMapDataIncompleteTiles(planData);

			currentPlan = createPlan(jsonPlan);
			plansByPlanId.put(currentPlan.id, currentPlan);

			drawCurrentPlan();
			updateCurrentPlanTable();

			map.fitBounds(getCurrentPlanBounds());
		}
	});
}

function showResetSaveTripPlanDiv() {
	$("#saveTripPlanChoiceDiv").css("display", "none");
	$("#tripPlanAlreadyExistsDiv").empty();
	$("#submitTripPlanDiv").empty();
	$("#saveTripPlanFormDiv").css("display", "none");
	$("#saveTripFormDiv").css("display", "none");
}

var userList = null;

function showSaveTripForm() {
	$("#saveTripPlanChoiceDiv").css("display", "none");
	$("#planRouteActionsDiv").css("display","none");
	$("#saveTripFormDiv").css("display", "block");
	uiState = "creatingTrip";

	if (userList == null) {
		$("#newTripMembersDiv")
				.html(
						'<img src="//cdn.jsdelivr.net/select2/3.4.3/select2-spinner.gif">');
		ajaxGet({
			url : "http://192.241.227.45/api/users/usernames",
			success : function(allUsers) {
				userList = allUsers;
				createTripMemberSelect();
			}
		});
	} else {
		createTripMemberSelect();
	}

	checkIfNewTripPlanAlreadyExists("trip");
}

function showSaveTripPlanForm() {
	$("#saveTripPlanChoiceDiv").css("display", "none");
	$("#planRouteActionsDiv").css("display","none");
	uiState = "creatingTripPlan";
	checkIfNewTripPlanAlreadyExists("plan")
}

function cancelNewTripPlan() {
	$("#saveTripPlanChoiceDiv").css("display", "block");
	$("#saveTripFormDiv").css("display", "none");
	$("#submitTripPlanDiv").empty();
	$("#tripPlanAlreadyExistsDiv").empty();
	$("#saveTripPlanFormDiv").css("display", "none");
	$("#planRouteActionsDiv").css("display","block");
	uiState = "planningRoute";
}

function checkIfNewTripPlanAlreadyExists(tripOrPlan) {
	existingPlanId = 0;
	$("#submitTripPlanDiv").empty();
	$("#tripPlanAlreadyExistsDiv")
			.html(
					'Checking if trip plan already exists: <img src="//cdn.jsdelivr.net/select2/3.4.3/select2-spinner.gif">');

	ajaxGet({
		url : "http://192.241.227.45/api/trips/plans/equalityString/"
				+ currentPlan.getEqualityString(),
		success : function(existingPlan) {
			if (existingPlan == null) {
				$("#tripPlanAlreadyExistsDiv")
						.html(
								"Trip plan hasn't been created before: <img src=\"images/checkmark.png\">");
				$("#saveTripPlanFormDiv").css("display", "block");
				if (tripOrPlan == "plan") {
					$("#submitTripPlanDiv").html(
							'<a href="javascript:saveTripPlan()">Submit</a> ' +
							'<a href="javascript:cancelNewTripPlan()">Cancel</a> ');
				}
			} else {
				existingPlanId = existingPlan.id;
				$("#tripPlanAlreadyExistsDiv").html(
						'Trip Plan already exists : <a href="javascript:displayPlan('
								+ existingPlan.id + ')">' + existingPlan.name
								+ "</a><br />"
								+ '<a href="javascript:cancelNewTripPlan()">Cancel</a> ');
			}
			if (tripOrPlan == "trip") {
				$("#submitTripPlanDiv").html(
						'<a href="javascript:saveTrip()">Submit</a> ' +
						'<a href="javascript:cancelNewTripPlan()">Cancel</a> ');
			}
		}
	});

}

function formatTripUsernames(state) {
	if (!state.id)
		return state.text; // optgroup
	if (loggedIn && state.id == siteCredentials.id) {
		return state.text + " (me)";
	}
	return state.text;
}

function createTripMemberSelect() {
	var tripMembersDiv = $("#newTripMembersDiv").empty();
	var tripMembersSelect = $('<select id="newTripMembers"></select>').attr(
			"multiple", "multiple").css("width", "300px").appendTo(
			tripMembersDiv);

	for ( var i = 0; i < userList.length; i++) {
		$("<option></option>").attr("value", userList[i].id).text(
				userList[i].name).appendTo(tripMembersSelect);
	}

	$("#newTripMembers").select2({
		placeholder : "Enter Site Users on this trip",
		formatResult : formatTripUsernames,
		formatSelection : formatTripUsernames
	});

	if (loggedIn) {
		$("#newTripMembers").select2("val", siteCredentials.id);
	}

	var tripNonMembersDiv = $("#newTripNonMembersDiv").empty();
	var tripNonMembersSelect = $('<input id="newTripNonMembers"></input>')
			.attr("type", "hidden").attr("multiple", "multiple").css("width",
					"300px").appendTo(tripNonMembersDiv);

	$("#newTripNonMembers").select2({
		tags : [],
		placeholder : "Enter other people on this trip",
		tokenSeparators : [ "," ]
	});
}

// if existingPlanId = 0, then its a new plan
function saveTrip() {
	var wireTrip = {
		id : 0,
		plan : existingPlanId == 0 ? getWirePlanForCurrentTrip() : null,
		tripMembers : [],
		planId : existingPlanId,
		tripDate : (new Date($("#newTripStartDate").val() + " GMT").getTime())/1000,
		extraDistance : Math.round(parseFloat($("#newTripExtraDistance").val()) * 100),
		extraGain : parseInt($("#newTripExtraGain").val())
	};
	
	var tripMemberUserIds = $("#newTripMembers").select2("val");
	for (var i = 0; i < tripMemberUserIds.length; i++) {
		wireTrip.tripMembers.push({ user : parseInt(tripMemberUserIds[i])});
	}
	var tripNonMemberNames = $("#newTripNonMembers").select2("val");
	for (var i = 0; i < tripNonMemberNames.length; i++) {
		wireTrip.tripMembers.push({ name : tripNonMemberNames[i]});
	}
	
	ajaxPost({
		url : "http://192.241.227.45/api/trips/trips",
		data : wireTrip,
		success : function(trip) {
			uiState = "new";
		}
	});
}

function getWirePlanForCurrentTrip() {
	var wirePlan = {
		id : 0,
		createdBy : 0,
		segments : [],
		days : currentPlan.dayCount,
		distance : currentPlan.distance,
		elevationGain : currentPlan.elevationGain,
		originalDistance : currentPlan.distance,
		originalGain : currentPlan.elevationGain,
		name : $("#newTripPlanName").val(),
		isMappable : 1
	};

	for ( var i = 1; i <= currentPlan.dayCount; i++) {
		tripDay = currentPlan.days[i - 1];
		for ( var j = 0; j < tripDay.segments.length; j++) {
			var newSegment = {
				day : i,
				routeId : tripDay.segments[j].routeId,
				sequence : j + 1,
				direction : tripDay.segments[j].direction,
				mode : tripDay.segments[j].mode
			};
			wirePlan.segments.push(newSegment)
		}
	}
	return wirePlan;
}

function saveTripPlan() {
	var wirePlan = getWirePlanForCurrentTrip();
	ajaxPost({
		url : "http://192.241.227.45/api/trips/plans",
		data : wirePlan,
		success : function(plan) {
			setAlert("Trip Plan Saved.");
		}
	});
}

// Called when user clicks on "Create New Trip Plan" to switch uiState from
// "viewingTripPlan" to "planningRoute";
function createNewTripPlan() {
	uiState = "planningRoute";
	setURL("Planning Trip", "/planningTrip");
	currentPlanId = 0;
	$("#saveTripPlanDiv").css("display", "block");
	showResetSaveTripPlanDiv();
	$("#planRouteActionsDiv").css("display", "block");
	$("#createNewTripPlanDiv").css("display", "none");
	startPlanRoute();
}

function startPlanRoute() {
	$("#planRouteDiv").css("display", "block");
	$("#currentPlanDiv").empty();
	$("#currentTripDiv").empty();
	uiState = "planningRoute";
	currentPlan = new TripPlan();
	currentPlanVertices.length = 0;
	clearTripPlanMapUI();
}

function clearTripPlanMapUI() {
	// remove any existing overlays and event listeners, reset everything to
	// empty

	if (currentPlanLastVertexMarker != null) {
		currentPlanLastVertexMarker.setMap(null);
		currentPlanLastVertexMarker = null;
	}

	while (currentPlanCampMarkers.length > 0) {
		currentPlanCampMarkers.pop().setMap(null);
	}
	while (currentPlanLines.length > 0) {
		currentPlanLines.pop().setMap(null);
	}
	while (currentPlanNeighboringLines.length > 0) {
		lineToRemove = currentPlanNeighboringLines.pop();
		google.maps.event.clearInstanceListeners(lineToRemove);
		lineToRemove.setMap(null);
	}

	currentPlanRouteIds.length = 0;
}

// Only used to set the first vertex of the route.
// Called by clicking on a marker when uiState = "planningRoute"
function addPointToCurrentPlan(placeId) {
	if (currentPlanVertices.length > 0)
		return;
	place = placesByPlaceId.get(placeId);
	currentPlanVertices.push(placeId);
	currentPlanLastVertexMarker = new google.maps.Marker({
		position : place.getLatLng(),
		icon : {
			path : google.maps.SymbolPath.CIRCLE,
			scale : 10,
			fillColor : "#00F",
			fillOpacity : 0.5,
			strokeOpacity : 0
		},
		map : map
	});
	setupNeighboringLines();
}

function removeLastPointFromPlan() {
	if (currentPlanVertices.length == 0)
		return;
	if (currentPlanVertices.length == 1) {
		startPlanRoute();
		return;
	}

	// We'll eithe remove a camp or a segment
	// if there are some segments in the current day, then remove the last
	// segment
	if (currentPlan.days[currentPlan.dayCount - 1].segments.length > 0) {
		currentPlanVertices.pop();
		currentPlanLines.pop().setMap(null);
		currentPlanRouteIds.pop();
		newLastPlace = placesByPlaceId
				.get(currentPlanVertices[currentPlanVertices.length - 1]);
		currentPlanLastVertexMarker.setPosition(newLastPlace.getLatLng());
	} else {
		// remove camp
		currentPlanCampMarkers.pop().setMap(null);
	}

	currentPlan.removeSegment(); // knows to remove either segment or camp

	updateCurrentPlanTable();
	setupNeighboringLines();
}

function addCampToCurrentPlan() {
	if (currentPlanVertices.length < 2)
		return;

	currentPlan.addCamp();

	lastPlaceId = currentPlanVertices[currentPlanVertices.length - 1];
	lastPlace = placesByPlaceId.get(lastPlaceId);

	currentPlanCampMarkers.push(new google.maps.Marker({
		position : lastPlace.getLatLng(),
		icon : {
			path : google.maps.SymbolPath.CIRCLE,
			scale : 10,
			fillColor : "#0F0",
			fillOpacity : 0.6,
			strokeOpacity : 0
		},
		map : map
	}));

	updateCurrentPlanTable();
}

function addRouteToCurrentPlan(routeId, direction) {
	route = routesByRouteId.get(routeId);
	lastVertexId = direction == 1 ? route.end : route.start;
	lastPlace = placesByPlaceId.get(lastVertexId);
	currentPlanVertices.push(lastVertexId);
	currentPlanLastVertexMarker.setPosition(lastPlace.getLatLng());
	currentPlanLines.push(new google.maps.Polyline({
		path : route.getLatLngPath(),
		strokeColor : "#00F",
		strokeOpacity : 0.5,
		strokeWeight : 8,
		zIndex : 5,
		map : map
	}));
	;

	var newSegment = new TripSegment(routeId, direction, 1);
	currentPlan.addSegment(newSegment);
	currentPlanRouteIds.push(routeId);

	updateCurrentPlanTable();
	setupNeighboringLines();

	if (uiState == "planningRoute") {
		$("#saveTripPlanChoiceDiv").css("display", "block");
	}
}

var travelModes = new Hashtable();
travelModes.put(1, "On Foot");
travelModes.put(2, "Bicycle");
travelModes.put(3, "Paddling");

var travelModeChoices = [ {
	id : 1,
	text : "On Foot"
}, {
	id : 2,
	text : "Bicycle"
}, {
	id : 3,
	text : "Paddling"
} ];

function updateCurrentPlanTable() {

	$("#currentPlanDiv").empty();

	currentPlanTable = $('<table></table>').attr({
		id : "currentPlanTable"
	}).addClass("hikePlan");

	var headerRow = $(
			"<tr><th>Place</th><th>Elev.</th><th>Route</th><th>Dist.</th><th>Elev. Gain</th><th>Mode</th></tr>")
			.appendTo(currentPlanTable);

	// write the row for the first place
	var firstSegment = currentPlan.days[0].segments[0];
	var firstRoute = routesByRouteId.get(firstSegment.routeId);
	startingPlace = placesByPlaceId
			.get(firstSegment.direction == 1 ? firstRoute.start
					: firstRoute.end);
	var newRow = $("<tr></tr>").addClass("odd").appendTo(currentPlanTable);
	$("<td></td>").html(startingPlace.name).appendTo(newRow);
	$("<td></td>").html(startingPlace.elevation).appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);

	for ( var i = 0; i < currentPlan.dayCount; i++) {
		day = currentPlan.days[i];
		for ( var j = 0; j < day.segments.length; j++) {
			var route = routesByRouteId.get(day.segments[j].routeId);
			var endingPlaceId = day.segments[j].direction == 1 ? route.end
					: route.start;
			var endingPlace = placesByPlaceId.get(endingPlaceId);
			var newRow = $("<tr></tr>").addClass(j % 2 == 0 ? "even" : "odd")
					.appendTo(currentPlanTable);
			$("<td></td>").html(endingPlace.name).appendTo(newRow);
			$("<td></td>").html(endingPlace.elevation).appendTo(newRow);
			$("<td></td>").html(route.name).appendTo(newRow);
			$("<td></td>").html(day.segments[j].distance / 100.0).appendTo(
					newRow);
			$("<td></td>").html(day.segments[j].elevationGain).appendTo(newRow);
			(function() {
				var dayNum = i;
				var segmentNum = j;
				var modeElem = $("<td></td>").html(
						travelModes.get(day.segments[j].mode));

				if (currentPlanId == 0) {
					modeElem.append(' <img src="/images/edit_icon.png">');
					modeElem
							.click(function(event) {
								createChoiceDialog(
										"Select the mode of travel for this trip segment",
										travelModeChoices, function(newModeId) {
											editTripPlanSegmentMode(dayNum,
													segmentNum, newModeId);
										});
							});
				}
				modeElem.appendTo(newRow);
			})();
		}

		// write the row for the day totals
		var dayTotalRow = $("<tr></tr>").addClass("totals").appendTo(
				currentPlanTable);
		$("<td></td>").attr("colspan", 3).html("Day " + (i + 1) + " Totals")
				.appendTo(dayTotalRow);
		$("<td></td>").html(day.distance / 100.0).appendTo(dayTotalRow);
		$("<td></td>").html(day.elevationGain).appendTo(dayTotalRow);
		$("<td></td>").html("").appendTo(dayTotalRow);
	}

	currentPlanTable.appendTo("#currentPlanDiv");
}

function editTripPlanSegmentMode(dayNum, segmentNum, newModeId) {
	currentPlan.days[dayNum].segments[segmentNum].mode = newModeId;
	updateCurrentPlanTable();
}

// if uiState is "planningRoute", then the mapdata.js handleLineClick() will
// call this method
function handleNeighboringMapLineClick(routeId) {
	route = routesByRouteId.get(routeId);
	var lastPlaceId = currentPlanVertices[currentPlanVertices.length - 1];
	if (route.start == lastPlaceId) {
		addRouteToCurrentPlan(routeId, 1);
	} else if (route.end == lastPlaceId) {
		addRouteToCurrentPlan(routeId, 0);
	}
}

function setupNeighboringLines() {

	var lastPlaceId = currentPlanVertices[currentPlanVertices.length - 1];

	// remove existing listeners and lines
	for ( var i = 0; i < currentPlanNeighboringLines.length; i++) {
		google.maps.event
				.clearInstanceListeners(currentPlanNeighboringLines[i]);
		currentPlanNeighboringLines[i].setMap(null);
	}
	currentPlanNeighboringLines.length = 0;

	// look through all routes to find neighbors of lastPlaceId
	routesByRouteId
			.each(function(routeId, route) {
				isSameAsLastRoute = routeId == currentPlanRouteIds[currentPlanRouteIds.length - 1];
				if (route.start == lastPlaceId || route.end == lastPlaceId) {
					var newLine = new google.maps.Polyline({
						path : route.getLatLngPath(),
						strokeColor : isSameAsLastRoute ? "#00F" : "#FF0",
						strokeOpacity : 0.4,
						strokeWeight : 10,
						zIndex : 10,
						map : map
					});
					// add click listener to line
					google.maps.event.addListener(newLine, "click", function() {
						if (uiState == "planningRoute") {
							if (lastPlaceId == route.start) {
								addRouteToCurrentPlan(routeId, 1);
							} else {
								addRouteToCurrentPlan(routeId, 0);
							}
						}
					});

					currentPlanNeighboringLines.push(newLine);
				}
			});

}

function openSearchMenu() {
	$("#searchDiv").css("display", "block");
}
function closeSearchMenu() {
	$("#searchDiv").css("display", "none");
}

function submitSearch() {
	var searchSettings = {};

	if ($("#minDistance").val() != "")
		searchSettings.minL = Math
				.round(parseFloat($("#minDistance").val()) * 100);
	if ($("#maxDistance").val() != "")
		searchSettings.maxL = Math
				.round(parseFloat($("#maxDistance").val()) * 100);
	if ($("#minElevationGain").val() != "")
		searchSettings.minG = $("#minElevationGain").val();
	if ($("#maxElevationGain").val() != "")
		searchSettings.maxG = $("#maxElevationGain").val();
	if ($("#minTripDays").val() != "")
		searchSettings.minD = $("#minTripDays").val();
	if ($("#maxTripDays").val() != "")
		searchSettings.maxD = $("#maxTripDays").val();

	ajaxGet({
		url : "http://192.241.227.45/api/trips/plans",
		data : searchSettings,
		success : updateSearchPlansResultTable
	});
}

function updateSearchPlansResultTable(data) {

	$("#searchResultsTableDiv").css("display", "block").empty();

	var searchTable = $('<table></table>').attr({
		id : "searchResultsTable"
	}).addClass("hikePlan");

	var headerRow = $(
			"<tr><th></th><th>Name</th><th>Distance</th><th>Elevation Gain</th><th>Days</th></tr>")
			.appendTo(searchTable);

	for ( var i = 0; i < data.length; i++) {
		var plan = data[i];

		var planRow = $("<tr></tr>").addClass(i % 2 == 0 ? "even" : "odd")
				.appendTo(searchTable);
		$("<td></td>").html(
				'<a href="javascript:displayPlan(' + plan.id + ')">Show</a>')
				.appendTo(planRow);
		$("<td></td>").html(plan.name).appendTo(planRow);
		$("<td></td>").html(plan.distance / 100.0).appendTo(planRow);
		$("<td></td>").html(plan.elevationGain).appendTo(planRow);
		$("<td></td>").html(plan.days).appendTo(planRow);
	}

	searchTable.appendTo("#searchResultsTableDiv");
}
