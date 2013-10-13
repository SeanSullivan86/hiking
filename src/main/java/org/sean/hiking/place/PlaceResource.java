package org.sean.hiking.place;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.mapdata.MapDataResponse;
import org.sean.hiking.route.Route;
import org.sean.hiking.route.RouteManager;
import org.sean.hiking.user.User;
import org.sean.hiking.user.UserManager;

import java.util.Arrays;
import java.util.List;

@Path("/api/places")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlaceResource {
    private final PlaceManager placeManager;
    private final RouteManager routeManager;
    private final UserManager userManager;
    
    public PlaceResource(PlaceManager placeManager, RouteManager routeManager, UserManager userManager) {
        this.placeManager = placeManager;
        this.routeManager = routeManager;
        this.userManager = userManager;
    }
    
    @POST
    @Path("/delete")
    public WrappedResponse<String> delete(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		DeletePlaceRequest req) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	Optional<Place> place = placeManager.getPlaceById(req.getPlaceId());
    	
    	if (!place.isPresent()) {
    		return WrappedResponse.failure("Error : The place you tried to delete no longer exists.");
    	}
    	
    	if (place.get().getIsPublic() == 0 && place.get().getCreatedBy() != authUser.get().getId()) {
    		return WrappedResponse.failure("Error : You cannot delete private places that were created by a different user.");
    	}
        	
    	placeManager.deletePlace(req.getPlaceId());
    	
    	return WrappedResponse.success("Completed.");
    }
    
    @POST
    public WrappedResponse<MapDataResponse> insert(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		Place place) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	place.setCreatedBy(authUser.get().getId());
    	place.setLastUpdatedBy(authUser.get().getId());
    	
    	WrappedResponse<Place> newPlace = placeManager.insertPlace(place);
    	
    	if (!newPlace.isSuccess()) {
    		return WrappedResponse.failure(newPlace.getMessage());
    	}
    	
    	return WrappedResponse.success(
    			new MapDataResponse(
    			Lists.newArrayList(newPlace.getResponse()),
    			Lists.<Route>newArrayList(),
    			null, null));
    }
    
    @POST
    @Path("/{id}")
    public WrappedResponse<MapDataResponse> updatePlace(
    		@PathParam("id") int id,
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		Place place) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	if (place == null || place.getId() != id) return WrappedResponse.failure("Place Id in URL must match Place Id in body");
    	
    	Optional<Place> existingPlace = this.placeManager.getPlaceById(id);
    	
    	if (!existingPlace.isPresent()) {
    		return WrappedResponse.failure("Place you are trying to edit does not exist.");
    	}
    	
    	place.setCreatedBy(existingPlace.get().getCreatedBy());
    	place.setLastUpdatedBy(authUser.get().getId());
    	
    	if (existingPlace.get().getIsPublic() == 0 && existingPlace.get().getCreatedBy() != authUser.get().getId()) {
    		return WrappedResponse.failure("Error : You cannot edit private places that were created by a different user.");
    	}
    	
    	WrappedResponse<Place> updatedPlace = placeManager.updatePlace(place);
    	
    	if (!updatedPlace.isSuccess()) {
    		return WrappedResponse.failure(updatedPlace.getMessage());
    	}
    	
    	return WrappedResponse.success(
    			new MapDataResponse(
    			Lists.newArrayList(updatedPlace.getResponse()),
    			routeManager.getRoutesForEndpoint(id),
    			null, null));
    }

    /** Optionally give a comma separated list of tiles to restrict the search to */
    @GET
    @Timed
    public WrappedResponse<List<Place>> getAll(@QueryParam("tiles") Optional<String> tiles) {
    	if (tiles.isPresent()) {
    		return WrappedResponse.success(placeManager.getPlacesForTiles(
    				Arrays.asList(tiles.get().split(",",-1))));
    	} else {
            return WrappedResponse.failure("Must specifiy tiles to fetch fetch places");
    	}
    }
    
    @GET
    @Path("/{id}")
    public WrappedResponse<Place> getById(@PathParam("id") int id) {
    	Optional<Place> place = placeManager.getPlaceById(id);
    	
    	if (place.isPresent()) {
    		return WrappedResponse.success(place.get());
    	} else {
    		return WrappedResponse.failure("Requested place does not exist.");
    	}
    }
}
