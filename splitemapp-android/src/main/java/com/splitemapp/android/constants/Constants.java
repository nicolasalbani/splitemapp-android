package com.splitemapp.android.constants;

import android.os.Build;

public class Constants {

	// Avatar image size
	public static final int AVATAR_WIDTH = 1000;
	public static final int AVATAR_HEIGHT = 1000;
	
	// Device information
	public static final String DEVICE=Build.MANUFACTURER+" "+Build.MODEL;
	public static final String OS_VERSION="Android " +Build.VERSION.CODENAME+"-"+Build.VERSION.RELEASE+ " SDK-" +Build.VERSION.SDK_INT;
	public static final String LOOPBACK_ADDRESS = "127.0.0.1";
	
	// Backend constants
	public static final String BACKEND_HOST="192.168.43.69";
	public static final String BACKEND_PORT="8080";
	public static final String BACKEND_PATH="splitemapp-backend-rest";
	
	// Extra parameters to be sent through intents
	public static final String EXTRA_PROJECT_ID = "com.splitemapp.android.project_id";
	public static final String EXTRA_EXPENSE_ID = "com.splitemapp.android.expense_id";
}
