package com.splitemapp.android.constants;

import android.os.Build;

public class Constants {
	
	// Application information
	public static final String APPLICATION_NAME = "SplitemApp";

	// Device information
	public static final String DEVICE=Build.MANUFACTURER+" "+Build.MODEL;
	public static final String OS_VERSION="Android " +Build.VERSION.CODENAME+"-"+Build.VERSION.RELEASE+ " SDK-" +Build.VERSION.SDK_INT;
	public static final String LOOPBACK_ADDRESS = "127.0.0.1";
	
	// Version threshold information for customizing layouts
	public static final int SDK_VERSION_THRESHOLD = 20;
	
	// Backend constants
	public static final String BACKEND_HOST="ec2-52-27-108-212.us-west-2.compute.amazonaws.com";
	public static final String BACKEND_PORT="8080";
	public static final String BACKEND_PATH="splitemapp-backend-rest";
	
	// Request codes
	public static final int SELECT_PICTURE_REQUEST_CODE = 1;
	public static final int CROP_PICTURE_REQUEST_CODE = 2;
	
	// Parameters
	public static final String IMAGE_WIDTH = "IMAGE_WIDTH";
	public static final String IMAGE_HEIGHT = "IMAGE_HEIGTH";
}
