// tile = concat(floor((longitude+200)*10),'_', floor((latitude+100)*10)) 

var markersByPlaceId = new Hashtable();
var placesByPlaceId = new Hashtable();

var linesByRouteId = new Hashtable();
var routesByRouteId = new Hashtable();

var placesByTile = new Hashtable();
var routesByTile = new Hashtable();
var tilesDisplayed = new HashSet();
var tilesInMemory = new HashSet();

var isMapDataEnabled = true;

function disableMapData() {
	isMapDataEnabled = false;
	
	markersByPlaceId.each(function(placeId, marker) {
	    marker.setMap(null);
	});
	markersByPlaceId.clear();
	linesByRouteId.each(function(routeId, poly) {
		poly.setMap(null);
	});
	linesByRouteId.clear();
	tilesDisplayed = new HashSet();
}

function enableMapData() {
	if (!isMapDataEnabled) {
		isMapDataEnabled = true;
		processMapMove();
	}
}

function processMapMove() {
	if (!isMapDataEnabled) {
		return false;
	}
	
	if (map.getZoom() < 12) {
		tilesDisplayed.clear();
		removeOffscreenMarkers();
		return;
	}
	
	bounds = map.getBounds();
	
	oldTilesDisplayed = tilesDisplayed;
	newTilesDisplayed = getTilesFromBounds(bounds);
	
	// tiles which were previously offscreen but now onscreen
	newToScreen = newTilesDisplayed.complement(oldTilesDisplayed).values();
	
	// only get tiles that we dont already have in memory
	toFetch = new Array();
	for (var i = 0; i < newToScreen.length; i++) {
		if (!tilesInMemory.contains(newToScreen[i])) {
			toFetch.push(newToScreen[i]);
		}
	}
	
	if(toFetch.length > 0) {
		ajaxGet({
			url: "http://192.241.227.45/api/mapdata/"+join(toFetch),
			requestDetails: toFetch,
			success: receiveMapData
		});
	}

	tilesDisplayed = newTilesDisplayed;
	
	removeOffscreenMarkers();
	updateMarkers();
}

function forceUpdateTilesAndCallback(tiles, nextAction) {
	if (! (tiles instanceof Array)) {
		tiles = new Array(tiles);
	}
	ajaxGet({
		url: "http://192.241.227.45/api/mapdata/"+join(tiles),
		requestDetails: tiles,
		success: function(data) { receiveMapData.call(this,data); nextAction(); }
	});
}

/** Call the server to update the map for an array of tiles */
function forceUpdateTiles(tiles) {
	if (! (tiles instanceof Array)) {
		tiles = new Array(tiles);
	}
	ajaxGet({
		url: "http://192.241.227.45/api/mapdata/"+join(tiles),
		requestDetails: tiles,
		success: receiveMapData
	});
}

/* TODO : If not logged in, then we should get all routes and places with isPublic = 1
 * 
 * If logged in, get routes and places with isPublic == 1 OR createdBy == currentUser
 */


