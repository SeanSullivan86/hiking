package org.sean.hiking.route;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class RouteMapper implements ResultSetMapper<Route> {
		
	public static Calendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	
	public Route map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			return new Route(r.getInt("id"),
					r.getInt("start"),
					r.getInt("end"),
					r.getInt("distance"),
					r.getInt("elevation_gain"),
					r.getInt("reverse_gain"),
					r.getString("type"),
					r.getString("subtype"),
					r.getString("name"),
					null,
					null,
					r.getInt("is_public"),
					r.getInt("created_by"),
					r.getTimestamp("creation_time", utcCalendar).getTime()/1000L,
					r.getInt("last_updated_by"),
					r.getTimestamp("last_updated_time", utcCalendar).getTime()/1000L
			);

	}
}
