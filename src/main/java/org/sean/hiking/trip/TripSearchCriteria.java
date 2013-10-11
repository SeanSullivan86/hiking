package org.sean.hiking.trip;

import java.util.List;

import org.joda.time.LocalDate;
import org.skife.jdbi.v2.Query;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class TripSearchCriteria {
	
	private Range<LocalDate> dateRange;
	
	private Range<Integer> totalDistances;
	private Range<Integer> totalElevationGains;
	
	private Optional<Integer> createdBy;
	
	private Optional<TripPlanSearchCriteria> tripPlanCriteria;

	public TripSearchCriteria(Range<LocalDate> dateRange,
			Range<Integer> totalDistances, Range<Integer> totalElevationGains,
			Optional<Integer> createdBy,
			Optional<TripPlanSearchCriteria> tripPlanCriteria) {
		this.dateRange = dateRange;
		this.totalDistances = totalDistances;
		this.totalElevationGains = totalElevationGains;
		this.createdBy = createdBy;
		this.tripPlanCriteria = tripPlanCriteria;
	}

	public Range<LocalDate> getDateRange() {
		return dateRange;
	}

	public Range<Integer> getTotalDistances() {
		return totalDistances;
	}

	public Range<Integer> getTotalElevationGains() {
		return totalElevationGains;
	}

	public Optional<Integer> getCreatedBy() {
		return createdBy;
	}

	public Optional<TripPlanSearchCriteria> getTripPlanCriteria() {
		return tripPlanCriteria;
	}
	
	public String getAsWhereClause(String tripTableAlias, String planTableAlias) {
		StringBuilder str = new StringBuilder("1 = 1");
		if (dateRange.hasLowerBound()) {
			str.append(" AND " + tripTableAlias + ".trip_date >= :minTripDate");
		}
		if (dateRange.hasUpperBound()) {
			str.append(" AND " + tripTableAlias + ".trip_date <= :maxTripDate");
		}
		if (totalDistances.hasLowerBound()) {
			str.append(" AND (" + tripTableAlias + ".extra_distance + " + planTableAlias + ".distance) >= :minTotalDistance");
		}
		if (totalDistances.hasUpperBound()) {
			str.append(" AND (" + tripTableAlias + ".extra_distance + " + planTableAlias + ".distance) <= :maxTotalDistance");
		}
		if (totalElevationGains.hasLowerBound()) {
			str.append(" AND (" + tripTableAlias + ".extra_gain + " + planTableAlias + ".elevation_gain) >= :minTotalGain");
		}
		if (totalElevationGains.hasUpperBound()) {
			str.append(" AND (" + tripTableAlias + ".extra_gain + " + planTableAlias + ".elevation_gain) <= :maxTotalGain");
		}
		if (createdBy.isPresent()) {
			str.append(" AND " + tripTableAlias + ".created_by = :tripCreatedBy"); 
		}

		return str.toString();
	}
	
	public void applyBindingsForQuery(Query<?> query) {
		if (dateRange.hasLowerBound()) {
			query.bind("minTripDate", dateRange.lowerEndpoint().toString());
		}
		if (dateRange.hasUpperBound()) {
			query.bind("maxTripDate", dateRange.upperEndpoint().toString());
		}
		if (totalDistances.hasLowerBound()) {
			query.bind("minTotalDistance", totalDistances.lowerEndpoint());
		}
		if (totalDistances.hasUpperBound()) {
			query.bind("maxTotalDistance", totalDistances.upperEndpoint());
		}
		if (totalElevationGains.hasLowerBound()) {
			query.bind("minTotalGain", totalElevationGains.lowerEndpoint());
		}
		if (totalElevationGains.hasUpperBound()) {
			query.bind("maxTotalGain", totalElevationGains.upperEndpoint());
		}
		if (createdBy.isPresent()) {
			query.bind("tripCreatedBy", createdBy.get());
		}
	}
	

}
