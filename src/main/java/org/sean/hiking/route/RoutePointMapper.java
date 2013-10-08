package org.sean.hiking.route;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class RoutePointMapper implements ResultSetMapper<RoutePoint> {
	public RoutePoint map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new RoutePoint(r.getInt("route"),
				r.getInt("sequence"),
				r.getDouble("latitude"),
				r.getDouble("longitude"));
	}
}