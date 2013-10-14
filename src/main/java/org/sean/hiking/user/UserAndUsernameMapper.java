package org.sean.hiking.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class UserAndUsernameMapper implements ResultSetMapper<UserAndUsername> {
	public UserAndUsername map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new UserAndUsername(r.getInt("id"),
				r.getString("username"));
	}
}