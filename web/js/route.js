function Route(id, start, end, distance, elevationGain, reverseGain, type, subtype, name, path, createdBy, isPublic) {
  this.id = id;
  this.start = start;
  this.end = end;
  this.distance = distance;
  this.elevationGain = elevationGain;
  this.reverseGain = reverseGain;
  this.path = path; // array containing {latitude: , longitude: } objects
  this.type = type;
  this.subtype = subtype;
  this.name = name;
  this.createdBy = createdBy;
  this.isPublic = isPublic;
  
  bounds = new google.maps.LatLngBounds();
  for (var i = 0; i < path.length; i++) {
	  bounds.extend(new google.maps.LatLng(path[i].latitude, path[i].longitude));
  }
  
  this.tiles = getTilesFromBounds(bounds).values();
  
  this.getLatLngPath = __getLatLngPath;
}

function createRoute(json) {
	return new Route(json.id, json.start, json.end, json.distance, json.elevationGain, json.reverseGain, json.type, json.subtype, json.name, json.path, json.createdBy, json.isPublic); 
}

function __getLatLngPath() {
	x = new Array();
	for (var i = 0; i < this.path.length; i++) {
		x.push(new google.maps.LatLng(this.path[i].latitude, this.path[i].longitude));
	}
	return x;
}

