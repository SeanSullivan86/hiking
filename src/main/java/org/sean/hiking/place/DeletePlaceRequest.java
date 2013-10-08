package org.sean.hiking.place;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeletePlaceRequest {

	@JsonProperty("x")
	private int placeId;
	
	private DeletePlaceRequest() { }

	public DeletePlaceRequest(int placeId) {
		this.placeId = placeId;
	}

	public int getPlaceId() {
		return placeId;
	}

	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	
}
