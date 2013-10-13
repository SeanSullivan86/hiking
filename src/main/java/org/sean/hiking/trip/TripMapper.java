package org.sean.hiking.trip;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sean.hiking.route.RouteMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class TripMapper implements ResultSetMapper<Trip> {

	public Trip map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			return new Trip(
					r.getInt("id"), 
					null, // tripPlan
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
