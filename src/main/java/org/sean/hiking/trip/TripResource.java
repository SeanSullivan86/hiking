package org.sean.hiking.trip;

import java.util.List;
import java.util.Set;

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

import org.joda.time.LocalDate;
import org.sean.hiking.APIUtils;
import org.sean.hiking.WrappedResponse;
import org.sean.hiking.mapdata.MapDataResponse;
import org.sean.hiking.place.Place;
import org.sean.hiking.place.PlaceManager;
import org.sean.hiking.route.Route;
import org.sean.hiking.route.RouteManager;
import org.sean.hiking.user.User;
import org.sean.hiking.user.UserManager;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

@Path("/api/trips")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TripResource {
    private final RouteManager routeManager;
    private final PlaceManager placeManager;
    private final TripManager tripManager;
    private final UserManager userManager;

    public TripResource(RouteManager routeManager, PlaceManager placeManager, TripManager tripManager, UserManager userManager) {
    	this.routeManager = routeManager;
    	this.placeManager = placeManager;
    	this.tripManager = tripManager;
    	this.userManager = userManager;
    }
    
    @POST
    @Path("/plans")
    public WrappedResponse<TripPlan> insert(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		TripPlan tripPlan) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to add a trip plan.");
    	
    	if (tripPlan == null) {
    		return WrappedResponse.failure("Trip Plan was null");
    	}
    	
    	tripPlan.setCreatedBy(authUser.get().getId());
    	
    	return tripManager.insert(tripPlan);
    }
    
    @POST
    @Path("/trips")
    public WrappedResponse<Trip> insertTrip(
    		@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
    		Trip trip) {
    	Optional<User> authUser = this.userManager.getUserFromAuthString(auth);
    	if (!authUser.isPresent()) return WrappedResponse.failure("You must be logged in to add a trip.");
    	
    	if (trip == null) {
    		return WrappedResponse.failure("Trip was null");
    	}
    	
    	trip.setCreatedBy(authUser.get().getId());
    	
    	// Create new plan first, if necessary
    	if (trip.getPlanId() == 0) {
    		if (trip.getPlan() == null) {
    			return WrappedResponse.failure("No trip plan specified for the new trip");
    		}
    		trip.getPlan().setCreatedBy(authUser.get().getId());
    		
    		WrappedResponse<TripPlan> newPlan = tripManager.insert(trip.getPlan());
    		
    		if (!newPlan.isSuccess()) {
    			return WrappedResponse.failure("Unable to create new trip plan : " + newPlan.getMessage());
    		}
    		
    		trip.setPlan(newPlan.getResponse());
    		trip.setPlanId(newPlan.getResponse().getId());
    	}
    	
    	return tripManager.insertTrip(trip);
    }
        
    @GET
    @Path("/plans/{id}")
    public WrappedResponse<MapDataResponse> getPlanById(@PathParam("id") int id) {
    	Optional<TripPlan> plan = tripManager.getTripPlanById(id);
    	
    	if (!plan.isPresent()) {
    		return WrappedResponse.failure("Requested Trip Plan does not exist");
    	}
    	
    	Set<Integer> routeIds = Sets.newHashSet();
    	Set<Integer> placeIds = Sets.newHashSet();
    	
    	for (TripPlanSegment segment : plan.get().getSegments()) {
    		routeIds.add(segment.getRoute());
    	}
    	
    	List<Route> routes = routeManager.getRoutesByIds(Lists.newArrayList(routeIds));
    	
    	for (Route route : routes) {
    		placeIds.add(route.getStart());
    		placeIds.add(route.getEnd());
    	}
    	
    	List<Place> places = placeManager.getPlacesByIds(Lists.newArrayList(placeIds));
    	
    	return WrappedResponse.success(
    			new MapDataResponse(places, routes, Lists.newArrayList(plan.get()), null)
    			);
    }
    
    @GET
    @Path("/trips/{id}")
    public WrappedResponse<MapDataResponse> getTripById(@PathParam("id") int id) {
    	Optional<Trip> trip = tripManager.getTripById(id);
    	
    	if (!trip.isPresent()) {
    		return WrappedResponse.failure("Requested Trip does not exist");
    	}
    	
    	Set<Integer> routeIds = Sets.newHashSet();
    	Set<Integer> placeIds = Sets.newHashSet();
    	
    	for (TripPlanSegment segment : trip.get().getPlan().getSegments()) {
    		routeIds.add(segment.getRoute());
    	}
    	
    	List<Route> routes = routeManager.getRoutesByIds(Lists.newArrayList(routeIds));
    	
    	for (Route route : routes) {
    		placeIds.add(route.getStart());
    		placeIds.add(route.getEnd());
    	}
    	
    	List<Place> places = placeManager.getPlacesByIds(Lists.newArrayList(placeIds));
    	
    	return WrappedResponse.success(
    			new MapDataResponse(places, routes, null, Lists.newArrayList(trip.get()))
    			);
    }
    
    @GET
    @Path("/plans/equalityString/{eq}")
    public WrappedResponse<TripPlan> getPlanByEqualityString(@PathParam("eq") String equalityString) {
    	Optional<TripPlan> plan = this.tripManager.getTripPlanByEqualityString(equalityString);
    	
    	if (plan.isPresent()) {
    		return WrappedResponse.success(plan.get());
    	}
    	return WrappedResponse.success(null);
    }
    
    @GET
    @Path("/trips")
    public WrappedResponse<TripsAndPlans> getTrips(@QueryParam("minL") Optional<Integer> minDistance,
    		@QueryParam("maxL") Optional<Integer> maxDistance,
    		@QueryParam("minG") Optional<Integer> minGain,
    		@QueryParam("maxG") Optional<Integer> maxGain,
    		@QueryParam("minD") Optional<Integer> minDays,
    		@QueryParam("maxD") Optional<Integer> maxDays,
    		@QueryParam("s") Optional<Integer> startingPlace,
    		@QueryParam("e") Optional<Integer> endingPlace,
    		@QueryParam("c") Optional<String> containsPlaces,
    		@QueryParam("cr") Optional<Integer> tripCreatedBy,
    		@QueryParam("minTd") Optional<String> minTripDate,
    		@QueryParam("maxTd") Optional<String> maxTripDate,
    		@QueryParam("u") Optional<String> tripMembers) {
    	
    	Optional<List<Integer>> placesVisited = Optional.absent();
    	
    	if (containsPlaces.isPresent()) {
    		List<Integer> placeList = Lists.newArrayList();
    		for (String place : containsPlaces.get().split(",",-1)) {
    			placeList.add(Integer.parseInt(place));
    		}
    		placesVisited = Optional.of(placeList);
    	}
    	
    	Optional<List<Integer>> tripUsers = Optional.absent();
    	if (tripMembers.isPresent()) {
    		List<Integer> userList = Lists.newArrayList();
    		for (String user : tripMembers.get().split(",",-1)) {
    			userList.add(Integer.parseInt(user));
    		}
    		tripUsers = Optional.of(userList);
    	}
    	    	
    	TripPlanSearchCriteria planSearchCriteria = new TripPlanSearchCriteria(
    			Range.<Integer>all(),
    			Range.<Integer>all(),
    			APIUtils.getRangeFromOptionals(minDays, maxDays),
    			startingPlace,
    			endingPlace,
    			placesVisited);
    	
    	TripSearchCriteria tripSearchCriteria = new TripSearchCriteria(
    			APIUtils.getRangeFromOptionals(
    					Optional.fromNullable(minTripDate.isPresent() ? new LocalDate(minTripDate.get()) : null),
    					Optional.fromNullable(maxTripDate.isPresent() ? new LocalDate(maxTripDate.get()) : null)),
    			APIUtils.getRangeFromOptionals(minDistance, maxDistance), 
    			APIUtils.getRangeFromOptionals(minGain, maxGain), 
    			tripCreatedBy,
    			Optional.of(planSearchCriteria),
    			tripUsers);
    	
    	return WrappedResponse.success(
    			TripsAndPlans.fromTrips(this.tripManager.getTrips(tripSearchCriteria)
    					));
    }
    		
    
    @GET
    @Path("/plans")
    public WrappedResponse<List<TripPlan>> getPlans(@QueryParam("minL") Optional<Integer> minDistance,
    		@QueryParam("maxL") Optional<Integer> maxDistance,
    		@QueryParam("minG") Optional<Integer> minGain,
    		@QueryParam("maxG") Optional<Integer> maxGain,
    		@QueryParam("minD") Optional<Integer> minDays,
    		@QueryParam("maxD") Optional<Integer> maxDays,
    		@QueryParam("s") Optional<Integer> startingPlace,
    		@QueryParam("e") Optional<Integer> endingPlace,
    		@QueryParam("c") Optional<String> containsPlaces) {
    	
    	Optional<List<Integer>> placesVisited = Optional.absent();
    	
    	if (containsPlaces.isPresent()) {
    		List<Integer> placeList = Lists.newArrayList();
    		for (String place : containsPlaces.get().split(",",-1)) {
    			placeList.add(Integer.parseInt(place));
    		}
    		placesVisited = Optional.of(placeList);
    	}
    	    	
    	TripPlanSearchCriteria searchCriteria = new TripPlanSearchCriteria(
    			APIUtils.getRangeFromOptionals(minDistance, maxDistance),
    			APIUtils.getRangeFromOptionals(minGain, maxGain),
    			APIUtils.getRangeFromOptionals(minDays, maxDays),
    			startingPlace,
    			endingPlace,
    			placesVisited);
    	
    	return WrappedResponse.success(tripManager.getTripPlans(searchCriteria));
    }
    

}
