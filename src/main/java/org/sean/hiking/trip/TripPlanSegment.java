package org.sean.hiking.trip;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

public class TripPlanSegment {
	
	public static final Map<Integer,String> travelModesById = ImmutableMap.<Integer,String>builder()
			.put(1, "ON_FOOT")
			.put(2, "BICYCLE")
			.put(3, "PADDLING")
			.build();
	
	private int tripPlan;
	private int day;
	private int sequence;
	
	@JsonProperty("routeId")
	private int route;
	
	private int direction;
	private int mode;
	
	public TripPlanSegment() { }
	
	public TripPlanSegment(int tripPlan, int day, int sequence, int route,
			int direction, int mode) {
		this.tripPlan = tripPlan;
		this.day = day;
		this.sequence = sequence;
		this.route = route;
		this.direction = direction;
		this.mode = mode;
	}

	public int getTripPlan() {
		return tripPlan;
	}

	public int getDay() {
		return day;
	}

	public int getSequence() {
		return sequence;
	}

	public int getRoute() {
		return route;
	}

	public int getDirection() {
		return direction;
	}

	public void setTripPlan(int tripPlan) {
		this.tripPlan = tripPlan;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void setRoute(int route) {
		this.route = route;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	
}
