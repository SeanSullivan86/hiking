package org.sean.hiking.trip;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TripsAndPlans {
	
	private List<Trip> trips;
	private List<TripPlan> tripPlans;
	
	public TripsAndPlans(List<Trip> trips, List<TripPlan> tripPlans) {
		this.trips = trips;
		this.tripPlans = tripPlans;
	}
	
	public static TripsAndPlans fromTrips(List<Trip> trips) {
		List<TripPlan> plans = Lists.newArrayList();
		Set<Integer> plansSeen = Sets.newHashSet();
		
		for (Trip trip : trips) {
			if (!plansSeen.contains(trip.getPlanId())) {
				plans.add(trip.getPlan());
				plansSeen.add(trip.getPlanId());
			}
			trip.setPlan(null);
		}
		
		return new TripsAndPlans(trips, plans);
	}

}
