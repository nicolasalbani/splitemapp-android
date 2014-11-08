package com.splitemapp.android.domain.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "loginResponse")
public class LoginResponse {

	/** 
	 * The session token response. This is necessary because User / UserSession operations may change the session!
	 */
	private Long userId;
	private String username;
	private String sessionToken;
	private Boolean isNewDevice;
	private String clientEvent;
	private String firstName;
	private String lastName;
	private Boolean changePassword;
	
	/**
	 * Required by RestEasy.
	 */
	public LoginResponse() {}
	
	/**
	 * <p>Constructor for LoginResponse.</p>
	 *
	 * @param sessionToken a {@link java.lang.String} object.
	 * @param userId a {@link java.lang.Long} object.
	 * @param username a {@link java.lang.String} object.
	 * @param isNewDevice a {@link java.lang.Boolean} object.
	 * @param clientEvent a {@link java.lang.String} object.
	 * @param firstName a {@link java.lang.String} object.
	 * @param lastName a {@link java.lang.String} object.
	 * @param changePassword a {@link java.lang.Boolean} object.
	 */
	public LoginResponse(Long userId,String username,String sessionToken,Boolean isNewDevice,String clientEvent,String firstName,String lastName,Boolean changePassword) {
		this.userId			= userId;
		this.username		= username;
		this.sessionToken	= sessionToken;
		this.isNewDevice	= isNewDevice;
		this.clientEvent	= clientEvent;
		this.firstName		= firstName;
		this.lastName		= lastName;
		this.changePassword	= changePassword;	
	}

	/**
	 * <p>Getter for the field <code>sessionToken</code>.</p>
	 *
	 * @return the sessionToken
	 */
	public String getSessionToken() {
		return sessionToken;
	}

	/**
	 * <p>Setter for the field <code>sessionToken</code>.</p>
	 *
	 * @param sessionToken the sessionToken to set
	 */
	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

	/**
	 * <p>Getter for the field <code>isNewDevice</code>.</p>
	 *
	 * @return the isNewDevice
	 */
	public Boolean getIsNewDevice() {
		return isNewDevice;
	}

	/**
	 * <p>Setter for the field <code>isNewDevice</code>.</p>
	 *
	 * @param isNewDevice the isNewDevice to set
	 */
	public void setIsNewDevice(Boolean isNewDevice) {
		this.isNewDevice = isNewDevice;
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
	 * <p>Getter for the field <code>firstName</code>.</p>
	 *
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * <p>Setter for the field <code>firstName</code>.</p>
	 *
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * <p>Getter for the field <code>lastName</code>.</p>
	 *
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * <p>Setter for the field <code>lastName</code>.</p>
	 *
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * <p>Getter for the field <code>userId</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * <p>Setter for the field <code>userId</code>.</p>
	 *
	 * @param userId a {@link java.lang.Long} object.
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * <p>Getter for the field <code>clientEvent</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getClientEvent() {
		return clientEvent;
	}

	/**
	 * <p>Setter for the field <code>clientEvent</code>.</p>
	 *
	 * @param clientEvent a {@link java.lang.String} object.
	 */
	public void setClientEvent(String clientEvent) {
		this.clientEvent = clientEvent;
	}

	/**
	 * <p>Getter for the field <code>changePassword</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getChangePassword() {
		return changePassword;
	}

	/**
	 * <p>Setter for the field <code>changePassword</code>.</p>
	 *
	 * @param changePassword a {@link java.lang.Boolean} object.
	 */
	public void setChangePassword(Boolean changePassword) {
		this.changePassword = changePassword;
	}
}
