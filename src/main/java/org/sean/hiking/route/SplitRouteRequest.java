package org.sean.hiking.route;

import org.sean.hiking.place.Place;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SplitRouteRequest {
	
	@JsonProperty("a")
	private Route newRouteA;
	
	@JsonProperty("b")
	private Route newRouteB;
	
	@JsonProperty("o")
	private Route oldRoute;
	
	@JsonProperty("p")
	private Place newPlace;
	
	public SplitRouteRequest() { }
	
	public SplitRouteRequest(Route newRouteA, Route newRouteB, Route oldRoute,
			Place newPlace) {
		this.newRouteA = newRouteA;
		this.newRouteB = newRouteB;
		this.oldRoute = oldRoute;
		this.newPlace = newPlace;
	}

	public Route getNewRouteA() {
		return newRouteA;
	}

	public Route getNewRouteB() {
		return newRouteB;
	}

	public Route getOldRoute() {
		return oldRoute;
	}

	public Place getNewPlace() {
		return newPlace;
	}

	public void setNewRouteA(Route newRouteA) {
		this.newRouteA = newRouteA;
	}

	public void setNewRouteB(Route newRouteB) {
		this.newRouteB = newRouteB;
	}

	public void setOldRoute(Route oldRoute) {
		this.oldRoute = oldRoute;
	}

	public void setNewPlace(Place newPlace) {
		this.newPlace = newPlace;
	}
	
	

}
