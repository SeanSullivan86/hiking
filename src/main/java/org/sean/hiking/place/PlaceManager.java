package org.sean.hiking.place;

import java.util.Date;
import java.util.List;

import org.sean.hiking.WrappedResponse;
import org.sean.hiking.coordinates.EarthPosition2D;
import org.sean.hiking.route.Route;
import org.sean.hiking.route.RouteManager;

import com.google.common.base.Optional;

public class PlaceManager {

	private PlacesDao placesDao;
	private RouteManager routeManager;
	
	public void setRouteManager(RouteManager routeManager) {
		this.routeManager = routeManager;
	}
	
	public PlaceManager(PlacesDao placesDao) {
		this.placesDao = placesDao;
	}
	
	private void editPlaceLocation(int id, EarthPosition2D newLocation, int updatingUser) {
		// edit the position of the place
		placesDao.updateLocation(id, newLocation.getLatitude(), newLocation.getLongitude());
		placesDao.updateTile(id);
		
		// modify the RoutePoints for any routes which start/end at this location
		for (Route route : routeManager.getRoutesForEndpoint(id)) {
			if (route.getStart() == id) {
				routeManager.updateStartingPoint(route.getId(), newLocation);
			}
			if (route.getEnd() == id) {
				routeManager.updateEndingPoint(route.getId(), newLocation);
			}
			routeManager.fixRouteTiles(route.getId());
			routeManager.updateAuditColumns(route.getId(), updatingUser);
		}
	}
	
	private void editPlaceElevation(int id, int newElevation, int updatingUser) {
		placesDao.updateElevation(id, newElevation);
		
		for (Route route : routeManager.getRoutesForEndpoint(id)) {
			routeManager.resolveElevationConflict(route);
			routeManager.updateAuditColumns(route.getId(), updatingUser);
		}
	}
	
	public void deletePlace(int id) {
		placesDao.delete(id);
	}
	
	public WrappedResponse<Place> insertPlace(Place place) {
		if (place == null) return WrappedResponse.failure("Cannot insert null place");
		
		WrappedResponse<String> fieldValidation = place.validateFields();
		
		if (! fieldValidation.isSuccess()) {
			return WrappedResponse.failure("Cannot insert place (" + fieldValidation.getMessage() + ")");
		}
		
		String creationTime = PlaceMapper.format.format(new Date(System.currentTimeMillis()));
		placesDao.insert(place.getName(),
    			place.getLatitude(), 
    			place.getLongitude(), 
    			place.getElevation(), 
    			place.getType(),
    			place.getTile(),
    			place.getIsPublic(),
    			place.getCreatedBy(),
    			creationTime,
    			place.getCreatedBy(),
    			creationTime
    			);
    	
    	return WrappedResponse.success(placesDao.findPlaceForId(placesDao.lastInsertId()));
	}
	
	public WrappedResponse<Place> updatePlace(Place place) {
		if (place == null) return WrappedResponse.failure("Cannot update null place");
		
		WrappedResponse<String> fieldValidation = place.validateFields();
		if (! fieldValidation.isSuccess()) {
			return WrappedResponse.failure("Cannot update place (" + fieldValidation.getMessage() + ")");
		}
		
		Optional<Place> existingPlace = getPlaceById(place.getId());
		
		if (!existingPlace.isPresent()) {
			return WrappedResponse.failure("The place you tried to update does not exist");
		}
				
		if (Math.abs(place.getLatitude() - existingPlace.get().getLatitude()) > 0.000001 ||
				Math.abs(place.getLongitude() - existingPlace.get().getLongitude()) > 0.000001) {
			editPlaceLocation(place.getId(), new EarthPosition2D(place.getLatitude(), place.getLongitude()), place.getLastUpdatedBy());
		}
		if (place.getElevation() != existingPlace.get().getElevation()) {
			editPlaceElevation(place.getId(), place.getElevation(), place.getLastUpdatedBy());
		}
		if (!place.getName().equals(existingPlace.get().getName())) {
			placesDao.updateName(place.getId(), place.getName());
		}
		if (!place.getType().equals(existingPlace.get().getType())) {
			placesDao.updateType(place.getId(), place.getType());
		}
		
		String updateTime = PlaceMapper.format.format(new Date(System.currentTimeMillis()));
		placesDao.updateAuditColumns(place.getId(), place.getLastUpdatedBy(), updateTime);
		
		return WrappedResponse.success(placesDao.findPlaceForId(place.getId()));
	}
	
	public List<Place> getPlacesByTile(String tile) {
		return placesDao.findPlacesForTile(tile);
	}
	
	public List<Place> getPlacesForTiles(List<String> tiles) {
		return placesDao.findPlacesForTiles(tiles);
	}
	
	public List<Place> getPlacesByIds(List<Integer> ids) {
		return placesDao.findPlacesByIds(ids);
	}
	
	public List<Place> getAllPlaces() {
		return placesDao.findAll();
	}
	
	/** Get a place by its id, or Optional.absent() if there is no place */
	public Optional<Place> getPlaceById(int id) {
		return Optional.fromNullable(placesDao.findPlaceForId(id));
	}
}
