package org.sean.hiking.user;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

public interface UserDao {

	@SqlUpdate("insert into users (username, email, salt, hash) VALUES (:username, :email, :salt, :hash)")
	void insert(@Bind("username") String username, @Bind("email") String email, @Bind("salt") String salt, @Bind("hash") String hash);
	
	@RegisterMapper(UserMapper.class)
	@SqlQuery("select * from users where id = :id")
	User findById(@Bind("id") int id);
	
	@RegisterMapper(UserMapper.class)
	@SqlQuery("select * from users where username = :username")
	User findByUsername(@Bind("username") String username);
	
	@RegisterMapper(UserMapper.class)
	@SqlQuery("select * from users where email = :email")
	User findByEmail(@Bind("email") String email);
	
	@RegisterMapper(UserAndUsernameMapper.class)
	@SqlQuery("select id, username from users")
	List<UserAndUsername> findAllUsers();
	
	@SqlQuery("SELECT LAST_INSERT_ID()")
	int lastInsertId();
}
