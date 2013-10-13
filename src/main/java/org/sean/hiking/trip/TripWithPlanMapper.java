package org.sean.hiking.trip;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sean.hiking.route.RouteMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class TripWithPlanMapper implements ResultSetMapper<Trip> {

	public Trip map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			return new Trip(
					r.getInt("id"), 
					 new TripPlan(
								r.getInt("p_id"), 
								r.getString("p_name"),
								r.getInt("p_days"), 
								r.getInt("p_distance"), 
								r.getInt("p_elevation_gain"), 
								r.getInt("p_created_by"), 
								null, // originalPlan
								r.getInt("p_original_distance"),
								r.getInt("p_original_gain"),
								null, // tiles
								null, // segments
								r.getInt("p_starting_point"),
								r.getInt("p_ending_point"),
								r.getInt("p_is_mappable"),
								r.getTimestamp("p_creation_time", RouteMapper.utcCalendar).getTime()/1000L
						),
					null, // tripMembers
					r.getInt("plan"),
					r.getTimestamp("trip_date", RouteMapper.utcCalendar).getTime()/1000L,
					r.getInt("extra_distance"),
					r.getInt("extra_gain"),
					r.getInt("created_by"),
					r.getTimestamp("creation_time", RouteMapper.utcCalendar).getTime()/1000L
			);
	}
}
