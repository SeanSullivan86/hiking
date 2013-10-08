package org.sean.hiking.trip;

import java.util.List;

import org.skife.jdbi.v2.Query;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class TripPlanSearchCriteria {
	
	private Range<Integer> distances;
	private Range<Integer> elevationGains;
	private Range<Integer> days;
	
	private Optional<Integer> startingPlace;
	private Optional<Integer> endingPlace;
	
	private Optional<List<Integer>> placesVisited;

	public TripPlanSearchCriteria(Range<Integer> distances,
			Range<Integer> elevationGains, Range<Integer> days,
			Optional<Integer> startingPlace, Optional<Integer> endingPlace,
			Optional<List<Integer>> placesVisited) {
		this.distances = distances;
		this.elevationGains = elevationGains;
		this.days = days;
		this.startingPlace = startingPlace;
		this.endingPlace = endingPlace;
		this.placesVisited = placesVisited;
	}

	public Range<Integer> getDistances() {
		return distances;
	}

	public void setDistances(Range<Integer> distances) {
		this.distances = distances;
	}

	public Range<Integer> getElevationGains() {
		return elevationGains;
	}

	public void setElevationGains(Range<Integer> elevationGains) {
		this.elevationGains = elevationGains;
	}

	public Range<Integer> getDays() {
		return days;
	}

	public void setDays(Range<Integer> days) {
		this.days = days;
	}

	public Optional<Integer> getStartingPlace() {
		return startingPlace;
	}

	public void setStartingPlace(Optional<Integer> startingPlace) {
		this.startingPlace = startingPlace;
	}

	public Optional<Integer> getEndingPlace() {
		return endingPlace;
	}

	public void setEndingPlace(Optional<Integer> endingPlace) {
		this.endingPlace = endingPlace;
	}

	public Optional<List<Integer>> getPlacesVisited() {
		return placesVisited;
	}

	public void setPlacesVisited(Optional<List<Integer>> placesVisited) {
		this.placesVisited = placesVisited;
	}
	
	public String getAsWhereClause(String tableAlias) {
		StringBuilder str = new StringBuilder("1 = 1");
		if (distances.hasLowerBound()) {
			str.append(" AND " + tableAlias + ".distance >= :minDistance");
		}
		if (distances.hasUpperBound()) {
			str.append(" AND " + tableAlias + ".distance <= :maxDistance");
		}
		if (elevationGains.hasLowerBound()) {
			str.append(" AND " + tableAlias + ".elevation_gain >= :minGain");
		}
		if (elevationGains.hasUpperBound()) {
			str.append(" AND " + tableAlias + ".elevation_gain <= :maxGain");
		}
		if (days.hasLowerBound()) {
			str.append(" AND " + tableAlias + ".days >= :minDays");
		}
		if (days.hasUpperBound()) {
			str.append(" AND " + tableAlias + ".days <= :maxDays");
		}
		if (startingPlace.isPresent()) {
			str.append(" AND " + tableAlias + ".starting_point = :startingPoint"); 
		}
		if (endingPlace.isPresent()) {
			str.append(" AND " + tableAlias + ".ending_point = :endingPoint");
		}
		if (placesVisited.isPresent()) {
			List<Integer> places = placesVisited.get();
			for (int i = 0; i < places.size(); i++) {
				str.append(" AND " + tableAlias + ".id IN (SELECT trip FROM trip_plan_segments tpss, routes rr WHERE tpss.route = rr.id AND (rr.start = :place"+i+" OR rr.end = :place"+i+"))");
			}
		}
		return str.toString();
	}
	
	public void applyBindingsForQuery(Query<?> query) {
		if (distances.hasLowerBound()) {
			query.bind("minDistance", distances.lowerEndpoint());
		}
		if (distances.hasUpperBound()) {
			query.bind("maxDistance", distances.upperEndpoint());
		}
		if (elevationGains.hasLowerBound()) {
			query.bind("minGain", elevationGains.lowerEndpoint());
		}
		if (elevationGains.hasUpperBound()) {
			query.bind("maxGain", elevationGains.upperEndpoint());
		}
		if (days.hasLowerBound()) {
			query.bind("minDays", days.lowerEndpoint());
		}
		if (days.hasUpperBound()) {
			query.bind("maxDays", days.upperEndpoint());
		}
		if (startingPlace.isPresent()) {
			query.bind("startingPoint", startingPlace.get());
		}
		if (endingPlace.isPresent()) {
			query.bind("endingPoint", endingPlace.get());
		}
		if (placesVisited.isPresent()) {
			List<Integer> places = placesVisited.get();
			for (int i = 0; i < places.size(); i++) {
				query.bind("place"+i, places.get(i));
			}
		}
	}
	

}
