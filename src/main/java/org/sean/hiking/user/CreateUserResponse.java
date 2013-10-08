package org.sean.hiking.user;

public class CreateUserResponse {

	private int id;
	private String username;
	private String hash;
	private int success;
	private String message;
	
	public CreateUserResponse() { }
	
	public static CreateUserResponse failure(String message) {
		return new CreateUserResponse(0,null,null,0, message);
	}

	public CreateUserResponse(int id, String username, String hash,
			int success, String message) {
		this.id = id;
		this.username = username;
		this.hash = hash;
		this.success = success;
		this.message = message;
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

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
	
	
}
