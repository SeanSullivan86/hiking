package org.sean.hiking.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.HttpHeaders;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.coordinates.EarthPosition2D;
import org.sean.hiking.place.Place;
import org.sean.hiking.place.PlaceManager;
import org.sean.hiking.place.PlaceMapper;
import org.sean.hiking.trip.TripManager;
import org.sean.hiking.user.User;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RouteManager {
	
	private RoutesDao routesDao;
	private PlaceManager placeManager;
	private TripManager tripManager;
	
	public void setPlaceManager(PlaceManager placeManager) {
		this.placeManager = placeManager;
	}
	
	public void setTripManager(TripManager tripManager) {
		this.tripManager = tripManager;
	}
	
	public RouteManager(RoutesDao routesDao) {
		this.routesDao = routesDao;
	}
	
	public void deleteRoute(int id) {
		routesDao.deletePathsForRoute(id);
		routesDao.deleteTilesForRoute(id);
		routesDao.deleteRoute(id);
		
		tripManager.updatePlansToHandleDeletedRoute(id);
	}
	
	public WrappedResponse<Route> insertRoute(Route route) {
		if (route == null) return WrappedResponse.failure("Cannot insert null place");
		
		// Validate basic sanity of fields (no DB checks)
		WrappedResponse<String> fieldValidation = route.validateFields();
		if (! fieldValidation.isSuccess()) {
			return WrappedResponse.failure("Cannot insert route (" + fieldValidation.getMessage() + ")");
		}
		
		// Check existence of endpoints
		Optional<Place> start = this.placeManager.getPlaceById(route.getStart());
		Optional<Place> end = this.placeManager.getPlaceById(route.getEnd());
		if (!start.isPresent() || !end.isPresent()) {
			return WrappedResponse.failure("Route endpoint(s) do not exist");
		}
		
		// If either endpoint is private, then the new route must also be private
		if (start.get().getIsPublic() == 0 || end.get().getIsPublic() == 0) {
			if (route.getIsPublic() == 1) {
				return WrappedResponse.failure("Routes with non-public endpoints must also be non-public");
			}
		}
		
		if (start.get().getIsPublic() == 0 && (start.get().getCreatedBy() != route.getCreatedBy())) {
			return WrappedResponse.failure("You cannot create a non-public route attached to endpoints that were created by a different user");
		}
		if (end.get().getIsPublic() == 0 && (end.get().getCreatedBy() != route.getCreatedBy())) {
			return WrappedResponse.failure("You cannot create a non-public route attached to endpoints that were created by a different user");
		}
		
		// Check that route path starts at starting point and ends at ending point
		if (!route.getPath().get(0).isApproximatelyEqualTo(start.get().getLocation()) ||
		    !route.getPath().get(route.getPath().size()-1).isApproximatelyEqualTo(end.get().getLocation())) {
			return WrappedResponse.failure("Route path polyline must start and end at the route endpoint place markers");
		}
		
		// Ignore 'tiles' in Route, replace with value known to be correct
		route.setTiles(getTilesForRoute(route.getPath()));
		
		/** Passed all checks, go ahead and insert */
		String creationTime = PlaceMapper.format.format(new Date(System.currentTimeMillis()));
    	routesDao.insertRoute(
    			route.getStart(), 
    			route.getEnd(), 
    			route.getDistance(),
    			route.getElevationGain(),
    			route.getReverseGain(),
    			route.getType(),
    			route.getSubtype(),
    			route.getName() == null ? "" : route.getName(),
    			route.getIsPublic(),
    			route.getCreatedBy(),
    			creationTime,
    			route.getCreatedBy(),
    			creationTime
    			);
    	
    	int routeId = routesDao.lastInsertId();
    	
    	for (String tile : route.getTiles()) {
    		routesDao.insertRouteTile(routeId, tile);
    	}
    	
    	int seq = 1;
    	for (EarthPosition2D position : route.getPath()) {
    		routesDao.insertRoutePoint(routeId, seq, position.getLatitude(), position.getLongitude());
    		seq++;
    	}
    	
    	return WrappedResponse.success(getRouteById(routeId).get());
		
	}
	
	
	public WrappedResponse<Route> updateRoute(Route route) {
		Route existingRoute = routesDao.findRouteById(route.getId());
		
		if (existingRoute == null) {
			return WrappedResponse.failure("The route you are editing does not exist.");
		}
		
		// Fill in the tiles and path fields of existingRoute
		includePathAndTilesForRoute(existingRoute);

		// Validate basic sanity of new fields (no DB checks)
		WrappedResponse<String> fieldValidation = route.validateFields();
		if (! fieldValidation.isSuccess()) {
			return WrappedResponse.failure("Cannot update route (" + fieldValidation.getMessage() + ")");
		}
		
		Optional<Place> start = this.placeManager.getPlaceById(route.getStart());
		Optional<Place> end = this.placeManager.getPlaceById(route.getEnd());
		if (!start.isPresent() || !end.isPresent()) {
			return WrappedResponse.failure("Route endpoint(s) do not exist");
		}
		
		// Validate startElevation + elevationGain - reverseGain = endElevation
		if ((start.get().getElevation() + route.getElevationGain() - route.getReverseGain()) != end.get().getElevation()) {
			return WrappedResponse.failure("New elevation gains are invalid (startingElevation + elevationGain - reverseGain - endingElevation must equal 0)");
		}
		
		if (existingRoute.getIsPublic() == 0 && route.getIsPublic() == 1) {
			if (start.get().getIsPublic() == 0 || end.get().getIsPublic() == 0) {
				return WrappedResponse.failure("You cannot make a route public unless its endpoints are already public");
			}
		}
		
		// TODO Only allow public to private if you created the route and there are no plans using the route
		
		if (!route.getPath().get(0).isApproximatelyEqualTo(start.get().getLocation()) ||
			    !route.getPath().get(route.getPath().size()-1).isApproximatelyEqualTo(end.get().getLocation())) {
				return WrappedResponse.failure("Route path polyline must start and end at the route endpoint place markers");
			}
		
		// update existing route
		String updateTime = PlaceMapper.format.format(new Date(System.currentTimeMillis()));
		routesDao.updateRoute(route.getId(), route.getStart(), route.getEnd(), route.getDistance(), route.getElevationGain(), route.getReverseGain(), route.getType(), route.getSubtype(), route.getName() == null ? "" : route.getName(), route.getIsPublic(), route.getLastUpdatedBy(), updateTime);

		// If distance or elevationGain changed, update trip plans using that route
		if (route.getElevationGain() != existingRoute.getElevationGain() ||
				route.getReverseGain() != existingRoute.getReverseGain() ||
				route.getDistance() != existingRoute.getDistance()) {
			tripManager.recomputeDistancesForPlansUsingRoute(route.getId());
		}
		
		// if path has changed, fix the path and tiles
		if (! route.isPathEqualTo(existingRoute.getPath())) {
			routesDao.deletePathsForRoute(route.getId());

			int seq = 1;
			for (EarthPosition2D position : route.getPath()) {
				routesDao.insertRoutePoint(route.getId(), seq, position.getLatitude(), position.getLongitude());
				seq++;
			}

			fixRouteTiles(route.getId());
		}
    	
    	return WrappedResponse.success(getRouteById(route.getId()).get());
	}
	
    public WrappedResponse<String> splitRoute(
    		Route newRouteA,
    		Route newRouteB,
    		Route oldRoute,
    		Place newPlace) {
    	if (newRouteA == null || newRouteB == null || oldRoute == null || newPlace == null) {
    		return WrappedResponse.failure("Must provide 2 new routes, the old route, and the new place in order to split a route");
    	}
    	
    	Optional<Route> existingOldRoute = getRouteById(oldRoute.getId());
    	
    	if (!existingOldRoute.isPresent()) {
    		return WrappedResponse.failure("Route you are trying to split does not exist");
    	}
    	
    	// New routes/place get same public-ness as the old route
    	newRouteA.setIsPublic(existingOldRoute.get().getIsPublic());
    	newRouteB.setIsPublic(existingOldRoute.get().getIsPublic());
    	newPlace.setIsPublic(existingOldRoute.get().getIsPublic());
    	
    	newRouteA.setEnd(1); // fake value to let it pass field validation
    	newRouteB.setStart(1); // fake value to let it pass field validation
    	newPlace.setId(0);
    	
    	WrappedResponse<String> routeAValidation = newRouteA.validateFields();
    	WrappedResponse<String> routeBValidation = newRouteB.validateFields();
    	
    	if (!routeAValidation.isSuccess()) {
    		return WrappedResponse.failure("Unable to split route. Invalid new route segment (" + routeAValidation.getMessage() +")");
    	}
    	if (!routeBValidation.isSuccess()) {
    		return WrappedResponse.failure("Unable to split route. Invalid new route segment (" + routeBValidation.getMessage() +")");
    	}
    	
    	if (newRouteA.getStart() != existingOldRoute.get().getStart()) {
    		return WrappedResponse.failure("New route must have same starting point as the route being split");
    	}
    	if (newRouteB.getEnd() != existingOldRoute.get().getEnd()) {
    		return WrappedResponse.failure("New route must have same ending point as the route being split");
    	}

    	WrappedResponse<String> placeValidation = newPlace.validateFields();
    	if (!placeValidation.isSuccess()) {
    		return WrappedResponse.failure("Unable to split route. Invalid new place (" + placeValidation.getMessage() +")");
    	}
    	
    	Optional<Place> existingStartingPlace = placeManager.getPlaceById(existingOldRoute.get().getStart());
    	Optional<Place> existingEndingPlace = placeManager.getPlaceById(existingOldRoute.get().getEnd());
    	
    	if (!existingStartingPlace.isPresent() || !existingEndingPlace.isPresent()) {
    		return WrappedResponse.failure("Endpoints of route you are trying to split no longer exist.");
    	}
    	
    	// Validate startElevation + elevationGain - reverseGain = endElevation
    	if (((existingStartingPlace.get().getElevation() + newRouteA.getElevationGain() - newRouteA.getReverseGain()) != newPlace.getElevation()) ||
    			((newPlace.getElevation() + newRouteB.getElevationGain() - newRouteB.getReverseGain()) != existingEndingPlace.get().getElevation()))	{
    		return WrappedResponse.failure("New elevation gains are invalid (startingElevation + elevationGain - reverseGain - endingElevation must equal 0)");
    	}
    	
		if (!newRouteA.getPath().get(0).isApproximatelyEqualTo(existingStartingPlace.get().getLocation()) ||
			    !newRouteA.getPath().get(newRouteA.getPath().size()-1).isApproximatelyEqualTo(newPlace.getLocation())) {
			return WrappedResponse.failure("Route path polyline must start and end at the route endpoint place markers");
		}
		if (!newRouteB.getPath().get(0).isApproximatelyEqualTo(newPlace.getLocation()) ||
			    !newRouteB.getPath().get(newRouteB.getPath().size()-1).isApproximatelyEqualTo(existingEndingPlace.get().getLocation())) {
			return WrappedResponse.failure("Route path polyline must start and end at the route endpoint place markers");
		}    	
    	
		// insert the new place
    	WrappedResponse<Place> insertedNewPlace = placeManager.insertPlace(newPlace);
    	if (!insertedNewPlace.isSuccess()) {
    		return WrappedResponse.failure("Unable to split route. Unable to insert new place (" + insertedNewPlace.getMessage() + ")");
    	}
    	
    	// set the endpoints of the new routes to the newly inserted place
    	newRouteA.setEnd(insertedNewPlace.getResponse().getId());
    	newRouteB.setStart(insertedNewPlace.getResponse().getId());
    	
    	WrappedResponse<Route> insertedNewRouteA = insertRoute(newRouteA);
    	if (!insertedNewRouteA.isSuccess()) {
    		return WrappedResponse.failure("Unable to split route. Unable to insert new route (" + insertedNewRouteA.getMessage() + ")");
    	}
    	
    	WrappedResponse<Route> insertedNewRouteB = insertRoute(newRouteB);
    	if (!insertedNewRouteB.isSuccess()) {
    		return WrappedResponse.failure("Unable to split route. Unable to insert new route (" + insertedNewRouteB.getMessage() + ")");
    	}
    	
    	// Update trip plans to reflect the new routes
    	tripManager.replaceRouteWithSplitRoutes(existingOldRoute.get(), insertedNewRouteA.getResponse(), insertedNewRouteB.getResponse());
    	
    	deleteRoute(existingOldRoute.get().getId());
    	
    	insertSplitRoute(
    			existingOldRoute.get().getId(),
    			insertedNewRouteA.getResponse().getId(),
    			insertedNewRouteB.getResponse().getId());
    	
    	return WrappedResponse.success("Completed.");
    }
	
	public List<String> getTilesForRouteSet(Collection<Integer> routes) {
		List<RouteTilePair> pairs = routesDao.findTilesForRoutes(Lists.newArrayList(routes));
		
		Set<String> tiles = Sets.newHashSet();
		
		for (RouteTilePair pair : pairs) {
			tiles.add(pair.getTile());
		}
		
		return Lists.newArrayList(tiles);
	}
	
	public Optional<Route> getRouteById(int id) {
		Route route = routesDao.findRouteById(id);
		
		if (route == null) {
			return Optional.absent();
		}
		
		List<EarthPosition2D> path = new ArrayList<EarthPosition2D>();
		for (RoutePoint point : routesDao.findPathForRoute(id)) {
			path.add(point.getPosition());
		}
		
		route.setPath(path);
		
		List<String> tiles = new ArrayList<String>();
		for (RouteTilePair routeTilePair : routesDao.findTilesForRoute(id)) {
			tiles.add(routeTilePair.getTile());
		}
		
		route.setTiles(tiles);
		
		return Optional.of(route);
	}

	public List<Route> getRoutesForTiles(List<String> tiles) {
		List<Route> routes = routesDao.findRoutesForTiles(tiles);
		
		Map<Integer, Route> routesById = new HashMap<Integer, Route>();
		for (Route route : routes) {
			routesById.put(route.getId(), route);
			route.setPath(new ArrayList<EarthPosition2D>());
			route.setTiles(new ArrayList<String>());
		}
		
		List<Integer> routeIds = new ArrayList<Integer>(routesById.keySet());
		
		if (!routeIds.isEmpty()) {
			for (RoutePoint point : routesDao.findPathsForRoutes(routeIds)) {
				routesById.get(point.getRoute()).getPath().add(point.getPosition());
			}
			
			for (RouteTilePair routeTilePair : routesDao.findTilesForRoutes(routeIds)) {
				routesById.get(routeTilePair.getRoute()).getTiles().add(routeTilePair.getTile());
			}
		}
		
		return routes;
	}
	
	private void includePathAndTilesForRoute(Route route) {
		includePathAndTilesForRoutes(Lists.newArrayList(route));
	}
	
	private void includePathAndTilesForRoutes(List<Route> routes) {
		Map<Integer, Route> routesById = Maps.newHashMap();
		for (Route route : routes) {
			routesById.put(route.getId(), route);
			route.setPath(new ArrayList<EarthPosition2D>());
			route.setTiles(new ArrayList<String>());
		}
		
		List<Integer> routeIds = new ArrayList<Integer>(routesById.keySet());
		
		if (!routeIds.isEmpty()) {
			for (RoutePoint point : routesDao.findPathsForRoutes(routeIds)) {
				routesById.get(point.getRoute()).getPath().add(point.getPosition());
			}
			
			for (RouteTilePair routeTilePair : routesDao.findTilesForRoutes(routeIds)) {
				routesById.get(routeTilePair.getRoute()).getTiles().add(routeTilePair.getTile());
			}
		}
		
	}
	
	public void fixRouteTiles() {
		List<Route> routes = routesDao.findAllRoutes();
		
		for (Route route : routes) {
			route.setPath(new ArrayList<EarthPosition2D>());
			List<RoutePoint> points = routesDao.findPathForRoute(route.getId());
			for (RoutePoint point : points) {
				route.getPath().add(point.getPosition());
			}
			
			routesDao.deleteTilesForRoute(route.getId());
			
			List<String> tiles = getTilesForRoute(route.getPath());
			
			for (String tile : tiles) {
				routesDao.insertRouteTile(route.getId(), tile);
			}
		}
	}
	
	public void fixRouteTiles(int id) {
		Route route = routesDao.findRouteById(id);
		route.setPath(new ArrayList<EarthPosition2D>());
		List<RoutePoint> points = routesDao.findPathForRoute(route.getId());
		for (RoutePoint point : points) {
			route.getPath().add(point.getPosition());
		}
		
		routesDao.deleteTilesForRoute(route.getId());
		
		List<String> tiles = getTilesForRoute(route.getPath());
		
		for (String tile : tiles) {
			routesDao.insertRouteTile(route.getId(), tile);
		}
	}
	
	public void resolveAllElevationConflicts() {
		for (Route route : routesDao.findAllRoutes()) {
			resolveElevationConflict(route);
		}
	}
	
	/** Assume that the starting and ending elevations are correct. Assume that the smaller
	 * of "elevationGain" and "reverseGain" is correct. Deduce the last of the 4 values from
	 * the other 3.
	 */
	public void resolveElevationConflict(Route route) {
		Place startPlace = placeManager.getPlaceById(route.getStart()).get();
		Place endPlace = placeManager.getPlaceById(route.getEnd()).get();
		
		int oldElevationGain = route.getElevationGain();
		int oldReverseGain = route.getReverseGain();
		int newElevationGain = 0;
		int newReverseGain = 0;
		
		// startElevation + elevationGain - reverseGain = endElevation
		// elevationGain = reverseGain + endElevation - startElevation
		// reverseGain = startElevation - endElevation + elevationGain
		
		if (route.getElevationGain() <= route.getReverseGain()) {
			newReverseGain = startPlace.getElevation() - endPlace.getElevation() + route.getElevationGain();
			if (newReverseGain < 0) {
				newReverseGain = 0;
				newElevationGain = endPlace.getElevation() - startPlace.getElevation();
			} else {
				newElevationGain = route.getElevationGain();
			}
		} else {
			newElevationGain = route.getReverseGain() + endPlace.getElevation() - startPlace.getElevation();
			if (newElevationGain < 0) {
				newElevationGain = 0;
				newReverseGain = startPlace.getElevation() - endPlace.getElevation();
			} else {
				newReverseGain = route.getReverseGain();
			}
		}
		
		if (oldElevationGain != newElevationGain || oldReverseGain != newReverseGain) {
			routesDao.updateElevationGains(route.getId(), newElevationGain, newReverseGain);
			tripManager.recomputeDistancesForPlansUsingRoute(route.getId());
		}
	}
	
	public void updateStartingPoint(int id, EarthPosition2D newStart) {
		routesDao.updateRoutePoint(id, 1, newStart.getLatitude(), newStart.getLongitude());
	}
	
	public void updateEndingPoint(int id, EarthPosition2D newEnd) {
		int numPoints = routesDao.findPathForRoute(id).size();
		routesDao.updateRoutePoint(id, numPoints, newEnd.getLatitude(), newEnd.getLongitude());
	}
	
	public List<Route> getRoutesForEndpoint(int id) {
		List<Route> routes = routesDao.findRoutesByEndpoint(id);
		includePathAndTilesForRoutes(routes);
		return routes;
	}
	
	public List<Route> getRoutesByIds(List<Integer> ids) {
		List<Route> routes = routesDao.findRoutesByIds(ids);
		includePathAndTilesForRoutes(routes);
		return routes;
	}
	
	public Map<Integer,Route> getRoutesByIdsAsMap(Iterable<Integer> ids) {
		List<Route> routes = routesDao.findRoutesByIds(Lists.newArrayList(ids));
		includePathAndTilesForRoutes(routes);
		Map<Integer,Route> map = Maps.newHashMap();
		for (Route route : routes) {
			map.put(route.getId(), route);
		}
		return map;
	}
	

	public List<String> getTilesForRoute(List<EarthPosition2D> points) {
		double minLng = Double.MAX_VALUE;
		double minLat = Double.MAX_VALUE;
		double maxLng = Double.NEGATIVE_INFINITY;
		double maxLat = Double.NEGATIVE_INFINITY;
		
		for (EarthPosition2D point : points) {
			if (point.getLatitude() < minLat) minLat = point.getLatitude();
			if (point.getLatitude() > maxLat) maxLat = point.getLatitude();
			if (point.getLongitude() < minLng) minLng = point.getLongitude();
			if (point.getLongitude() > maxLng) maxLng = point.getLongitude();
		}
		
		int minX = (int) Math.floor(10*(minLng+200));
		int maxX = (int) Math.floor(10*(maxLng+200));
		int minY = (int) Math.floor(10*(minLat+100));
		int maxY = (int) Math.floor(10*(maxLat+100));
		
		List<String> tiles = new ArrayList<String>();
		
		for (int x = minX ; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				tiles.add(x+"_"+y);
			}
		}
		return tiles;
	}
	
	public void insertSplitRoute(int oldRoute, int newStartingRoute, int newEndingRoute) {
		routesDao.insertSplitRoute(oldRoute, newStartingRoute, newEndingRoute);
	}
	
	public void updateAuditColumns(int routeId, int updatingUser) {
		String updateTime = PlaceMapper.format.format(new Date(System.currentTimeMillis()));
		this.routesDao.updateAuditColumns(routeId, updatingUser, updateTime);
	}
}
