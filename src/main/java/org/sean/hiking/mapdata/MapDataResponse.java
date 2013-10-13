package org.sean.hiking.mapdata;

import java.util.List;

import org.sean.hiking.place.Place;
import org.sean.hiking.route.Route;
import org.sean.hiking.trip.Trip;
import org.sean.hiking.trip.TripPlan;

public class MapDataResponse {

	private List<Place> places;
	private List<Route> routes;
	private List<TripPlan> plans;
	private List<Trip> trips;
	
	public MapDataResponse(List<Place> places, List<Route> routes, List<TripPlan> plans, List<Trip> trips) {
		this.places = places;
		this.routes = routes;
		this.plans = plans;
		this.trips = trips;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public List<Route> getRoutes() {
		return routes;
	}
	
	public List<TripPlan> getPlans() {
		return plans;
	}

	public List<Trip> getTrips() {
		return trips;
	}
	
	
}
