package org.sean.hiking.trip;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator
public interface TripDao {
	
	@SqlUpdate("INSERT INTO trips (plan, trip_date, created_by, extra_distance, extra_gain, creation_time) VALUES (:plan, :tripDate, :createdBy, :extraDistance, :extraGain, :creationTime")
	public void insertTrip(@Bind("plan") int planId,
			@Bind("tripDate") String tripDate,
			@Bind("createdBy") String createdBy,
			@Bind("extraDistance") int extraDistance,
			@Bind("extraGain") int extraGain,
			@Bind("creationTime") String creationTime);
	
	@SqlQuery("SELECT * FROM trips WHERE id = :id")
	@RegisterMapper(TripMapper.class)
	public Trip getTripById(@Bind("id") int id);
	

	
	@SqlUpdate("INSERT INTO trip_plans (days, name, distance, elevation_gain, created_by, original_plan, original_distance, original_gain, starting_point, ending_point, equality_string, is_mappable, creation_time) VALUES (:days, :name, :distance, :elevationGain, :createdBy, :originalPlan, :originalDistance, :originalGain, :startingPoint, :endingPoint, :equalityString, :isMappable, :creationTime)")
	public void insertTripPlan(@Bind("days") int days,
			@Bind("name") String name,
			@Bind("distance") int distance,
			@Bind("elevationGain") int elevationGain,
			@Bind("createdBy") int createdBy,
			@Bind("originalPlan") String originalPlan,
			@Bind("originalDistance") int originalDistance,
			@Bind("originalGain") int originalGain,
			@Bind("startingPoint") int startingPoint,
			@Bind("endingPoint") int endingPoint,
			@Bind("equalityString") String equalityString,
			@Bind("isMappable") int isMappable,
			@Bind("creationTime") String creationTime);
	
	@SqlUpdate("INSERT INTO trip_plan_segments (trip, day, sequence, route, direction, mode) VALUES (:trip, :day, :sequence, :route, :direction, :mode)")
	public void insertTripPlanSegment(@Bind("trip") int trip,
			@Bind("day") int day,
			@Bind("sequence") int sequence,
			@Bind("route") int route,
			@Bind("direction") int direction,
			@Bind("mode") int mode);
	
	@SqlUpdate("DELETE FROM trip_plan_segments where trip = :planId")
	public void removeAllSegmentsForTripPlan(@Bind("planId") int planId);
	
	
	@SqlUpdate("UPDATE trip_plans SET distance = :distance, elevation_gain = :elevationGain WHERE id = :id")
	public void updateDistanceAndGain(@Bind("id") int planId,
			@Bind("distance") int distance,
			@Bind("elevationGain") int elevationGain);
	
	@SqlUpdate("UPDATE trip_plans SET is_mappable = 0, distance = original_distance, elevation_gain = original_gain WHERE id = :id")
	public void setPlanAsUnmappable(@Bind("id") int tripPlanId);
	
	@RegisterMapper(TripPlanMapper.class)
	@SqlQuery("SELECT id, name, days, distance, elevation_gain, created_by, original_distance, original_gain, starting_point, ending_point, is_mappable, creation_time FROM trip_plans WHERE id = :id")
	public TripPlan getPlanById(@Bind("id") int id);
	
	@RegisterMapper(TripPlanMapper.class)
	@SqlQuery("SELECT id, name, days, distance, elevation_gain, created_by, original_distance, original_gain, starting_point, ending_point, is_mappable, creation_time FROM trip_plans WHERE id IN (SELECT trip FROM trip_plan_segments WHERE route = :routeId)")
	public List<TripPlan> getPlansByRoute(@Bind("routeId") int routeId);
	
	@SqlQuery("SELECT id, name, days, distance, elevation_gain, created_by, original_distance, original_gain, starting_point, ending_point, is_mappable, creation_time FROM trip_plans WHERE equality_string = :equalityString")
	public List<TripPlan> getPlansByEqualityString(@Bind("equalityString") String equalityString);
	
	@SqlQuery("SELECT original_plan FROM trip_plans WHERE id = :id")
	public String getOriginalPlanById(@Bind("id") int id);
	
	@SqlUpdate("UPDATE trip_plans SET original_plan = :originalPlan WHERE id = :id")
	public void setOriginalPlan(@Bind("id") int id, @Bind("originalPlan") String originalPlan);
	
	@RegisterMapper(TripPlanSegmentMapper.class)
	@SqlQuery("SELECT * FROM trip_plan_segments WHERE trip = :trip ORDER BY day, sequence")
	public List<TripPlanSegment> getSegmentsForPlan(@Bind("trip") int tripId);
	
	
	
	@SqlQuery("SELECT LAST_INSERT_ID()")
	int lastInsertId();
	
}
