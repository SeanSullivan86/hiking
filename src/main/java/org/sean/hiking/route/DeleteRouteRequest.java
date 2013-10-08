package org.sean.hiking.route;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteRouteRequest {

	@JsonProperty("x")
	private int routeId;
	
	public DeleteRouteRequest() { }

	public DeleteRouteRequest(int routeId) {
		this.routeId = routeId;
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	
	
}