function receiveMapData(data) {
	// clear out old placesByTile entries for these tiles
	routesToRemove = new HashSet();
	for (var idx = 0; idx < this.requestDetails.length; idx++) {
		tile = this.requestDetails[idx];
		
		if (placesByTile.containsKey(tile)) {
			placesByTile.get(tile).each(function(placeId, place) {
				placesByPlaceId.remove(placeId);
			});
		}
		
		placesByTile.remove(tile);
		placesByTile.put(tile, new Hashtable());
		if (routesByTile.containsKey(tile)) {
		    routesToRemove.addAll(routesByTile.get(tile).values());
		}
		if (!tilesInMemory.contains(tile)) {
			tilesInMemory.add(tile);
		}
	}
	routesToRemove = routesToRemove.values();
	
	// remove old routes from tiles
	for (var idx = 0; idx < routesToRemove.length; idx++) {
		route = routesToRemove[idx];
		for (j = 0; j < route.tiles.length; j++) {
			if (routesByTile.containsKey(route.tiles[j])) {
			    routesByTile.get(route.tiles[j]).remove(route.id);
			}
		}
		routesByRouteId.remove(route.id);
	}
	
	for (var idx = 0; idx < data.places.length; idx++) {
		place = createPlace(data.places[idx]);
		if (placesByPlaceId.containsKey(place.id)) {
			oldPlace = placesByPlaceId.get(place.id);
			if (oldPlace.tile != place.tile) {
				if (placesByTile.containsKey(oldPlace.tile)) {
					placesByTile.get(oldPlace.tile).remove(place.id);
				}
			}
		}
		placesByPlaceId.put(place.id,place);
		if (!placesByTile.containsKey(place.tile)) {
			placesByTile.put(place.tile, new Hashtable());
		}
		placesByTile.get(place.tile).put(place.id, place);
	}
	
	for (var idx = 0; idx < data.routes.length; idx++) {
		route = createRoute(data.routes[idx]);
		if (routesByRouteId.containsKey(route.id)) {
			oldRouteTiles = routesByRouteId.get(route.id).tiles;
			for (var j = 0; j < oldRouteTiles.length; j++) {
				if (routesByTile.containsKey(oldRouteTiles[j])) {
					routesByTile.get(oldRouteTiles[j]).remove(route.id);
				}
			}
		}
		routesByRouteId.put(route.id, route);
		for (var j = 0; j < route.tiles.length; j++) {
			if (!routesByTile.containsKey(route.tiles[j])) {
				routesByTile.put(route.tiles[j], new Hashtable());
			}
			routesByTile.get(route.tiles[j]).put(route.id, route);
		}
	}
	
	removeOffscreenMarkers();
	updateMarkers();
}

function receiveMapDataIncompleteTiles(data) {
	for (var idx = 0; idx < data.places.length; idx++) {
		place = createPlace(data.places[idx]);
		if (placesByPlaceId.containsKey(place.id)) {
			oldPlace = placesByPlaceId.get(place.id);
			if (oldPlace.tile != place.tile) {
				if (placesByTile.containsKey(oldPlace.tile)) {
					placesByTile.get(oldPlace.tile).remove(place.id);
				}
			}
		}
		placesByPlaceId.put(place.id,place);
		if (!placesByTile.containsKey(place.tile)) {
			placesByTile.put(place.tile, new Hashtable());
		}
		placesByTile.get(place.tile).put(place.id, place);
	}
	
	for (var idx = 0; idx < data.routes.length; idx++) {
		route = createRoute(data.routes[idx]);
		if (routesByRouteId.containsKey(route.id)) {
			oldRouteTiles = routesByRouteId.get(route.id).tiles;
			for (var j = 0; j < oldRouteTiles.length; j++) {
				if (routesByTile.containsKey(oldRouteTiles[j])) {
					routesByTile.get(oldRouteTiles[j]).remove(route.id);
				}
			}
		}
		routesByRouteId.put(route.id, route);
		for (var j = 0; j < route.tiles.length; j++) {
			if (!routesByTile.containsKey(route.tiles[j])) {
				routesByTile.put(route.tiles[j], new Hashtable());
			}
			routesByTile.get(route.tiles[j]).put(route.id, route);
		}
	}
}

// Removes any markers which are not in "tilesDisplayed"
function removeOffscreenMarkers() {
	toRemove = new Array();
	markersByPlaceId.each(function(placeId, marker) {
		if (!placesByPlaceId.containsKey(placeId)) {
			marker.setMap(null);
			toRemove.push(placeId);
		} else if (!tilesDisplayed.contains(placesByPlaceId.get(placeId).tile)) {
		    marker.setMap(null);
		    toRemove.push(placeId);
		}
	});
	for (var i = 0; i < toRemove.length; i++) {
		markersByPlaceId.remove(toRemove[i]);
	}
	
	toRemove = new Array();
	linesByRouteId.each(function(routeId, poly) {
		if(!routesByRouteId.containsKey(routeId)) {
			poly.setMap(null); toRemove.push(routeId); return;
		}
		routeTiles = new HashSet();
		routeTiles.addAll(routesByRouteId.get(routeId).tiles);
		if (routeTiles.intersection(tilesDisplayed).isEmpty()) {
			poly.setMap(null); toRemove.push(routeId); return;
		}
	});
	for (var i = 0; i < toRemove.length; i++) {
		linesByRouteId.remove(toRemove[i]);
	}
}

