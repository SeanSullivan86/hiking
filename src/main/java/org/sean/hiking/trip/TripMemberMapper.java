package org.sean.hiking.trip;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class TripMemberMapper implements ResultSetMapper<TripMember> {
	public TripMember map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new TripMember(
				r.getInt("trip"),
				r.getInt("user"), 
				r.getString("name"));
	}
}