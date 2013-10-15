package org.sean.hiking.trip;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sean.hiking.APIUtils;
import org.sean.hiking.HelloWorldService;
import org.sean.hiking.WrappedResponse;
import org.sean.hiking.place.Place;
import org.sean.hiking.place.PlaceManager;
import org.sean.hiking.place.PlaceMapper;
import org.sean.hiking.route.Route;
import org.sean.hiking.route.RouteManager;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TripManager {
	private TripDao tripDao;
	private ObjectMapper mapper;
	private RouteManager routeManager;
	private PlaceManager placeManager;
	
	public TripManager(TripDao tripDao, RouteManager routeManager, PlaceManager placeManager) {
		this.tripDao = tripDao;
		this.routeManager = routeManager;
		this.placeManager = placeManager;
		mapper = new ObjectMapper();
	}
	
	public Optional<TripPlan> getTripPlanById(int id) {
		TripPlan plan = tripDao.getPlanById(id);
		
		if (plan == null) {
			return Optional.absent();
		}
		populateSegmentsAndTilesForPlan(plan);
		return Optional.of(plan);
	}
	
	private void populateSegmentsAndTilesForPlan(TripPlan plan) {
		plan.setSegments(tripDao.getSegmentsForPlan(plan.getId()));
		try {
			plan.setOriginalPlan(mapper.readValue(tripDao.getOriginalPlanById(plan.getId()), OriginalPlan.class));
		} catch (Exception e) {
			throw new RuntimeException("Unable to deserialize original trip plan for trip " + plan.getId(), e);
		}
		
		Set<Integer> routes = Sets.newHashSet();
		for (TripPlanSegment segment : plan.getSegments()) {
			routes.add(segment.getRoute());
		}
		
		plan.setTiles(routeManager.getTilesForRouteSet(routes));
	}
	
	
	/** Argument is a 'plan' which doesn't yet have an originalPlan created for it.
	 */
	private OriginalPlan constructOriginalPlan(TripPlan plan) {
		Map<Integer, Route> routes = Maps.newHashMap();
		Map<Integer, Place> places = Maps.newHashMap();
		for (TripPlanSegment segment : plan.getSegments()) {
			if (!routes.containsKey(segment.getRoute())) {
				Route route = routeManager.getRouteById(segment.getRoute()).get();
				routes.put(segment.getRoute(), route);
				if (!places.containsKey(route.getStart())) {
					places.put(route.getStart(), placeManager.getPlaceById(route.getStart()).get());
				}
				if (!places.containsKey(route.getEnd())) {
					places.put(route.getEnd(), placeManager.getPlaceById(route.getEnd()).get());
				}
			}
		}
		
		return new OriginalPlan(
				Lists.newArrayList(places.values()),
				Lists.newArrayList(routes.values()),
				plan);
	}
	
	
	/** Looks at the tripPlanSegments in the argument, then queries the DB to retrieve those routes and sum their distances */
	private DistanceAndGain calculatePlanDistanceAndGain(TripPlan tripPlan) {
		Map<Integer, Route> routes = Maps.newHashMap();
		Set<Integer> routeIds = Sets.newHashSet();
		for (TripPlanSegment segment : tripPlan.getSegments()) {
			routeIds.add(segment.getRoute());
		}
		
		for (Route route : routeManager.getRoutesByIds(Lists.newArrayList(routeIds))) {
			routes.put(route.getId(), route);
		}
		
		int distance = 0;
		int elevationGain = 0;
		
		for (TripPlanSegment segment : tripPlan.getSegments()) {
			Route route = routes.get(segment.getRoute());
			distance += route.getDistance();
			elevationGain += segment.getDirection() == 1 ? route.getElevationGain() : route.getReverseGain();
		}
		
		return new DistanceAndGain(distance, elevationGain);
	}
	
	/** Updates the db with corrected distance and elevation gain for a trip plan
	 *  (if the distances/gains of the underlying routes changed) */
	private void recomputeDistanceAndGain(int tripPlanId) {
		Optional<TripPlan> plan = getTripPlanById(tripPlanId);
		
		if (plan.isPresent()) {
			DistanceAndGain stats = calculatePlanDistanceAndGain(plan.get());
			tripDao.updateDistanceAndGain(tripPlanId, stats.getDistance(), stats.getElevationGain());
		}
	}
	
	public void recomputeDistancesForPlansUsingRoute(int routeId) {
		for (TripPlan plan : this.tripDao.getPlansByRoute(routeId)) {
			if (plan.getIsMappable() == 1) {
			    recomputeDistanceAndGain(plan.getId());
			}
		}
	}
	
	private void setPlanAsUnmappable(int tripPlanId) {
		tripDao.setPlanAsUnmappable(tripPlanId);
	}
	
	
	public void updatePlansToHandleDeletedRoute(int routeId) {
		for (TripPlan plan : this.tripDao.getPlansByRoute(routeId)) {
			setPlanAsUnmappable(plan.getId());
		}
	}
	
	public void replaceRouteWithSplitRoutes(Route oldRoute, Route newRouteA, Route newRouteB) {
		for (TripPlan plan : this.tripDao.getPlansByRoute(oldRoute.getId())) {
			if (plan.getIsMappable() == 0) {
				continue;
			}
			
			List<TripPlanSegment> newSegments = Lists.newArrayList();
			
			for (TripPlanSegment segment : plan.getSegments()) {
				int nextSequenceId = 1;
				if (segment.getSequence() != 1) {
					nextSequenceId = newSegments.get(newSegments.size()-1).getSequence() + 1;
				}
				
				if (segment.getRoute() != oldRoute.getId()) {
					newSegments.add(new TripPlanSegment(plan.getId(), segment.getDay(), nextSequenceId,
							segment.getRoute(), segment.getDirection(), segment.getMode()));
				} else {
					newSegments.add(new TripPlanSegment(plan.getId(), segment.getDay(), nextSequenceId,
							segment.getDirection() == 1 ? newRouteA.getId() : newRouteB.getId(),
							segment.getDirection(), segment.getMode()));
					newSegments.add(new TripPlanSegment(plan.getId(), segment.getDay(), nextSequenceId+1,
							segment.getDirection() == 1 ? newRouteB.getId() : newRouteA.getId(),
							segment.getDirection(), segment.getMode()));
				}
			}
			
			updateSegmentsForExistingPlan(plan.getId(), newSegments);
			recomputeDistanceAndGain(plan.getId());
		}
	}
	
	private void updateSegmentsForExistingPlan(int planId, List<TripPlanSegment> segments) {
		tripDao.removeAllSegmentsForTripPlan(planId);
		
		for (TripPlanSegment segment : segments) {
			tripDao.insertTripPlanSegment(planId,
					segment.getDay(), 
					segment.getSequence(), 
					segment.getRoute(), 
					segment.getDirection(),
					segment.getMode());
		}
	}
	
	/** startingPlace,route,mode,route,mode,x,route,mode . ("x" indicates a camp). Does NOT query DB */
	private String constructEqualityString(TripPlan tripPlan) {
		StringBuilder builder = new StringBuilder(""+tripPlan.getStartingPoint());
		int day = 1;
		
		for (TripPlanSegment segment : tripPlan.getSegments()) {
			while (day < segment.getDay()) {
				builder.append(",x");
				day++;
			}
			builder.append(","+segment.getRoute()+","+segment.getMode());
		}
		
		String str = builder.toString();
		if (str.length() < 400) {
			return str;
		} else {
			return APIUtils.sha1(str);
		}
	}
	
	
	public WrappedResponse<TripPlan> insert(TripPlan tripPlan) {
		if (tripPlan == null) {
			return WrappedResponse.failure("Cannot insert null trip plan");
		}
		
		// Validate basic sanity of fields (no DB checks)
		WrappedResponse<String> fieldValidation = tripPlan.validateFields();
		if (! fieldValidation.isSuccess()) {
			return WrappedResponse.failure("Cannot insert trip plan (" + fieldValidation.getMessage() + ")");
		}
		
		Set<Integer> routeIds = Sets.newHashSet();
		for (TripPlanSegment segment : tripPlan.getSegments()) {
			routeIds.add(segment.getRoute());
		}
		Map<Integer, Route> routesByRouteId = routeManager.getRoutesByIdsAsMap(routeIds);
		
		
		/* Verify that all routes exist and are connected properly */
		/* If any routes are private, they must be created by the same user as the trip plan creator */
		int idx = 0;
		int prevEndpoint = 0;
		int distance = 0;
		int elevationGain = 0;
		for (TripPlanSegment segment : tripPlan.getSegments()) {
			if (!routesByRouteId.containsKey(segment.getRoute())) {
				return WrappedResponse.failure("Trip Plan contains non-existant route(s)");
			}
			Route route = routesByRouteId.get(segment.getRoute());
			
			if (route.getIsPublic() == 0 && route.getCreatedBy() != tripPlan.getCreatedBy()) {
				return WrappedResponse.failure("Cannot create trip plan using someone else's private routes");
			}
			
			if (idx > 0) {
				int startOfSegment = segment.getDirection() == 1 ? route.getStart() : route.getEnd();
				if (startOfSegment != prevEndpoint) {
					return WrappedResponse.failure("Trip Plan segments must be connected.");
				}
			}
			
			distance += route.getDistance();
			elevationGain += segment.getDirection() == 1 ? route.getElevationGain() : route.getReverseGain();
			
			prevEndpoint = segment.getDirection() == 1 ? route.getEnd() : route.getStart();
			idx++;
		}
		
		TripPlanSegment firstSegment = tripPlan.getSegments().get(0);
		TripPlanSegment lastSegment = tripPlan.getSegments().get(tripPlan.getSegments().size()-1);
		Route firstRoute = routesByRouteId.get(firstSegment.getRoute());
		Route lastRoute = routesByRouteId.get(lastSegment.getRoute());
		
		// Ignore startingPoint and endingPoint , recompute them
		tripPlan.setStartingPoint(firstSegment.getDirection() == 1 ? firstRoute.getStart() : firstRoute.getEnd());
		tripPlan.setEndingPoint(lastSegment.getDirection() == 1 ? lastRoute.getEnd() : lastRoute.getStart());

		// Ignore distances and elevation gains, recompute them
		tripPlan.setDistance(distance);
		tripPlan.setElevationGain(elevationGain);
		tripPlan.setOriginalDistance(distance);
		tripPlan.setOriginalGain(elevationGain);
		
		// Verify that no other plan has the same equalityString
		String equalityString = constructEqualityString(tripPlan);
		if (!tripDao.getPlansByEqualityString(equalityString).isEmpty()) {
			return WrappedResponse.failure("Another identical trip plan already exists.");
		}
		
		String creationTime = PlaceMapper.format.format(new Date(System.currentTimeMillis()));
		tripDao.insertTripPlan(tripPlan.getDays(),
				tripPlan.getName(),
				tripPlan.getDistance(), 
				tripPlan.getElevationGain(), 
				tripPlan.getCreatedBy(), 
				"",
				tripPlan.getOriginalDistance(),
				tripPlan.getOriginalGain(),
				tripPlan.getStartingPoint(),
				tripPlan.getEndingPoint(),
				equalityString,
				tripPlan.getIsMappable(),
				creationTime);
		
		int tripId = tripDao.lastInsertId();
		
		for (TripPlanSegment segment : tripPlan.getSegments()) {
			segment.setTripPlan(tripId);
			tripDao.insertTripPlanSegment(tripId,
					segment.getDay(), 
					segment.getSequence(), 
					segment.getRoute(), 
					segment.getDirection(),
					segment.getMode());
		}
		
		tripPlan.setId(tripId);
		
		try {
			tripDao.setOriginalPlan(tripId, mapper.writeValueAsString(constructOriginalPlan(tripPlan)));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to serialize originalPlan for trip id " + tripId);
		}
		
		return WrappedResponse.success(getTripPlanById(tripId).get());
	}
	
	public Optional<TripPlan> getTripPlanByEqualityString(String equalityString) {
		List<TripPlan> plans = tripDao.getPlansByEqualityString(equalityString);
		
		if (plans.isEmpty()) {
			return Optional.absent();
		}
		
		return Optional.of(plans.get(0));
	}
		
	public List<TripPlan> getTripPlans(TripPlanSearchCriteria criteria) {
		Handle h = HelloWorldService.jdbi.open();
		
		Query<TripPlan> query = h.createQuery("SELECT a.id, a.name, a.days, a.distance, a.elevation_gain, a.created_by, a.original_distance, "
				+ "a.original_gain, a.starting_point, a.ending_point, a.is_mappable, a.creation_time, a.equality_string FROM trip_plans a WHERE "
				+ criteria.getAsWhereClause("a"))
			.map(new TripPlanMapper());
		
		criteria.applyBindingsForQuery(query);
		
		List<TripPlan> plans = query.list();
		
		h.close();
		
		return plans;
	}
	
	public Optional<Trip> getTripById(int tripId) {
		
		Trip trip = tripDao.getTripWithPlanById(tripId);
		if (trip == null) { return Optional.absent(); }
		
		trip.setTripMembers(tripDao.getTripMembersForTrip(tripId));
		
		populateSegmentsAndTilesForPlan(trip.getPlan());
		
		return Optional.of(trip);
	}
	
	public List<Trip> getTrips(TripSearchCriteria criteria) {
		Handle h = HelloWorldService.jdbi.open();
		
		String queryString = "select t.*, p.id p_id, "
				+ "p.days p_days, p.distance p_distance, p.elevation_gain p_elevation_gain, "
				+ "p.created_by p_created_by, p.original_distance p_original_distance, "
				+ "p.original_gain p_original_gain, p.starting_point p_starting_point, "
				+ "p.ending_point p_ending_point, p.equality_string p_equality_string, "
				+ "p.name p_name, p.is_mappable p_is_mappable, p.creation_time p_creation_time "
				+ "from trips t, trip_plans p where t.plan = p.id AND ";
		queryString += criteria.getAsWhereClause("t","p");
		if (criteria.getTripPlanCriteria().isPresent()) {
			queryString += " AND ";
			queryString += criteria.getTripPlanCriteria().get().getAsWhereClause("p");
		}

		Query<Trip> query = h.createQuery(queryString).map(new TripWithPlanMapper());
		
		criteria.applyBindingsForQuery(query);
		if (criteria.getTripPlanCriteria().isPresent()) {
			criteria.getTripPlanCriteria().get().applyBindingsForQuery(query);
		}
		
		List<Trip> trips = query.list();
		
		h.close();
		
		return trips;
	}

}
