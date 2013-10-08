package org.sean.hiking.trip;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.sean.hiking.route.RouteMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class TripPlanMapper implements ResultSetMapper<TripPlan> {

	public TripPlan map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			return new TripPlan(
					r.getInt("id"), 
					r.getString("name"),
					r.getInt("days"), 
					r.getInt("distance"), 
					r.getInt("elevation_gain"), 
					r.getInt("created_by"), 
					null,
					r.getInt("original_distance"),
					r.getInt("original_gain"),
					null, // tiles
					null,
					r.getInt("starting_point"),
					r.getInt("ending_point"),
					r.getInt("is_mappable"),
					r.getTimestamp("creation_time", RouteMapper.utcCalendar).getTime()/1000L
			);
	}
}
