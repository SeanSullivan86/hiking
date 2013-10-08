package org.sean.hiking;

public class Credentials {
	private int id;
	private String username;
	private String hash;
	
	public Credentials() { }

	public Credentials(int id, String username, String hash) {
		this.id = id;
		this.username = username;
		this.hash = hash;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	

}
