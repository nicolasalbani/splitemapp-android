package com.splitemapp.android.domain.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "loginRequest")
public class LoginRequest {

	private String username;
	private String password;
	private String device;
	
	/**
	 * Required by FasterXML.
	 */
	public LoginRequest() {}
	
	public LoginRequest(String username, String password, String device) {
		this.username      	= username;
		this.password     	= password;
		this.device     	= device;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
