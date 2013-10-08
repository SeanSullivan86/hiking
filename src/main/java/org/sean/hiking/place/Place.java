package org.sean.hiking.place;

import java.util.Set;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.coordinates.EarthPosition2D;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;

public class Place {
	public static Set<String> validPlaceTypes = Sets.newHashSet("summit","saddle","ridge","junction","trailhead","water");
	
	private int id;
	private String name;
	private double latitude;
	private double longitude;
	private int elevation;
	private String type;
	private String tile;
	private int isPublic;
	private int createdBy;
	private long creationTime;
	private int lastUpdatedBy;
	private long lastUpdatedTime;
	
	
	public Place() { }
		
	public Place(int id, String name, double latitude, double longitude,
			int elevation, String type, String tile, int isPublic, int createdBy,
			long creationTime, int lastUpdatedBy, long lastUpdatedTime) {
		super();
		this.id = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
		this.type = type;
		this.tile = tile;
		this.isPublic = isPublic;
		this.createdBy = createdBy;
		this.creationTime = creationTime;
		this.lastUpdatedBy = lastUpdatedBy;
		this.lastUpdatedTime = lastUpdatedTime;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getElevation() {
		return elevation;
	}

	public String getType() {
		return type;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTile() {
		return tile;
	}

	public void setTile(String tile) {
		this.tile = tile;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public int getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(int isPublic) {
		this.isPublic = isPublic;
	}
	
	
	
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public int getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(int lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	@JsonIgnore
	public EarthPosition2D getLocation() {
		return new EarthPosition2D(this.latitude, this.longitude);
	}
		
	
	
	public WrappedResponse<String> validateFields() {
		if (id < 0 || id > 2000000000) 
			return WrappedResponse.failure("Id must be positive integer.");
		if (name == null || name.trim().isEmpty())
			return WrappedResponse.failure("Place Name must not be blank.");
		if (name.length() > 90)
			return WrappedResponse.failure("Place Name must be at most 90 characters");
		if (latitude > 90.0 || latitude < -90.0)
			return WrappedResponse.failure("Latitude must be between -90 and 90");
		if (longitude > 180.0 || longitude < -180.0)
			return WrappedResponse.failure("Longitude must be between -180 and 180");
		if (elevation < -1500 || elevation > 30000)
			return WrappedResponse.failure("Elevation must be between -1500 and 30000 feet.");
		if (!validPlaceTypes.contains(type))
			return WrappedResponse.failure("Invalid Place Type");
		if (tile == null || !tile.equals((new EarthPosition2D(latitude,longitude)).getTile()))
			return WrappedResponse.failure("Incorrect tile for place latitude,longitude");
		if (isPublic != 0 && isPublic != 1)
			return WrappedResponse.failure("isPublic must be 0 or 1");
		return WrappedResponse.success("Valid");
	}
}