package org.sean.hiking.mapdata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.place.Place;
import org.sean.hiking.place.PlaceManager;
import org.sean.hiking.route.Route;
import org.sean.hiking.route.RouteManager;



@Path("/api/mapdata")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MapDataResource {
    private final PlaceManager placeManager;
    private final RouteManager routeManager;

    public MapDataResource(PlaceManager placeManager, RouteManager routeManager) {
    	this.placeManager = placeManager;
    	this.routeManager = routeManager;
    }
    
    @GET
    @Path("/{tiles}")
    public WrappedResponse<MapDataResponse> getTileData(@PathParam("tiles") String tileStr) {
    	List<String> tiles = Arrays.asList(tileStr.split(",",-1));
    	
    	List<Route> routes = routeManager.getRoutesForTiles(tiles);
    	List<Place> places = placeManager.getPlacesForTiles(tiles);
    	
    	Set<Integer> placeIds = new HashSet<Integer>();
    	Set<Integer> missingPlaceIds = new HashSet<Integer>();
    	
    	for (Place place : places) {
    		placeIds.add(place.getId());
    	}
    	
    	/* make sure we do a separate query to get the places where these
    	 * routes go out of the specified tile list */
    	for (Route route : routes) {
    		if (!placeIds.contains(route.getStart())) {
    			missingPlaceIds.add(route.getStart());
    		}
    		if (!placeIds.contains(route.getEnd())) {
    			missingPlaceIds.add(route.getEnd());
    		}
    	}
    	
    	// TODO make into batch query
    	for (int placeId : missingPlaceIds) {
    		places.add(placeManager.getPlaceById(placeId).get());
    	}
    	
    	return WrappedResponse.success(new MapDataResponse(places, routes, null, null));
    }
}