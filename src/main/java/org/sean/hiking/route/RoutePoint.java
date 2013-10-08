package org.sean.hiking.route;

import org.sean.hiking.coordinates.EarthPosition2D;

public class RoutePoint {
	private int route;
	private int sequence;
	private double latitude;
	private double longitude;
	
	public RoutePoint(int route, int sequence, double latitude, double longitude) {
		this.route = route;
		this.sequence = sequence;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getRoute() {
		return route;
	}

	public int getSequence() {
		return sequence;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public EarthPosition2D getPosition() {
		return new EarthPosition2D(latitude, longitude);
	}

	public void setRoute(int route) {
		this.route = route;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	
}
