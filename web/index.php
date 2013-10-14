<html>

<head>

<style>
table.hikePlan  { border-spacing: 0px; border-collapse: collapse; }

table.hikePlan td { 
  border: 1px solid black; 
  border-spacing: 0px;
  font-size: 10pt;
  text-align: center;
  padding: 0px 4px;
}

table.hikePlan th {
  border: 1px solid black; 
  border-spacing: 0px;
  font-size: 10pt;
  font-weight: bold;
  background-color: #c0c0ff;
  padding: 1px 4px;
}

table.hikePlan tr.even {
  background-color: #d0d0d0;
}

table.hikePlan tr.odd {
  background-color: #dddddd;
}

table.hikePlan tr.totals {
  background-color: #c0c0ff;
}

table.hikePlan tr.totals td {
  font-weight: bold;
}

.alertMessage {
  display: none;
  background-color: #ff8888;
  color: black;
  max-width: 400px;
  border: 1px solid red;
  padding: 2px;
  margin: 2px;
  font-size: 10pt;
}

</style>

<script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
<script type="text/javascript" src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://cdn.jsdelivr.net/jquery.cookie/1.3.1/jquery.cookie.js"></script>
<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?libraries=geometry&key=AIzaSyDyoFpxsdIpWMSgTR_eh3wmhnljYmVXnQ8&sensor=false"></script>

<script type="text/javascript" src="/js/jshashtable-3.0.js"></script>
<script type="text/javascript" src="/js/jshashset-3.0.js"></script>


<link type="text/css" rel="stylesheet" href="//cdn.jsdelivr.net/select2/3.4.3/select2.css" />
<script type="text/javascript" src="//cdn.jsdelivr.net/select2/3.4.3/select2.js"></script>
<script type="text/javascript" src="//cdn.jsdelivr.net/select2/3.4.3/select2.min.js"></script>



<link type="text/css" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" rel="Stylesheet" />	

<script type="text/javascript" src="/js/utils.js"></script>
<script type="text/javascript" src="/js/place.js"></script>
<script type="text/javascript" src="/js/addPlace.js"></script>
<script type="text/javascript" src="/js/mapData.js"></script>
<script type="text/javascript" src="/js/route.js"></script>
<script type="text/javascript" src="/js/addRoute.js"></script>
<script type="text/javascript" src="/js/planRoute.js"></script>

<script>

var map;
var uiState = "new";

var loggedIn = false;
var siteCredentials = {};

$(document).ready(function(){
  var mapOptions = {
    center: new google.maps.LatLng(10,-90),
    zoom: 2,
    mapTypeId: google.maps.MapTypeId.TERRAIN
  };
  map = new google.maps.Map(document.getElementById("map_canvas"),
      mapOptions);
  
  google.maps.event.addListener(map, 'idle', processMapMove);
  
  $.cookie.json = true;
  var savedCred = $.cookie("outdoorCred");
  
  if (savedCred != null) {
	  loggedIn = true;
	  siteCredentials = savedCred;
	  
	  $("#loginStatusDiv").html("Logged In : " + siteCredentials.username + ' | <a href="javascript:logout()">Logout</a>');
  } else {
	  $("#loginStatusDiv").empty();
	  $($.parseHTML('Username: <input id="loginUsername" size="30" maxlength="40">  Password: <input id="loginPassword" size="30" maxlength="40"> <a href="javascript:login()">Login</a> | <a href="javascript:openSignupForm()">Sign Up</a>'))
			  .appendTo("#loginStatusDiv");
  }
    
  $( "#signupDialogDiv" ).dialog({
      autoOpen: false,
      height: 350,
      width: 500,
      modal: true,
      buttons: {
        "Submit": function() {
        	var email = $("#signupEmail").val();
        	var username = $("#signupUsername").val();
        	var password = $("#signupPassword").val();
        	
        	ajaxPost(
	        	{
	        		url : "http://192.241.227.45/api/users",
	        		data: { email : email , username : username, password: password },
	        		error: function(message) {
	        			$("#signupDialogMessage").html(message);
	        		},
	        		success: function(data) {
	        			siteCredentials = data;
	        			loggedIn = true;
	        			$("#loginStatusDiv").html("Logged In : " + siteCredentials.username + ' | <a href="javascript:logout()">Logout</a>');
	        			$.cookie("outdoorCred", siteCredentials);
	        			$("#signupDialogDiv").dialog( "close" );
	        		}
	        	}
        	);
        },
        "Cancel": function() {
          $( this ).dialog( "close" );
        }
      }
    });
  
  $("#newTripStartDate").datepicker({changeMonth: true, changeYear: true});
  
  handleInitialURL();
  
  handleRouteTypeSelection();
  
  initializeSearchUI();
});

