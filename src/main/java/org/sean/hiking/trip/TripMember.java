package org.sean.hiking.trip;

public class TripMember {
	private int trip;
	private int user;
	private String name;
	
	public TripMember(int trip, int user, String name) {
		this.trip = trip;
		this.user = user;
		this.name = name;
	}
	
	public int getTrip() {
		return trip;
	}
	public void setTrip(int trip) {
		this.trip = trip;
	}
	public int getUser() {
		return user;
	}
	public void setUser(int user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
