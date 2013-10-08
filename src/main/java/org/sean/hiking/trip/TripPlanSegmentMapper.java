package org.sean.hiking.trip;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class TripPlanSegmentMapper implements ResultSetMapper<TripPlanSegment> {
	public TripPlanSegment map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new TripPlanSegment(
				r.getInt("trip"),
				r.getInt("day"), 
				r.getInt("sequence"), 
				r.getInt("route"), 
				r.getInt("direction"),
				r.getInt("mode"));
		
	}
}