package org.sean.hiking.place;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.sean.hiking.route.RouteMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class PlaceMapper implements ResultSetMapper<Place> {

	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public Place map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			return new Place(
					r.getInt("id"),
					r.getString("name"),
					r.getDouble("latitude"),
					r.getDouble("longitude"),
					r.getInt("elevation"),
					r.getString("type"),
					r.getString("tile"),
					r.getInt("is_public"),
					r.getInt("created_by"),
					r.getTimestamp("creation_time", RouteMapper.utcCalendar).getTime()/1000L,
					r.getInt("last_updated_by"),
					r.getTimestamp("last_updated_time", RouteMapper.utcCalendar).getTime()/1000L
					);
	}
}
