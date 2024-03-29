package org.sean.hiking.trip;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.sean.hiking.WrappedResponse;

public class Trip {
	
	private int id;
	private TripPlan plan;
	private List<TripMember> tripMembers;
	private int planId;
	private long tripDate;
	
	private int extraDistance;
	private int extraGain;
	
	private int createdBy;
	private long creationTime;
	
	public Trip () {}

	public Trip(int id, TripPlan plan, List<TripMember> tripMembers, int planId, long tripDate,
			int extraDistance, int extraGain, int createdBy, long creationTime) {
		this.id = id;
		this.plan = plan;
		this.tripMembers = tripMembers;
		this.planId = planId;
		this.tripDate = tripDate;
		this.extraDistance = extraDistance;
		this.extraGain = extraGain;
		this.createdBy = createdBy;
		this.creationTime = creationTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TripPlan getPlan() {
		return plan;
	}

	public void setPlan(TripPlan plan) {
		this.plan = plan;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public long getTripDate() {
		return tripDate;
	}

	public void setTripDate(long tripDate) {
		this.tripDate = tripDate;
	}

	public int getExtraDistance() {
		return extraDistance;
	}

	public void setExtraDistance(int extraDistance) {
		this.extraDistance = extraDistance;
	}

	public int getExtraGain() {
		return extraGain;
	}

	public void setExtraGain(int extraGain) {
		this.extraGain = extraGain;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public List<TripMember> getTripMembers() {
		return tripMembers;
	}

	public void setTripMembers(List<TripMember> tripMembers) {
		this.tripMembers = tripMembers;
	}
	
	
	public WrappedResponse<String> validateFields() {
		if (planId <= 0) {
			return WrappedResponse.failure("Missing a trip plan for the new trip");
		}
		
		if (tripMembers == null || tripMembers.isEmpty()) {
			return WrappedResponse.failure("New Trip has no trip members");
		}
		
		if (tripMembers.size() > 100) {
			return WrappedResponse.failure("Trips can have at most 100 members");
		}
		
		DateTime dateTime = new DateTime(creationTime*1000L, DateTimeZone.UTC);
		if (dateTime.getMillisOfDay() != 0) {
			return WrappedResponse.failure("Trip Date must be at midnight UTC, specified in epoch seconds");
		}
		
		// TODO extraDistance, extraGain
				
		return WrappedResponse.success("Valid");
	}

}
