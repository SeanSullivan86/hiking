package org.sean.hiking.coordinates;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EarthPosition2D {
	private double latitude;
	private double longitude;
	
	public EarthPosition2D() { }
	
	public EarthPosition2D(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@JsonIgnore
	public String getTile() {
		int x = (int) Math.floor(10*(longitude+200));
		int y = (int) Math.floor(10*(latitude+100));
		
		return x+"_"+y;
	}
	
	@JsonIgnore
	public boolean isApproximatelyEqualTo(double latitude, double longitude) {
		return Math.abs(latitude - this.latitude) < 0.000001 && Math.abs(longitude - this.longitude) < 0.000001;
	}
	
	@JsonIgnore
	public boolean isApproximatelyEqualTo(EarthPosition2D x) {
		return Math.abs(x.latitude - this.latitude) < 0.000001 && Math.abs(x.longitude - this.longitude) < 0.000001;
	}
	
	@JsonIgnore
	public boolean isValid() {
		return this.latitude >= -90.0 && this.latitude <= 90.0 && this.longitude >= -180.0 && this.longitude <= 180.0;
	}
	
}
