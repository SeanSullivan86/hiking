package org.sean.hiking.user;

public class User {

	private int id;
	private String username;
	private String email;
	private String salt;
	private String hash;
	
	public User() { }
	
	public User(int id, String username, String email, String salt, String hash) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.salt = salt;
		this.hash = hash;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
	
}
