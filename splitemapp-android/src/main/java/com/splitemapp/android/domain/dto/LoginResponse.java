package com.splitemapp.android.domain.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "loginResponse")
public class LoginResponse {

	private Boolean success;
	private String sessionToken;
	private Boolean changePassword;
	
	public LoginResponse() {}
	
	public LoginResponse(Boolean success,String sessionToken,Boolean changePassword) {
		this.setSuccess(success);
		this.sessionToken = sessionToken;
		this.changePassword	= changePassword;	
	}

	public String getSessionToken() {
		return sessionToken;
	}

	public void setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

	public Boolean getChangePassword() {
		return changePassword;
	}

	public void setChangePassword(Boolean changePassword) {
		this.changePassword = changePassword;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
