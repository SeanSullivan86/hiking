package org.sean.hiking.route;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.coordinates.EarthPosition2D;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class Route {
	
	public static Map<String, Set<String>> validRouteTypes = 
			ImmutableMap.<String, Set<String>>builder()
			.put("road", Sets.newHashSet("paved","easy2wd","hard2wd","4wd","closed"))
			.put("trail", Sets.newHashSet("wide","good","minor","poor"))
			.put("open", Sets.newHashSet("easy","moderate","hard","1","2","3","4","5"))
			.build();
	
	private int id;
	private int start;
	private int end;
	private int distance; // hundredths of miles
	private int elevationGain; // feet
	private int reverseGain; // feet
	private String type;
	private String subtype;
	private String name;
	private List<EarthPosition2D> path;
	private List<String> tiles;
	private int isPublic;
	private int createdBy;
	private long creationTime;
	private int lastUpdatedBy;
	private long lastUpdatedTime;
	
	public Route() { }
	
	public Route(int id, int start, int end, int distance, int elevationGain, int reverseGain,
			String type, String subtype, String name,
			List<EarthPosition2D> path, List<String> tiles,
			int isPublic, int createdBy,
			long creationTime, int lastUpdatedBy, long lastUpdatedTime) {
		this.id = id;
		this.start = start;
		this.end = end;
		this.distance = distance;
		this.elevationGain = elevationGain;
		this.reverseGain = reverseGain;
		this.type = type;
		this.subtype = subtype;
		this.name = name;
		this.path = path;
		this.tiles = tiles;
		this.isPublic = isPublic;
		this.createdBy = createdBy;
		this.creationTime = creationTime;
		this.lastUpdatedBy = lastUpdatedBy;
		this.lastUpdatedTime = lastUpdatedTime;
		
	}
	
	public WrappedResponse<String> validateFields() {
		if (id < 0 || id > 2000000000)
			return WrappedResponse.failure("Route Id must be a positive integer");
		if (start <= 0 || end <= 0)
			return WrappedResponse.failure("Route Endpoints must be positive integers");
		if (distance <= 0 || distance > 10000)
			return WrappedResponse.failure("Route Distance must be between 0 and 100 miles");
		if (elevationGain < 0 || elevationGain > 50000)
			return WrappedResponse.failure("Elevation Gain must be between 0 and 50000 feet");
		if (reverseGain < 0 || reverseGain > 50000)
			return WrappedResponse.failure("Reverse Elevation Gain must be between 0 and 50000 feet");
		if (!validRouteTypes.containsKey(type))
			return WrappedResponse.failure("Invalid Route Type");
		if (!validRouteTypes.get(type).contains(subtype))
			return WrappedResponse.failure("Invalid Route Subtype");
		if (name != null && name.length() > 90) {
			return WrappedResponse.failure("Route Name must be at most 90 characters");
		}
		if (path == null || path.size() < 2)
			return WrappedResponse.failure("Route Path must have at least 2 points");
		for (EarthPosition2D position : path) {
			if (!position.isValid()) {
				return WrappedResponse.failure("All entries in route path must be valid latitude, longitude pairs");
			}
		}
		if (tiles == null || tiles.isEmpty())
			return WrappedResponse.failure("Route must include at least 1 tile");
		if (isPublic != 0 && isPublic != 1)
			return WrappedResponse.failure("isPublic must be 0 or 1");
		return WrappedResponse.success("Valid");
	}

	public int getId() {
		return id;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getDistance() {
		return distance;
	}

	public int getElevationGain() {
		return elevationGain;
	}

	public int getReverseGain() {
		return reverseGain;
	}

	public List<EarthPosition2D> getPath() {
		return path;
	}

	public List<String> getTiles() {
		return tiles;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setElevationGain(int elevationGain) {
		this.elevationGain = elevationGain;
	}

	public void setReverseGain(int reverseGain) {
		this.reverseGain = reverseGain;
	}

	public void setPath(List<EarthPosition2D> path) {
		this.path = path;
	}

	public void setTiles(List<String> tiles) {
		this.tiles = tiles;
	}

	public String getType() {
		return type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public boolean isPathEqualTo(List<EarthPosition2D> otherPath) {
		if (path.size() != otherPath.size()) return false;
		for (int i = 0; i < path.size(); i++) {
			if ((Math.abs(path.get(i).getLatitude() - otherPath.get(i).getLatitude()) > 0.000001) ||
					(Math.abs(path.get(i).getLongitude() - otherPath.get(i).getLongitude()) > 0.000001)) {
				return false;
			}
		}
		return true;
	}
	
}