function updateMarkers() {
	tilesArray = tilesDisplayed.values();
	for (var i = 0; i < tilesArray.length; i++) {
		tile = tilesArray[i];
		if (placesByTile.containsKey(tile)) {
			placesByTile.get(tile).each(function (placeId, place) {
				if (!markersByPlaceId.containsKey(placeId)) {
				    marker = new google.maps.Marker({
				    	position: place.getLatLng(),
						icon: { path: google.maps.SymbolPath.CIRCLE, 
						  scale: 4, fillColor: "#F00", fillOpacity: 0.8, strokeWeight: 1, strokeOpacity: 1, strokeColor: "#000"
						},
				    	map: map});
				    google.maps.event.addListener(marker, 'click', function() {
				    	handleMarkerClick(placeId);
				    });
				    markersByPlaceId.put(placeId, marker);
				} else {
					// marker already exists. Only replace it if the position has changed
					var existingMarker = markersByPlaceId.get(placeId);
					if (!arePositionsEqual(existingMarker.getPosition(), place.getLatLng())) {
						existingMarker.setPosition(place.getLatLng());
					}
				}
			});
		}
		if (routesByTile.containsKey(tile)) {
			routesByTile.get(tile).each(function (routeId, route) {
				if (!linesByRouteId.containsKey(routeId)) {
					
					options = {strokeColor: "#00F"};
					if (subtypeStyles.containsKey(route.subtype)) {
						options = subtypeStyles.get(route.subtype);
					}
					options.path = route.getLatLngPath();
					options.zIndex = 8;
					options.map = map;
					
					line = new google.maps.Polyline(options);
				    google.maps.event.addListener(line, 'click', function() {
				    	handleLineClick(routeId);
				    });
					linesByRouteId.put(routeId, line);
				} else {
					// if line has changed, create a new line
					
					var existingLine = linesByRouteId.get(routeId);
					var newPath = route.getLatLngPath();
					if (!areLinesEqual(existingLine.getPath().getArray(), newPath)) {
						existingLine.setMap(null);
						existingLine = null;
						linesByRouteId.remove(routeId);
						options = {strokeColor: "#00F"};
						if (subtypeStyles.containsKey(route.subtype)) {
							options = subtypeStyles.get(route.subtype);
						}
						options.path = newPath;
						options.zIndex = 8;
						options.map = map;
						
						line = new google.maps.Polyline(options);
						google.maps.event.addListener(line, 'click', function() {
					    	handleLineClick(routeId);
					    });
						linesByRouteId.put(routeId, line);
					}
				}
			});
		}
	}
}

function handleMarkerClick(placeId) {
	if (uiState == "new") {
    	placeViewer.setPlace(placeId);
	} else if (uiState == "addRoute") {
		setRouteStart(placeId);
	} else if (uiState == "addRoute2") {
		setRouteEnd(placeId);
	} else if (uiState == "planningRoute") {
		tripPlanBuilder.addPointToCurrentPlan(placeId);
	} else if (uiState == "editPlace") {
		setPlaceToEdit(placeId);
	}
}

function handleLineClick(routeId) {
	if (uiState == "editRoute") {
		setRouteToEdit(routeId);
	} else if (uiState == "planningRoute") {
		tripPlanBuilder.handleNeighboringMapLineClick(routeId);
	} else if (uiState == "splitRoute") {
		handleSplitRouteSelectRoute(routeId);
	}
}

function getTileFromLatLng(latLng) {
	x = Math.floor(10*(latLng.lng()+200));
	y = Math.floor(10*(latLng.lat()+100));
	return x+"_"+y;
}

function getTilesFromBounds(bounds) {
	minX = Math.floor(10*(bounds.getSouthWest().lng()+200));
	maxX = Math.floor(10*(bounds.getNorthEast().lng()+200));
	minY = Math.floor(10*(bounds.getSouthWest().lat()+100));
	maxY = Math.floor(10*(bounds.getNorthEast().lat()+100));
	
	newTiles = new HashSet();
	
	for (var x = minX ; x <= maxX; x++) {
		for (var y = minY; y <= maxY; y++) {
			newTiles.add(x+"_"+y);
		}
	}
	return newTiles;
}
