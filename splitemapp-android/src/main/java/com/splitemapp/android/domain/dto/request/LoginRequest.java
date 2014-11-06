package com.splitemapp.android.domain.dto.request;



public class LoginRequest {

	private String username;
	private String password;
	private String device;
	
	/**
	 * Required by RestEasy.
	 */
	public LoginRequest() {}
	
	/**
	 * <p>Constructor for LoginRequest.</p>
	 *
	 * @param username a {@link java.lang.String} object.
	 * @param device a {@link java.lang.String} object.
	 * @param password a {@link java.lang.String} object.
	 */
	public LoginRequest(String username, String password, String device) {

		this.username      	= username;
		this.password     	= password;
		this.device     	= device;
	}

	/**
	 * <p>Getter for the field <code>username</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * <p>Setter for the field <code>username</code>.</p>
	 *
	 * @param username a {@link java.lang.String} object.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * <p>Getter for the field <code>device</code>.</p>
	 *
	 * @return the device
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * <p>Setter for the field <code>device</code>.</p>
	 *
	 * @param device the device to set
	 */
	public void setDevice(String device) {
		this.device = device;
	}

	/**
	 * <p>Getter for the field <code>password</code>.</p>
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * <p>Setter for the field <code>password</code>.</p>
	 *
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