function login() {
	
	var identifier = $("#loginUsername").val();
	var password = $("#loginPassword").val();
	
	ajaxPost({
        	url : "http://192.241.227.45/api/users/login",
        	data: { identifier : identifier, password: password },
        	error: function(message) {
        		alert(message);
        	},
        	success: function(data) {
        		siteCredentials = data;
        		loggedIn = true;
        		$("#loginStatusDiv").html("Logged In : " + siteCredentials.username + ' | <a href="javascript:logout()">Logout</a>');
        		$.cookie("outdoorCred", siteCredentials);
        	}
        });
}


function logout() {
	siteCredentials = {};
	loggedIn = false;
	$.removeCookie("outdoorCred");
	
	location.reload();
}

function openSignupForm() {
	$("#signupDialogDiv").dialog("open");
}

function handleInitialURL() {
	var initialURL = window.location.pathname;
	if (initialURL.startsWith("/plans")) {
		var planId = parseInt(initialURL.match("\/plans\/(\\d+)")[1]);
		chooseMenu("plan");
		displayPlan(planId);
	}
}

function initializeSearchUI() {
	
	/*
	var distanceSlider = $( "#distanceSlider" ).slider({
		min: 0,
		max: 400,
		range: true,
		values: [Math.log(20)/Math.LN10*100.0,Math.log(100)/Math.LN10*100.0],
		slide: function( event, ui ) {
			$( "#minDistance" ).val(Math.floor(Math.pow(10,ui.values[0]/100.0))/10.0);
			$( "#maxDistance" ).val(Math.floor(Math.pow(10,ui.values[1]/100.0))/10.0);
		}
	});
	$( "#minDistance" ).keyup(function() {
		distanceSlider.slider( "values", [Math.log(this.value*10)/Math.LN10*100.0 , distanceSlider.slider( "option", "values" )[1]]);
	});
	$( "#maxDistance" ).keyup(function() {
		distanceSlider.slider( "values", [distanceSlider.slider( "option", "values" )[0] , Math.log(this.value*10)/Math.LN10*100.0]);
	});
	*/
}


var currentMenuChoice = "search";

var menuChoices = [
  {
	  code : "search",
	  open : openSearchMenu,
	  close : closeSearchMenu
  },
  {
	  code : "plan",
	  open : openPlanMenu,
	  close : closePlanMenu
  },
  {
	  code : "edit",
	  open : openEditMapMenu,
	  close : closeEditMapMenu
  }];

function chooseMenu(newMenuChoice) {
	if (newMenuChoice == currentMenuChoice) return;
	
	for (var i = 0; i < menuChoices.length; i++) {
		if (currentMenuChoice == menuChoices[i].code) {
			menuChoices[i].close();
			break;
		}
	}	
	for (var i = 0; i < menuChoices.length; i++) {
		if (newMenuChoice == menuChoices[i].code) {
			menuChoices[i].open();
			currentMenuChoice = newMenuChoice;
			break;
		}
	}
}
	

</script>


</head>
<body>

<div id="siteHeaderDiv" style="width:100%;">
<table style="width:100%">

<tr><td>Outdoor Tools</td>
<td style="text-align:right" id="loginStatusDiv">

</td>
</tr>
</table>
</div>

<table>
<tr>
<td valign="top">
<div id="map_canvas" style="width:800px;height:600px;"></div>
</td>
<td valign="top">

<div id="mainMenu">
	<a href="javascript:chooseMenu('search')">Search</a> |
	<a href="javascript:chooseMenu('edit')">Edit Map</a> |  
	<a href="javascript:chooseMenu('plan')">Plan/Save Trip</a>
</div>

<div id="clickDetail"></div>

