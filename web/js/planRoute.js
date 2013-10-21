function TripPlan() {
	this.dayCount = 1;
	this.days = new Array(new TripDay(1));
	this.distance = 0;
	this.elevationGain = 0;
	
	this.addCamp = function() {
		this.dayCount++;
		this.days.push(new TripDay(this.dayCount));
	};
	
	
	this.addSegment = function(segment) {
		this.days[this.dayCount - 1].segments.push(segment);
		this.days[this.dayCount - 1].distance += segment.distance;
		this.days[this.dayCount - 1].elevationGain += segment.elevationGain;
		this.distance += segment.distance;
		this.elevationGain += segment.elevationGain;
	};
	
	
	this.removeSegment = function() {
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
	};
	
	this.getEqualityString = function() {
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
	};
	
	this.getBounds = function() {
		var planBounds = new google.maps.LatLngBounds();
		for ( var i = 0; i < this.dayCount; i++) {
			var day = this.days[i];
			for ( var j = 0; j < day.segments.length; j++) {
				var latLngArray = routesByRouteId.get(day.segments[j].routeId).getLatLngPath();
				
				for (var k = 0; k < latLngArray.length; k++) {
					planBounds.extend(latLngArray[k]);
				}
			}
		}
		return planBounds;
	};
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

var userList = null;
var plansByPlanId = new Hashtable();
var tripsByTripId = new Hashtable();


var tripPlanBuilder = {
		existingPlanId : 0,
		newPlanId : 0,
		newPlan : null,
		newPlanVertices : new Array(),
		newPlanLines : new Array(),
		newPlanCampMarkers : new Array(),
		newPlanLastVertexMarker : null,
		newPlanNeighboringLines : new Array(),
		newPlanRouteIds : new Array(),
		
		showSaveTripForm : function() {
			$("#saveTripPlanChoiceDiv").css("display", "none");
			$("#planRouteActionsDiv").css("display","none");
			$("#saveTripFormDiv").css("display", "block");
			uiState = "creatingTrip";

			if (userList == null) {
				$("#newTripMembersDiv").html('<img src="//cdn.jsdelivr.net/select2/3.4.3/select2-spinner.gif">');
				ajaxGet({
					url : "http://192.241.227.45/api/users/usernames",
					success : function(allUsers) {
						userList = allUsers;
						tripPlanBuilder.createTripMemberSelect();
					}
				});
			} else {
				this.createTripMemberSelect();
			}

			this.checkIfNewTripPlanAlreadyExists("trip");
		},
		
		showSaveTripPlanForm : function() {
			$("#saveTripPlanChoiceDiv").css("display", "none");
			$("#planRouteActionsDiv").css("display","none");
			uiState = "creatingTripPlan";
			this.checkIfNewTripPlanAlreadyExists("plan")
		},
		
		// Hides/empties the various subcomponents of 'saveTripPlanDiv'
		resetSaveTripPlanDiv : function() {
			$("#saveTripPlanChoiceDiv").css("display", "none");
			$("#tripPlanAlreadyExistsDiv").empty();
			$("#submitTripPlanDiv").empty();
			$("#saveTripPlanFormDiv").css("display", "none");
			$("#saveTripFormDiv").css("display", "none");
		},

		// Go from entering details about a trip back to the route planning
		cancelNewTripPlan : function() {
			this.resetSaveTripPlanDiv();
			$("#saveTripPlanChoiceDiv").css("display", "block");
			$("#planRouteActionsDiv").css("display","block");
			uiState = "planningRoute";
		},

		checkIfNewTripPlanAlreadyExists : function(tripOrPlan) {
			this.existingPlanId = 0;
			$("#submitTripPlanDiv").empty();
			$("#tripPlanAlreadyExistsDiv").html('Checking if trip plan already exists: <img src="//cdn.jsdelivr.net/select2/3.4.3/select2-spinner.gif">');

			ajaxGet({
				url : "http://192.241.227.45/api/trips/plans/equalityString/" + tripPlanBuilder.newPlan.getEqualityString(),
				success : function(existingPlan) {
					if (existingPlan == null) {
						$("#tripPlanAlreadyExistsDiv").html("Trip plan hasn't been created before: <img src=\"images/checkmark.png\">");
						$("#saveTripPlanFormDiv").css("display", "block");
						if (tripOrPlan == "plan") {
							$("#submitTripPlanDiv").html(
									'<a href="javascript:tripPlanBuilder.saveTripPlan()">Submit</a> ' +
									'<a href="javascript:tripPlanBuilder.cancelNewTripPlan()">Cancel</a> ');
						}
					} else {
						tripPlanBuilder.existingPlanId = existingPlan.id;
						$("#tripPlanAlreadyExistsDiv").html(
								'Trip Plan already exists : <a href="javascript:tripViewer.setPlan('
										+ existingPlan.id + ')">' + existingPlan.name
										+ "</a><br />"
										+ '<a href="javascript:tripPlanBuilder.cancelNewTripPlan()">Cancel</a> ');
					}
					if (tripOrPlan == "trip") {
						$("#submitTripPlanDiv").html(
								'<a href="javascript:tripPlanBuilder.saveTrip()">Submit</a> ' +
								'<a href="javascript:tripPlanBuilder.cancelNewTripPlan()">Cancel</a> ');
					}
				}
			});
		},

		formatTripUsernames : function(state) {
			if (!state.id)
				return state.text; // optgroup
			if (loggedIn && state.id == siteCredentials.id) {
				return state.text + " (me)";
			}
			return state.text;
		},

		createTripMemberSelect : function() {
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
				formatResult : tripPlanBuilder.formatTripUsernames,
				formatSelection : tripPlanBuilder.formatTripUsernames
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
		},
		
		// if existingPlanId = 0, then its a new plan
		saveTrip : function() {
			var wireTrip = {
				id : 0,
				plan : this.existingPlanId == 0 ? this.getWirePlanForCurrentTrip() : null,
				tripMembers : [],
				planId : this.existingPlanId,
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
		},

		getWirePlanForCurrentTrip : function() {
			var wirePlan = {
				id : 0,
				createdBy : 0,
				segments : [],
				days : this.newPlan.dayCount,
				distance : this.newPlan.distance,
				elevationGain : this.newPlan.elevationGain,
				originalDistance : this.newPlan.distance,
				originalGain : this.newPlan.elevationGain,
				name : $("#newTripPlanName").val(),
				isMappable : 1
			};

			for ( var i = 1; i <= this.newPlan.dayCount; i++) {
				tripDay = this.newPlan.days[i - 1];
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
		},

		saveTripPlan : function() {
			var wirePlan = this.getWirePlanForCurrentTrip();
			ajaxPost({
				url : "http://192.241.227.45/api/trips/plans",
				data : wirePlan,
				success : function(plan) {
					setAlert("Trip Plan Saved.");
				}
			});
		},
		
		// Called as a result of opening the "Plan/Save Trip" tab or clicking "Reset Hike" once already in that tab
		startPlanRoute : function() {
			$("#planRouteActionsDiv").css("display", "block");
			$("#currentPlanDiv").empty();
			this.resetSaveTripPlanDiv();
			uiState = "planningRoute";
			this.newPlan = new TripPlan();
			this.newPlanVertices.length = 0;
			this.clearTripPlanMapUI();
		},

		clearTripPlanMapUI : function() {
			// remove any existing overlays and event listeners, reset everything to empty

			if (this.newPlanLastVertexMarker != null) {
				this.newPlanLastVertexMarker.setMap(null);
				this.newPlanLastVertexMarker = null;
			}
			
			clearMapOverlayArray(this.newPlanCampMarkers);
			clearMapOverlayArray(this.newPlanLines);

			while (this.newPlanNeighboringLines.length > 0) {
				lineToRemove = this.newPlanNeighboringLines.pop();
				google.maps.event.clearInstanceListeners(lineToRemove);
				lineToRemove.setMap(null);
			}

			this.newPlanRouteIds.length = 0;
		},

		// Only used to set the first vertex of the route.
		// Called by clicking on a marker when uiState = "planningRoute"
		addPointToCurrentPlan : function(placeId) {
			if (this.newPlanVertices.length > 0)
				return;
			place = placesByPlaceId.get(placeId);
			this.newPlanVertices.push(placeId);
			this.newPlanLastVertexMarker = new google.maps.Marker({
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
			this.setupNeighboringLines();
		},

		removeLastPointFromPlan : function() {
			if (this.newPlanVertices.length == 0)
				return;
			if (this.newPlanVertices.length == 1) {
				this.startPlanRoute();
				return;
			}

			// We'll eithe remove a camp or a segment
			// if there are some segments in the current day, then remove the last
			// segment
			if (this.newPlan.days[this.newPlan.dayCount - 1].segments.length > 0) {
				this.newPlanVertices.pop();
				this.newPlanLines.pop().setMap(null);
				this.newPlanRouteIds.pop();
				newLastPlace = placesByPlaceId
						.get(this.newPlanVertices[this.newPlanVertices.length - 1]);
				this.newPlanLastVertexMarker.setPosition(newLastPlace.getLatLng());
			} else {
				// remove camp
				this.newPlanCampMarkers.pop().setMap(null);
			}

			this.newPlan.removeSegment(); // knows to remove either segment or camp

			printPlanTable($("#currentPlanDiv"), this.newPlan, true);
			this.setupNeighboringLines();
		},

		addCampToCurrentPlan : function() {
			if (this.newPlanVertices.length < 2)
				return;

			this.newPlan.addCamp();

			lastPlaceId = this.newPlanVertices[this.newPlanVertices.length - 1];
			lastPlace = placesByPlaceId.get(lastPlaceId);

			this.newPlanCampMarkers.push(new google.maps.Marker({
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

			printPlanTable($("#currentPlanDiv"), this.newPlan, true);
		},

		addRouteToCurrentPlan : function(routeId, direction) {
			route = routesByRouteId.get(routeId);
			lastVertexId = direction == 1 ? route.end : route.start;
			lastPlace = placesByPlaceId.get(lastVertexId);
			this.newPlanVertices.push(lastVertexId);
			this.newPlanLastVertexMarker.setPosition(lastPlace.getLatLng());
			this.newPlanLines.push(new google.maps.Polyline({
				path : route.getLatLngPath(),
				strokeColor : "#00F",
				strokeOpacity : 0.5,
				strokeWeight : 8,
				zIndex : 5,
				map : map
			}));
			;

			var newSegment = new TripSegment(routeId, direction, 1);
			this.newPlan.addSegment(newSegment);
			this.newPlanRouteIds.push(routeId);

			printPlanTable($("#currentPlanDiv"), this.newPlan, true);
			this.setupNeighboringLines();

			if (uiState == "planningRoute") {
				$("#saveTripPlanChoiceDiv").css("display", "block");
			}
		},
		
		editTripPlanSegmentMode : function(dayNum, segmentNum, newModeId) {
			this.newPlan.days[dayNum].segments[segmentNum].mode = newModeId;
			printPlanTable($("#currentPlanDiv"), this.newPlan, true);
		},

		// if uiState is "planningRoute", then the mapdata.js handleLineClick() will
		// call this method
		handleNeighboringMapLineClick : function(routeId) {
			route = routesByRouteId.get(routeId);
			var lastPlaceId = this.newPlanVertices[this.newPlanVertices.length - 1];
			if (route.start == lastPlaceId) {
				this.addRouteToCurrentPlan(routeId, 1);
			} else if (route.end == lastPlaceId) {
				this.addRouteToCurrentPlan(routeId, 0);
			}
		},

		setupNeighboringLines : function() {
			var lastPlaceId = this.newPlanVertices[this.newPlanVertices.length - 1];

			// remove existing listeners and lines
			for ( var i = 0; i < this.newPlanNeighboringLines.length; i++) {
				google.maps.event
						.clearInstanceListeners(this.newPlanNeighboringLines[i]);
				this.newPlanNeighboringLines[i].setMap(null);
			}
			this.newPlanNeighboringLines.length = 0;

			// look through all routes to find neighbors of lastPlaceId
			routesByRouteId.each(function(routeId, route) {
				isSameAsLastRoute = routeId == tripPlanBuilder.newPlanRouteIds[tripPlanBuilder.newPlanRouteIds.length - 1];
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
								tripPlanBuilder.addRouteToCurrentPlan(routeId, 1);
							} else {
								tripPlanBuilder.addRouteToCurrentPlan(routeId, 0);
							}
						}
					});
						tripPlanBuilder.newPlanNeighboringLines.push(newLine);
				}
			});
		}

	};

var tripViewer = {
	plan : null,
	planLines : new Array(),
	planCampMarkers : new Array(),
	trip: null,
	
	drawPlan : function() {
		for ( var i = 0; i < this.plan.days.length; i++) {
			var day = this.plan.days[i];
			for ( var j = 0; j < day.segments.length; j++) {
				var segment = day.segments[j];
				var route = routesByRouteId.get(segment.routeId);
				this.planLines.push(new google.maps.Polyline({
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
					this.planCampMarkers.push(new google.maps.Marker({
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
	},
	
	clearUI : function() {
		clearMapOverlayArray(this.planLines);
		clearMapOverlayArray(this.planCampMarkers);
	},
	
	setPlan: function (planId) {
		chooseMenu("details");
		uiState = "viewingTripPlan";
		setURL("Trip Plan " + planId, "/plans/" + planId);
				
		this.clearUI();
		var detailsMenuDiv = $("#itemDetailDiv").empty();
		var planDetailsDiv = $('<div id="detailsMenu_plan"></div>').appendTo(detailsMenuDiv);

		if (plansByPlanId.containsKey(planId)) {
			this.plan = plansByPlanId.get(planId);
			this.drawPlan();
			printPlanTable(planDetailsDiv, this.plan, false);
			map.fitBounds(this.plan.getBounds());
			detailHistoryViewer.addItem("Trip Plan", this.plan.name, planId);
			return;
		}

		ajaxGet({
			url : "http://192.241.227.45/api/trips/plans/" + planId,
			success : function(planData) {
				var jsonPlan = planData.plans[0];
				receiveMapDataIncompleteTiles(planData);
				tripViewer.plan = createPlan(jsonPlan);
				plansByPlanId.put(tripViewer.plan.id, tripViewer.plan);
				tripViewer.drawPlan();
				printPlanTable(planDetailsDiv, tripViewer.plan, false);
				map.fitBounds(tripViewer.plan.getBounds());
				detailHistoryViewer.addItem("Trip Plan", tripViewer.plan.name, planId);
			}
		});
	},

 	setTrip: function (tripId) {
 		chooseMenu("details");
 		uiState = "viewingTrip";
		setURL("Trip " + tripId, "/trips/" + tripId);
	
		this.clearUI();
		var detailsMenuDiv = $("#itemDetailDiv").empty();
		var planDetailsDiv = $('<div id="detailsMenu_plan"></div>');
		var tripDetailsDiv = $('<div id="detailsMenu_trip"></div>');
		planDetailsDiv.appendTo(detailsMenuDiv);
		tripDetailsDiv.appendTo(detailsMenuDiv);
		
		ajaxGet({
			url : "http://192.241.227.45/api/trips/trips/" + tripId,
			success : function(tripData) {
				var jsonTrip = tripData.trips[0];
				var jsonPlan = jsonTrip.plan;
				receiveMapDataIncompleteTiles(tripData);	
				tripViewer.plan = createPlan(jsonPlan);
				plansByPlanId.put(tripViewer.plan.id, tripViewer.plan);
				tripViewer.drawPlan();
				printPlanTable(planDetailsDiv, tripViewer.plan, false);	
				tripViewer.trip = jsonTrip;
				tripsByTripId.put(jsonTrip.id, jsonTrip);			
				tripViewer.printTripDetails(tripDetailsDiv);
				map.fitBounds(tripViewer.plan.getBounds());
				var tripDate = moment.utc(tripViewer.trip.tripDate*1000).format("YYYY-MM-DD");
				detailHistoryViewer.addItem("Trip", tripViewer.plan.name + " ( " + tripDate + " )" , tripId);
			}
		});
 	},
 	
 	printTripDetails : function(domElement) {
 		var tripDate = moment.utc(this.trip.tripDate*1000).format("YYYY-MM-DD");
 		$("<div></div>").html("Trip Date : " + tripDate).appendTo(domElement);
 		var members = $("<div>Trip Members : </div>");
 		for (var i = 0; i < this.trip.tripMembers.length; i++) {
 			if (i > 0) { members.append(" , ") }
 			var member = this.trip.tripMembers[i];
 			if (member.user > 0) {
 				members.append('<a href="javascript:selectUser('+member.user+')">'+member.name+"</a>");
 			} else {
 				members.append(member.name);
 			}
 		}
 		members.appendTo(domElement);
 	}

}

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

// Called when someone clicks on the "Plan/Save Trip" tab
function openPlanMenu() {
	$("#planRouteDiv").css("display", "block");

	uiState = "planningRoute";
	setURL("Planning Trip", "/planningTrip");
	
	tripPlanBuilder.startPlanRoute();
}

function closePlanMenu() {
	$("#planRouteDiv").css("display", "none");
	tripPlanBuilder.clearTripPlanMapUI();
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

/* Draw the table for a trip plan.
 *   domElement should be a jquery object specifying which container to draw the table in.
 *   planToPrint is a TripPlan
 *   isEditMode should be true if the plan can be edited
 */
function printPlanTable(domElement, planToPrint, isEditMode) {
	domElement.empty();

	var currentPlanTable = $('<table></table>').attr({
		id : domElement.attr("id")+"_table"
	}).addClass("hikePlan");

	var headerRow = $(
			"<tr><th>Place</th><th>Elev.</th><th>Route</th><th>Dist.</th><th>Elev. Gain</th><th>Mode</th></tr>")
			.appendTo(currentPlanTable);

	// write the row for the first place
	var firstSegment = planToPrint.days[0].segments[0];
	var firstRoute = routesByRouteId.get(firstSegment.routeId);
	var startingPlace = placesByPlaceId.get(firstSegment.direction == 1 ? firstRoute.start	: firstRoute.end);
	var newRow = $("<tr></tr>").addClass("odd").appendTo(currentPlanTable);
	$("<td></td>").html(startingPlace.name).appendTo(newRow);
	$("<td></td>").html(startingPlace.elevation).appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);
	$("<td></td>").html("").appendTo(newRow);

	for ( var i = 0; i < planToPrint.dayCount; i++) {
		var day = planToPrint.days[i];
		for ( var j = 0; j < day.segments.length; j++) {
			var route = routesByRouteId.get(day.segments[j].routeId);
			var endingPlaceId = day.segments[j].direction == 1 ? route.end : route.start;
			var endingPlace = placesByPlaceId.get(endingPlaceId);
			var newRow = $("<tr></tr>").addClass(j % 2 == 0 ? "even" : "odd").appendTo(currentPlanTable);
			$("<td></td>").html(endingPlace.name).appendTo(newRow);
			$("<td></td>").html(endingPlace.elevation).appendTo(newRow);
			$("<td></td>").html(route.name).appendTo(newRow);
			$("<td></td>").html(day.segments[j].distance / 100.0).appendTo(newRow);
			$("<td></td>").html(day.segments[j].elevationGain).appendTo(newRow);
			(function() {
				var dayNum = i;
				var segmentNum = j;
				var modeElem = $("<td></td>").html(travelModes.get(day.segments[j].mode));

				if (isEditMode) {
					modeElem.append(' <img src="/images/edit_icon.png">');
					modeElem.click(function(event) {
						createChoiceDialog(
							"Select the mode of travel for this trip segment",
							travelModeChoices, 
							function(newModeId) {
								tripPlanBuilder.editTripPlanSegmentMode(dayNum, segmentNum, newModeId);
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

	currentPlanTable.appendTo(domElement);
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
		searchSettings.minL = Math.round(parseFloat($("#minDistance").val()) * 100);
	if ($("#maxDistance").val() != "")
		searchSettings.maxL = Math.round(parseFloat($("#maxDistance").val()) * 100);
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
				'<a href="javascript:tripViewer.setPlan(' + plan.id + ')">Show</a>')
				.appendTo(planRow);
		$("<td></td>").html(plan.name).appendTo(planRow);
		$("<td></td>").html(plan.distance / 100.0).appendTo(planRow);
		$("<td></td>").html(plan.elevationGain).appendTo(planRow);
		$("<td></td>").html(plan.days).appendTo(planRow);
	}

	searchTable.appendTo("#searchResultsTableDiv");
}

var placeViewer = {
	
	setPlace : function(placeId) {
		var place = placesByPlaceId.get(placeId);
		detailHistoryViewer.addItem("Place", place.name, placeId);
		$("#itemDetailDiv").empty();
		$("#itemDetailDiv").html("Name: " + place.name + "<br />" +
    			"Type: " + place.type + "<br />" +
    			"Elevation: " + place.elevation);
	}
};

var detailHistoryViewer = {
	detailHistory : new Array(),
	
	addItem : function(type, title, id) {
		// Check if item already exists
		for (var i = 0; i < this.detailHistory.length; i++) {
			var historyItem = this.detailHistory[i];
			if (historyItem.type == type && historyItem.id == id) {
				var removedItemArr = this.detailHistory.splice(i,1); // remove ith element
				this.detailHistory.unshift(removedItemArr[0]); // insert at head of array
				this.redraw();
				return;
			}
		}
		
		this.detailHistory.unshift({
			type : type,
			title : title,
			id : id}); // insert at start of array
		if (this.detailHistory.length > 10) {
			this.detailHistory.pop();
		}
		this.redraw();
	},
	
	getOnclick : function(type, id) {
		if (type == "Place") {
			return function() { placeViewer.setPlace(id); };
		} else if (type == "Trip Plan") {
			return function() { tripViewer.setPlan(id); };
		} else if (type == "Trip") {
			return function() { tripViewer.setTrip(id); };
		}
	},

	redraw : function() {
		var detailHistoryDiv = $("#detailHistoryDiv").empty();
		
		if (this.detailHistory.length == 0) {
			return;
		}
		
		var header = $("<div><br />Viewing history : </div>").appendTo(detailHistoryDiv);
		
		var historyTable = $('<table></table>')
				.attr({ id : "detailHistoryTable"})
				.addClass("hikePlan");

		var headerRow = $(
				"<tr><th>#</th><th>Item Type</th><th>History Item</th></tr>")
				.appendTo(historyTable);

		for ( var i = 0; i < this.detailHistory.length; i++) {
			var historyItem = this.detailHistory[i];
			var row = $("<tr></tr>").addClass(i % 2 == 0 ? "even" : "odd")
					.appendTo(historyTable);
			$("<td></td>").html((i+1)).appendTo(row);
			$("<td></td>").html(historyItem.type).appendTo(row);
			$("<td></td>").html(historyItem.title)
				.click(this.getOnclick(historyItem.type, historyItem.id))
				.appendTo(row);
		}
		
		historyTable.appendTo(detailHistoryDiv);
	}
	
};

function openDetailsMenu() {
	$("#detailsMenuDiv").css("display","block");
	uiState = "new";
}

function closeDetailsMenu() {
	$("#detailsMenuDiv").css("display","none");
	tripViewer.clearUI();
}

var gpxViewer = {
	
	tracks : [], // isSelected, track (array of LatLng), name, polyline
	
	redrawList : function() {
		var fileListDiv = $("#gpsFileList").empty();
		
		if (this.tracks.length == 0) {
			return;
		}
		
		var header = $("<div><br />GPS Files: </div>").appendTo(fileListDiv);
		
		var fileTable = $('<table></table>')
				.attr({ id : "gpsFileTable"})
				.addClass("hikePlan");

		var headerRow = $("<tr><th></th><th>File Name</th></tr>")
				.appendTo(fileTable);

		for ( var i = 0; i < this.tracks.length; i++) {
			var trackLog = this.tracks[i];
			var row = $("<tr></tr>").addClass(i % 2 == 0 ? "even" : "odd")
					.appendTo(fileTable);
			var inputTd = $("<td></td>").appendTo(row);
			
			var input = $("<input />").attr("id", "gpsFileInput"+i).attr("type","checkbox");
			if (trackLog.isSelected) {
				input.attr("checked","checked");
			}
			input.change((function(idx) { 
					return function() { 
						if ($("#gpsFileInput"+idx).is(":checked")) {
							gpxViewer.tracks[idx].isSelected = true;
							if (gpxViewer.tracks[idx].polyline != null) {
								alert("Polyline should have been null");
							}
							gpxViewer.tracks[idx].polyline = new google.maps.Polyline({
								path : gpxViewer.tracks[idx].track,
								map : map,
								strokeColor: "#F00", 
								strokeWeight: 1
							});
						} else {
							gpxViewer.tracks[idx].isSelected = false;
							if (gpxViewer.tracks[idx].polyline != null) {
								gpxViewer.tracks[idx].polyline.setMap(null);
								gpxViewer.tracks[idx].polyline = null;
							}
						}
					};
				})(i)
			);
			input.appendTo(inputTd);
			
			$("<td></td>").html(trackLog.name).appendTo(row);
		}
		
		fileTable.appendTo(fileListDiv);
	}
		
};

var gpsInputInitialized = false;
function openGpsMenu() {
	$("#gpsMenuDiv").css("display","block");
	
	if (gpsInputInitialized == false) {
		gpsInputInitialized = true;
		$('#gpsFileInput').on("change", function() {
			var files = $("#gpsFileInput")[0].files;
			for (var i = 0; i < files.length; i++) {
				var fileReader = new FileReader();
				fileReader.onload = (function(file) {
					return function(e) {
						var gpsDiv = $("#gpsMenuDiv");
						var gpx = e.target.result;
						var doc = $.parseXML(gpx);
						var trkpts = doc.getElementsByTagName("trkpt");
						
						var trackLog = {
							name : file.name,
							track : []
						};
						
						for (var j = 0; j < trkpts.length; j++) {
							var trkpt = trkpts[j];
							var lat = parseFloat(trkpt.getAttribute("lat"));
							var lon = parseFloat(trkpt.getAttribute("lon"));
							trackLog.track.push(new google.maps.LatLng(lat,lon));
						}
						
						trkpts = null;
						doc = null;
						gpx = null;
						
						gpxViewer.tracks.push(trackLog);
						gpxViewer.redrawList();
					};
				})(files[i]);
				fileReader.readAsText(files[i])
			}
		});
	}
}

function closeGpsMenu() {
	$("#gpsMenuDiv").css("display","none");
}
