package org.sean.hiking.route;



public class RouteTilePair {
	private int route;
	private String tile;
	
	public RouteTilePair() { }
	
	public RouteTilePair(int route, String tile) {
		this.route = route;
		this.tile = tile;
	}

	public int getRoute() {
		return route;
	}

	public String getTile() {
		return tile;
	}

	public void setRoute(int route) {
		this.route = route;
	}

	public void setTile(String tile) {
		this.tile = tile;
	}
	
	

}