<div id="editMenuDiv" style="display:none">

	<div id="editMapActionsDiv">
	<a href="javascript:startAddPlace()">Add New Place</a><br />
	<a href="javascript:startAddRoute()">Add New Route</a><br />
	<a href="javascript:startPlanRoute()">Start Trip PLan</a><br />
	<a href="javascript:startEditPlace()">Edit a Place</a><br />
	<a href="javascript:startEditRoute()">Edit a Route</a><br />
	<a href="javascript:startSplitRoute()">Split a Route</a>
	</div>

	<div id="splitRouteFormDiv" style="display:none">
	<form name="splitRouteForm">
	<span id="splitRouteStep1">
	Step 1 : Click on the route wish to split: <span id="splitRouteStep1Done"></span>
	<br />
	</span>
	<span id="splitRouteStep2">
	Step 2 : Click on the map to set the location of the new place which will split the existing route into two: <span id="splitRouteStep2Done"></span>
	</span>
	<br />
	<span id="splitRouteStep3">
	Step 3 : Enter Details about the new place ...<br />
	Name : <input type="text" id="splitRoute_name" size=40 maxlength=99><br />
	Elevation : <input type="text" id="splitRoute_elevation"><br />
	Type : <select id="splitRoute_type">
	  <option value="summit">Summit</option>
	  <option value="saddle">Pass / Saddle / Col</option>
	  <option value="ridge">Ridge</option>
	  <option value="junction">Trail Junction</option>
	  <option value="trailhead">Trailhead</option>
	  <option value="water">Water (stream or lake)</option>
	</select>
	<br />
	</span>
	<span id="splitRouteStep4">
	Step 4 : Enter details about the two new routes ...<br />
	Route A : <span id="splitRoute_a_names"></span>
	<br />Distance (miles) : <input id="splitRoute_a_distance"> <span id="splitRoute_a_suggest_distance"></span>
	<table class="hikePlan">
		<tr>
			<td>Direction</td>
			<td>Required Gain</td>
			<td></td>
			<td>Extra Gain<br />(each direction)</td>
			<td></td>
			<td>Total Gain</td></tr>
		<tr>
			<td>Forward</td>
			<td><span id="splitRoute_a_base_elevation_forward"></span></td>
			<td rowspan="2">+</td>
			<td rowspan="2"><input id="splitRoute_a_extra_elevation" size="5"></td>
			<td rowspan="2">=</td>
			<td><span id="splitRoute_a_total_elevation_forward"></span></td>
		</tr>
		<tr>
			<td>Reverse</td>
			<td><span id="splitRoute_a_base_elevation_reverse"></span></td>
			<td><span id="splitRoute_a_total_elevation_reverse"></span></td>
		</tr>
	</table>
	<br />Type : <select onchange="javascript:handleRouteTypeSelection('a')" id="splitRoute_a_type">
	  <option value="trail">Trail</option>
	  <option value="road">Road</option>
	  <option value="open">Open Country</option>
	</select>
	<br />Sub-type : <select onchange="javascript:handleRouteSubTypeSelection('a')" id="splitRoute_a_subtype"></select>
	<br />Route B : <span id="splitRoute_b_names"></span>
	<br />Distance (miles) : <input id="splitRoute_b_distance"> <span id="splitRoute_b_suggest_distance"></span>
	<table class="hikePlan">
		<tr>
			<td>Direction</td>
			<td>Required Gain</td>
			<td></td>
			<td>Extra Gain<br />(each direction)</td>
			<td></td>
			<td>Total Gain</td></tr>
		<tr>
			<td>Forward</td>
			<td><span id="splitRoute_b_base_elevation_forward"></span></td>
			<td rowspan="2">+</td>
			<td rowspan="2"><input id="splitRoute_b_extra_elevation" size="5"></td>
			<td rowspan="2">=</td>
			<td><span id="splitRoute_b_total_elevation_forward"></span></td>
		</tr>
		<tr>
			<td>Reverse</td>
			<td><span id="splitRoute_b_base_elevation_reverse"></span></td>
			<td><span id="splitRoute_b_total_elevation_reverse"></span></td>
		</tr>
	</table>
	<br />Type : <select onchange="javascript:handleRouteTypeSelection('b')" id="splitRoute_b_type">
	  <option value="trail">Trail</option>
	  <option value="road">Road</option>
	  <option value="open">Open Country</option>
	</select>
	<br />Sub-type : <select onchange="javascript:handleRouteSubTypeSelection('b')" id="splitRoute_b_subtype"></select>
	<br />Validation:
	<table class="hikePlan">
		<tr>
			<th></th>
			<th>Distance</th>
			<th>Elev. Gain</th>
			<th>Elev. Gain<br />(reverse)</th>
		</tr>
		<tr>
			<td>Old Route</td>
			<td><span id="splitRoute_old_distance"></span></td>
			<td><span id="splitRoute_old_gain"></span></td>
			<td><span id="splitRoute_old_reverse_gain"></span></td>
		</tr>
		<tr>
			<td>New Route (A+B)</td>
			<td><span id="splitRoute_new_distance"></span></td>
			<td><span id="splitRoute_new_gain"></span></td>
			<td><span id="splitRoute_new_reverse_gain"></span></td>
		</tr>
	</table>
	
	<a href="javascript:submitSplitRoute()">Submit</a>
	</span>
	<a href="javascript:cancelSplitRoute()">Cancel</a>
	
	</form>
	</div>
	
	
	
	<div id="addRouteFormDiv" style="display:none">
	<form name="addRouteForm">
	
	<span id="editRouteStep1" style="display:none">
	Step 1 : Click on a route you wish to edit: <span id="editRouteStep1Done"></span>
	</span>
	<span id="addRouteStep1" style="display:none">
	Step 1 : Click on map marker to set starting location: <span id="addRouteStep1Status"></span>
	</span>
	<br />
	<span id="addRouteStep2">
	Step 2 : Click on map marker to set ending location: <span id="addRouteStep2Status"></span>
	</span>
	<br />
	<span id="addRouteStep3">
	Step 3 : Adjust the line on the map to show the approximate route
	<br />
	Step 4 : Enter Route Details ...
	<br />Distance (miles) : <input id="addRoute_distance"> <span id="addRoute_suggest_distance"></span>
	<table class="hikePlan">
	<tr>
		<td>Direction</td>
		<td>Required Gain</td>
		<td></td>
		<td>Extra Gain<br />(each direction)</td>
		<td></td>
		<td>Total Gain</td></tr>
	<tr>
		<td>Forward</td>
		<td><span id="addRoute_base_elevation_forward"></span></td>
		<td rowspan="2">+</td>
		<td rowspan="2"><input id="addRoute_extra_elevation" size="5"></td>
		<td rowspan="2">=</td>
		<td><span id="addRoute_total_elevation_forward"></span></td>
	</tr>
	<tr>
		<td>Reverse</td>
		<td><span id="addRoute_base_elevation_reverse"></span></td>
		<td><span id="addRoute_total_elevation_reverse"></span></td>
	</tr>
	</table>
	"Required Gain" is the elevation gain calculated by subtracting the elevations of the two endpoints of the route.<br />
	"Extra Gain" is the additional elevation gain needed (in each direction) due to extra bumps in the route.
	
	<br />Name : <input id="addRoute_name">
	
	<br />Type : <select onchange="javascript:handleRouteTypeSelection('add')" id="addRoute_type">
	  <option value="trail">Trail</option>
	  <option value="road">Road</option>
	  <option value="open">Open Country</option>
	</select>
	Sub-type : <select onchange="javascript:handleRouteSubTypeSelection('add')" id="addRoute_subtype"></select>
	<br />
	Step 5 : <a href="javascript:submitAddRouteForm()">Submit</a><span id="deleteRouteButtonSpan"> | <a href="javascript:deleteEditedRoute()">Delete This Route</a></span>
	</span>
	
	</form>
	<br />
	<a href="javascript:cancelAddRoute()">Cancel</a>
	</div>
	
	<div id="editPlaceFormDiv" style="display:none">
	
	Step 1 : Click on an existing place. <span id="editPlaceStep1Done"></span>
	<br />
	<span id="editPlaceStep2" style="display:none">
	Step 2 : Edit any of the fields and/or drag the place marker to a new location.<br />
	<form name="editPlaceForm">
	Name : <input type="text" id="editPlace_name" size=40 maxlength=99><br />
	Elevation : <input type="text" id="editPlace_elevation"><br />
	Type : <select id="editPlace_type">
	  <option value="summit">Summit</option>
	  <option value="saddle">Pass / Saddle / Col</option>
	  <option value="ridge">Ridge</option>
	  <option value="junction">Trail Junction</option>
	  <option value="trailhead">Trailhead</option>
	  <option value="water">Water (stream or lake)</option>
	</select>
	</form>
	<a href="javascript:submitEditPlace()">Submit Changes</a> | <a href="javascript:deleteEditPlace()">Delete This Place</a>
	</span>
	
	<a href="javascript:cancelEditPlace()">Cancel</a>
	</div>
	
	<div id="addPlaceFormDiv" style="display:none">
	
	<form name="addPlaceForm">
	
	Step 1 : Click on map to set location. 
	<span id="addPlaceStep1Done"></span>
	<br />Location : <span id="addPlaceLocation"></span>
	<br />
	<br />
	<span id="addPlaceStep2" style="display:none"">
	Step 2 : Fill in details about new place<br />
	
	Name : <input type="text" id="addPlace_name" size=40 maxlength=99><br />
	Elevation : <input type="text" id="addPlace_elevation"><br />
	Type : <select id="addPlace_type">
	  <option value="summit">Summit</option>
	  <option value="saddle">Pass / Saddle / Col</option>
	  <option value="ridge">Ridge</option>
	  <option value="junction">Trail Junction</option>
	  <option value="trailhead">Trailhead</option>
	  <option value="water">Water (stream or lake)</option>
	</select>
	</form>
	<br />
	Step 3 : <a href="javascript:submitAddPlaceForm()">Submit</a>
	
	</span> <a href="javascript:cancelAddPlace()">Cancel</a>
	
	</div>
