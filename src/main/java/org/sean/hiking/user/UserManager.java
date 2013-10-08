package org.sean.hiking.user;

import java.util.UUID;

import org.sean.hiking.APIUtils;
import org.sean.hiking.Credentials;
import org.sean.hiking.WrappedResponse;

import com.google.common.base.Optional;

public class UserManager {
	
	private final UserDao userDao;
	
	public UserManager(UserDao userDao) {
		this.userDao = userDao;
	}
		
	public Optional<User> getUserFromAuthString(String auth) {
		if (auth == null) return Optional.absent();
		if ("0".equals(auth)) return Optional.absent();
		
		String[] parts = auth.split(" ", -1);
		
		if (parts.length != 2) return Optional.absent();
		
		int userId = 0;
		
		try {
			userId = Integer.parseInt(parts[0]);
		} catch (NumberFormatException e) {
			return Optional.absent();
		}
		
		String hash = parts[1];
		
		if (hash == null || hash.length() != 40) return Optional.absent();
		
		User user = this.userDao.findById(userId);
		
		if (user != null && user.getHash().equals(hash)) {
			return Optional.of(user);
		} else {
			return Optional.absent();
		}
	}
	
	public WrappedResponse<Credentials> createUser(String username, String email, String password) {
		
		if (username == null || username.isEmpty()) return WrappedResponse.failure("Username must not be blank");
		if (email == null || email.isEmpty()) return WrappedResponse.failure("Email must not be blank");
		if (password == null || password.isEmpty()) return WrappedResponse.failure("Password must not be blank");
		
		if (this.userDao.findByEmail(email) != null) {
			return WrappedResponse.failure("Error : An account already exists for this email address");
		}
		
		if (this.userDao.findByUsername(username) != null) {
			return WrappedResponse.failure("Error : An account already exists with the same username");
		}
		
		String salt = APIUtils.sha1(UUID.randomUUID().toString());
		String hash = APIUtils.sha1(password+salt);
		
		this.userDao.insert(username, email, salt, hash);
		
		int id = this.userDao.lastInsertId();
		
		return WrappedResponse.success(new Credentials(id, username, hash));
	}
	
	public WrappedResponse<Credentials> getCredentials(String identifier, String password) {
		if (identifier == null || identifier.isEmpty()) return WrappedResponse.failure("Account must not be blank");
		if (password == null || password.isEmpty()) return WrappedResponse.failure("Password must not be blank");
		
		User user = this.userDao.findByEmail(identifier);
		if (user == null) {
			user = this.userDao.findByUsername(identifier);
		}
		
		if (user == null) {
			return WrappedResponse.failure("Cannot find account with that username or email");
		}
		
		if (APIUtils.sha1(password + user.getSalt()).equals(user.getHash())) {
			return WrappedResponse.success(new Credentials(user.getId(), user.getUsername(), user.getHash()));
		}
		
		return WrappedResponse.failure("Incorrect password.");
	}
	
	public boolean isValidUser(Credentials credentials) {
		if (credentials == null) return false;
		
		User user = this.userDao.findById(credentials.getId());
		
		return user != null && user.getHash().equals(credentials.getHash());
	}

}
