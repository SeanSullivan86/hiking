package org.sean.hiking.trip;

import java.util.List;

import org.sean.hiking.WrappedResponse;

public class TripPlan {
	private int id;
	private String name;
	private int days;
	private int distance;
	private int elevationGain;
	private int createdBy;
	private OriginalPlan originalPlan;
	private int originalDistance;
	private int originalGain;
	private int startingPoint;
	private int endingPoint;
	private int isMappable;
	private long creationDate;
	
	
	private List<String> tiles;
	
	private List<TripPlanSegment> segments;
	
	public TripPlan() { }

	public TripPlan(int id, String name, int days, int distance, int elevationGain,
			int createdBy, OriginalPlan originalPlan, int originalDistance, 
			int originalGain, List<String> tiles, List<TripPlanSegment> segments,
			int startingPoint, int endingPoint, int isMappable, long creationDate) {
		this.id = id;
		this.days = days;
		this.distance = distance;
		this.elevationGain = elevationGain;
		this.createdBy = createdBy;
		this.originalPlan = originalPlan;
		this.originalDistance = originalDistance;
		this.originalGain = originalGain;
		this.tiles = tiles;
		this.segments = segments;
		this.startingPoint = startingPoint;
		this.endingPoint = endingPoint;
		this.name = name;
		this.isMappable = isMappable;
		this.creationDate = creationDate;
	}
	
	public WrappedResponse<String> validateFields() {
		if (name == null || name.trim().isEmpty()) {
			return WrappedResponse.failure("Missing name for new trip plan.");
		}
		if (segments == null || segments.isEmpty()) {
			return WrappedResponse.failure("Missing route segments for new trip plan.");
		}
		
		if (segments.get(0).getDay() != 1 || segments.get(0).getSequence() != 1) {
			return WrappedResponse.failure("First segment of trip plan must be marked with day 1, sequence 1");
		}
		
		int prevDay = 1;
		int prevSeq = 0;
		for (TripPlanSegment segment : segments) {
			if (segment == null) {
				return WrappedResponse.failure("Trip Plan Segment List contains null segment");
			}
			if (!TripPlanSegment.travelModesById.containsKey(segment.getMode())) {
				return WrappedResponse.failure("Trip Plan Segment has invalid travel mode");
			}
			if (segment.getDay() < prevDay) {
				return WrappedResponse.failure("Trip Plan Segments must be in order");
			} else if (segment.getDay() == prevDay) {
				if (segment.getSequence() != (prevSeq + 1)) {
					return WrappedResponse.failure("Trip Plan Segments must be in order");
				}
			} else {
				if (segment.getSequence() != 1) {
					return WrappedResponse.failure("Trip Plan Segments must be in order");
				}
			}
			prevDay = segment.getDay();
			prevSeq = segment.getSequence();
		}
		
		if (days != prevDay) {
			return WrappedResponse.failure("Trip Plan Day Count must match day of last trip plan segment");
		}
				
		return WrappedResponse.success("Valid");
	}

	public int getId() {
		return id;
	}

	public int getDays() {
		return days;
	}

	public int getDistance() {
		return distance;
	}

	public int getElevationGain() {
		return elevationGain;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public OriginalPlan getOriginalPlan() {
		return originalPlan;
	}

	public List<TripPlanSegment> getSegments() {
		return segments;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setElevationGain(int elevationGain) {
		this.elevationGain = elevationGain;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public void setOriginalPlan(OriginalPlan originalPlan) {
		this.originalPlan = originalPlan;
	}

	public void setSegments(List<TripPlanSegment> segments) {
		this.segments = segments;
	}

	public int getOriginalDistance() {
		return originalDistance;
	}

	public int getOriginalGain() {
		return originalGain;
	}

	public void setOriginalDistance(int originalDistance) {
		this.originalDistance = originalDistance;
	}

	public void setOriginalGain(int originalGain) {
		this.originalGain = originalGain;
	}

	public List<String> getTiles() {
		return tiles;
	}

	public void setTiles(List<String> tiles) {
		this.tiles = tiles;
	}

	public int getStartingPoint() {
		return startingPoint;
	}

	public void setStartingPoint(int startingPoint) {
		this.startingPoint = startingPoint;
	}

	public int getEndingPoint() {
		return endingPoint;
	}

	public void setEndingPoint(int endingPoint) {
		this.endingPoint = endingPoint;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsMappable() {
		return isMappable;
	}

	public void setIsMappable(int isMappable) {
		this.isMappable = isMappable;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}
	
	
	
}
