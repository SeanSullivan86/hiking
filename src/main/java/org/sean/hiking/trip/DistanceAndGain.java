package org.sean.hiking.trip;

public class DistanceAndGain {
	private int distance;
	private int elevationGain;
	
	public DistanceAndGain(int distance, int elevationGain) {
		this.distance = distance;
		this.elevationGain = elevationGain;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getElevationGain() {
		return elevationGain;
	}

	public void setElevationGain(int elevationGain) {
		this.elevationGain = elevationGain;
	}
	
	
}
