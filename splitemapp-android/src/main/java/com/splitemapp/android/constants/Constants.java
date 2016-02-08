package com.splitemapp.android.constants;

import android.os.Build;

public class Constants {
	
	// Application information
	public static final String APPLICATION_NAME = "SplitemApp";

	// Device information
	public static final String DEVICE=Build.MANUFACTURER+" "+Build.MODEL;
	public static final String OS_VERSION="Android " +Build.VERSION.CODENAME+"-"+Build.VERSION.RELEASE+ " SDK-" +Build.VERSION.SDK_INT;
	public static final String LOOPBACK_ADDRESS = "127.0.0.1";
	
	// Backend constants
	public static final String BACKEND_HOST="192.168.0.3";
	public static final String BACKEND_PORT="8080";
	public static final String BACKEND_PATH="splitemapp-backend-rest";
	
	// Decimal number entry
	public static final int MAX_DIGITS_BEFORE_DECIMAL = 5;
	public static final int MAX_DIGITS_AFTER_DECIMAL = 2;
	
	// Request codes
	public static final int SELECT_PICTURE_REQUEST_CODE = 1;
	public static final int CROP_PICTURE_REQUEST_CODE = 2;
	
	// Parameters
	public static final String IMAGE_WIDTH = "IMAGE_WIDTH";
	public static final String IMAGE_HEIGHT = "IMAGE_HEIGTH";
}
