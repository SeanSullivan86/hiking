package org.sean.hiking;

public class WrappedResponse<T> {

	private boolean success;
	private String message;
	
	private T response;
	
	public WrappedResponse() { }
	
	public static <T> WrappedResponse<T> failure(String message) {
		return new WrappedResponse<T>(false, message, null);
	}
	
	public static <T> WrappedResponse<T> success(T response) {
		return new WrappedResponse<T>(true, null, response);
	}

	public WrappedResponse(boolean success, String message, T response) {
		this.success = success;
		this.message = message;
		this.response = response;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getResponse() {
		return response;
	}

	public void setResponse(T response) {
		this.response = response;
	}
	
	
	
}
