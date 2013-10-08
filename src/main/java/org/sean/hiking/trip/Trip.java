package org.sean.hiking.trip;

public class Trip {
	
	private int id;
	private TripPlan plan;
	private int planId;
	private long tripDate;
	
	private int extraDistance;
	private int extraGain;
	
	private int createdBy;
	private long creationTime;
	
	public Trip () {}

	public Trip(int id, TripPlan plan, int planId, long tripDate,
			int extraDistance, int extraGain, int createdBy, long creationTime) {
		this.id = id;
		this.plan = plan;
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
	
	

}
