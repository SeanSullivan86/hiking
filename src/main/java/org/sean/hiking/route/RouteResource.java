package org.sean.hiking.route;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.place.Place;
import org.sean.hiking.place.PlaceManager;
import org.sean.hiking.user.User;
import org.sean.hiking.user.UserManager;

import com.google.common.base.Optional;


@Path("/api/routes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RouteResource {
    private final RouteManager routeManager;
    private final PlaceManager placeManager;
    private final UserManager userManager;

    public RouteResource(RouteManager routeManager, PlaceManager placeManager, UserManager userManager) {
    	this.routeManager = routeManager;
    	this.placeManager = placeManager;
    	this.userManager = userManager;
    }
    
    @POST
    public WrappedResponse<Route> insert(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		Route route) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	route.setCreatedBy(authUser.get().getId());
    	route.setLastUpdatedBy(authUser.get().getId());
    	
    	return routeManager.insertRoute(route);
    }
    
    @POST
    @Path("/{id}")
    public WrappedResponse<Route> update(
    		@PathParam("id") int id,
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		Route route) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	if (route == null || id != route.getId()) {
    		return WrappedResponse.failure("Route ID in URL must match id in Route object");
    	}
    	
    	Optional<Route> existingRoute = this.routeManager.getRouteById(id);
    	
    	if (!existingRoute.isPresent()) {
    		return WrappedResponse.failure("Route you are trying to edit does not exist.");
    	}
    	
    	route.setCreatedBy(existingRoute.get().getCreatedBy());
    	route.setLastUpdatedBy(authUser.get().getId());
    	
    	if (existingRoute.get().getIsPublic() == 0 && existingRoute.get().getCreatedBy() != authUser.get().getId()) {
    		return WrappedResponse.failure("Error : You cannot edit private routes that were created by a different user.");
    	}
    	
    	return routeManager.updateRoute(route);
    }
    
    @POST
    @Path("/delete")
    public WrappedResponse<String> delete(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		DeleteRouteRequest req) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	routeManager.deleteRoute(req.getRouteId());
    	
    	return WrappedResponse.success("Completed.");
    }
    
    @POST
    @Path("/split")
    public WrappedResponse<String> split(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		SplitRouteRequest req) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to edit the map.");
    	
    	if (req == null || req.getNewPlace() == null || req.getNewRouteA() == null || req.getNewRouteB() == null) {
    		return WrappedResponse.failure("Unable to split route. Must provide 2 new routes and new midpoint");
    	}
    	
    	Optional<Route> existingRoute = this.routeManager.getRouteById(req.getOldRoute().getId());
    	
    	if (!existingRoute.isPresent()) {
    		return WrappedResponse.failure("Route you are trying to split does not exist.");
    	}
    	
    	if (existingRoute.get().getIsPublic() == 0 && authUser.get().getId() != existingRoute.get().getCreatedBy()) {
    		return WrappedResponse.failure("Error : You cannot split private routes that were created by a different user.");
    	}
    	
    	req.getNewPlace().setCreatedBy(authUser.get().getId());
    	req.getNewRouteA().setCreatedBy(authUser.get().getId());
    	req.getNewRouteB().setCreatedBy(authUser.get().getId());
    	req.getNewPlace().setLastUpdatedBy(authUser.get().getId());
    	req.getNewRouteA().setLastUpdatedBy(authUser.get().getId());
    	req.getNewRouteB().setLastUpdatedBy(authUser.get().getId());
    	
    	return routeManager.splitRoute(req.getNewRouteA(), req.getNewRouteB(), req.getOldRoute(), req.getNewPlace());
    	
    }

    @GET
    @Path("/{id}")
    public WrappedResponse<Route> getById(@PathParam("id") int id) {
    	Optional<Route> route = routeManager.getRouteById(id);
    	
    	if (route.isPresent()) {
    		return WrappedResponse.success(route.get());
    	} else {
    		return WrappedResponse.failure("Requested Route does not exist.");
    	}
    }
    
    @GET
    @Path("/fixAllRouteTiles")
    public WrappedResponse<String> fixAllRouteTiles() {
    	routeManager.fixRouteTiles();
    	routeManager.resolveAllElevationConflicts();
    	return WrappedResponse.success("Completed.");
    }
}
