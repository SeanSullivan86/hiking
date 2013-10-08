package org.sean.hiking.trip;

import java.util.List;

import org.sean.hiking.place.Place;
import org.sean.hiking.route.Route;

public class OriginalPlan {
	private List<Place> places;
	private List<Route> routes;
	private TripPlan plan;
	
	public OriginalPlan() { }
	
	public OriginalPlan(List<Place> places, List<Route> routes, TripPlan plan) {
		this.places = places;
		this.routes = routes;
		this.plan = plan;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public List<Route> getRoutes() {
		return routes;
	}

	public TripPlan getPlan() {
		return plan;
	}

	public void setPlaces(List<Place> places) {
		this.places = places;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public void setPlan(TripPlan plan) {
		this.plan = plan;
	}
	
	
	
}
