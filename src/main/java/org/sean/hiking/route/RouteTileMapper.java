package org.sean.hiking.route;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class RouteTileMapper implements ResultSetMapper<RouteTilePair> {
	public RouteTilePair map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new RouteTilePair(r.getInt("route"), r.getString("tile"));
	}
}