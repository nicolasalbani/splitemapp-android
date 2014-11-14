package com.splitemapp.android.constants;

import android.os.Build;

public class Constants {
	// Device information
	public static final String DEVICE=Build.MANUFACTURER+" "+Build.MODEL;
	
	// Backend constants
	public static final String BACKEND_HOST="192.168.1.30";
	public static final String BACKEND_PORT="8080";
	public static final String BACKEND_PATH="splitemapp-service-backend-rest";
	
	// Service name constants
	public static final String LOGIN_SERVICE="login";
}