</div>

<div id="planRouteDiv" style="display:none">
	<div id="planRouteActionsDiv">
		<a href="javascript:startPlanRoute()">Reset Hike</a> | 
		<a href="javascript:removeLastPointFromPlan()">Remove Last Point</a> |
		<a href="javascript:addCampToCurrentPlan()">Add Camp</a>
	</div>
	<br />
		<div id="currentPlanDiv"></div>
	<div id="saveTripPlanDiv">
		<div id="saveTripPlanChoiceDiv">
			<a href="javascript:showSaveTripPlanForm()">Save As Trip Plan</a> 
			<a href="javascript:showSaveTripForm()">Save As Real Trip</a>
		</div>
		<div id="saveTripFormDiv">
			Trip Start Date : <input type="text" id="newTripStartDate" /><br />
			Extra Distance : <input id="newTripExtraDistance" /><br />
			Extra Elevation Gain : <input id="newTripExtraGain" /><br />
			Trip Members (existing users) <br />
			<span style="margin-left:40px">Site Users:	<span id="newTripMembersDiv"></span></span><br />
			<span style="margin-left:40px">Other People: <span id="newTripNonMembersDiv"></span></span><br />
		</div>
		<div id="saveTripPlanFormDiv">
			Trip Name : <input id="newTripPlanName" type="text" maxlength="100" size="40">
			<br /><a href="javascript:saveTripPlan()">Submit</a>
		</div>
	</div>
	
	
	<div id="createNewTripPlanDiv"><a href="javascript:createNewTripPlan()">Create New Trip Plan</a></div>
