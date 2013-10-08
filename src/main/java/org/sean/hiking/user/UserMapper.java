package org.sean.hiking.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class UserMapper implements ResultSetMapper<User> {
	public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new User(r.getInt("id"),
				r.getString("username"),
				r.getString("email"),
				r.getString("salt"),
				r.getString("hash"));
	}
}