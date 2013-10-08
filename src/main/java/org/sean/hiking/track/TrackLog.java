package org.sean.hiking.track;

import java.util.List;

import org.sean.hiking.coordinates.TrackLogEntry;

public class TrackLog {

	private List<TrackLogEntry> points;
	private String name;
	
	public TrackLog(String name, List<TrackLogEntry> points) {
		this.name = name;
		this.points = points;
	}

	public List<TrackLogEntry> getPoints() {
		return points;
	}

	public String getName() {
		return name;
	}
	
	
}
