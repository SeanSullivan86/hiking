package org.sean.hiking.coordinates;

import org.joda.time.DateTime;

public class TrackLogEntry {
	private EarthPosition3D position;
	private DateTime time;
	
	public TrackLogEntry(EarthPosition3D position, DateTime time) {
		this.position = position;
		this.time = time;
	}

	public EarthPosition3D getPosition() {
		return position;
	}

	public DateTime getTime() {
		return time;
	}
	
	public double getElevation() {
		return position.getGroundElevation();
	}
}
