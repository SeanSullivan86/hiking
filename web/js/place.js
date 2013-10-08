function Place(id, name, latitude, longitude, elevation, type, tile, createdBy, isPublic) {
  this.id = id;
  this.name = name;
  this.latitude = latitude;
  this.longitude = longitude;
  this.elevation = elevation;
  this.type = type;
  this.tile = tile;
  this.createdBy = createdBy;
  this.isPublic = isPublic;
  
  this.getLatLng = __getLatLng;
}

function createPlace(json) {
	return new Place(json.id, json.name, json.latitude, json.longitude, json.elevation, json.type, json.tile, json.createdBy, json.isPublic); 
}

function __getLatLng() {
	return new google.maps.LatLng(this.latitude, this.longitude);
}