</div>

<div id="searchDiv" style="display:none">
	Distance (miles) : <input id="minDistance" type="text" value="0" maxlength="5" size="5"> to
	<input id="maxDistance" type="text" value="1000" maxlength="5" size="5">
	<br />
	Elevation Gain (feet) : <input id="minElevationGain" type="text" value="0" maxlength="6" size="6"> to
	<input id="maxElevationGain" type="text" value="100000" maxlength="6" size="6">
	<br />
	Days : <input id="minTripDays" type="text" value="1" maxlength="3" size="3"> to
	<input id="maxTripDays" type="text" value="100" maxlength="3" size="3">
	<br />
	<a href="javascript:submitSearch()">Get Results</a><br />
	
	
	<div id="searchResultsTableDiv" style="display:none"></div>

</div>

<span id="alertMessage" class="alertMessage"></span>

</td>
</tr>
</table>


<div id="signupDialogDiv" title="Create New Account">

Email : <input id="signupEmail" size="30" maxlength="90"><br />
Username : <input id="signupUsername" size="30" maxlength="40"><br />
Password : <input id="signupPassword" size="30" maxlength="40">

<div id="signupDialogMessage"></div>
</div>

<?php
echo "PHP! <br />";
echo $_SERVER['REQUEST_URI'];

?>





</body>
</html>
