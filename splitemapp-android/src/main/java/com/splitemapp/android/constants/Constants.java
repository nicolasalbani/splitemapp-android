package com.splitemapp.android.constants;

import android.os.Build;

public class Constants {

	// Device information
	public static final String DEVICE=Build.MANUFACTURER+" "+Build.MODEL;
	public static final String OS_VERSION="Android " +Build.VERSION.CODENAME+"-"+Build.VERSION.RELEASE+ " SDK-" +Build.VERSION.SDK_INT;
	public static final String LOOPBACK_ADDRESS = "127.0.0.1";
	
	// Backend constants
	public static final String BACKEND_HOST="135.20.205.106";
	public static final String BACKEND_PORT="8080";
	public static final String BACKEND_PATH="splitemapp-backend-rest";
	
	// Request codes
	public static final int SELECT_PICTURE_REQUEST_CODE = 1;
	public static final int CROP_PICTURE_REQUEST_CODE = 2;
}
