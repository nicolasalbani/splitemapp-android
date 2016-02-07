package com.splitemapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PreferencesManager {

	private static final String SETTINGS_NAME = "SplitemappSettings";
	private static final String SETTINGS_INITIALIZED = "SETTINGS_INITIALIZED";

	public static final String NOTIFY_NEW_PROJECT = "NOTIFY_NEW_PROJECT";
	public static final String NOTIFY_NEW_EXPENSE = "NOTIFY_NEW_EXPENSE";
	public static final String NOTIFY_UPDATED_PROJECT_COVER = "NOTIFY_UPDATED_PROJECT_COVER";

	private SharedPreferences settings;

	public PreferencesManager(Context context){
		settings = context.getSharedPreferences(SETTINGS_NAME, 0);

		// Initializing settings if required
		initializeSettings();
	}

	private void initializeSettings(){
		if(!getBoolean(SETTINGS_INITIALIZED)){
			// Updating initialized status
			setBoolean(SETTINGS_INITIALIZED, true);

			// Initializing settings 
			setBoolean(NOTIFY_NEW_EXPENSE, true);
			setBoolean(NOTIFY_NEW_PROJECT, true);
			setBoolean(NOTIFY_UPDATED_PROJECT_COVER, true);
		}
	}

	/**
	 * Sets the boolean setting identified by settingName
	 * @param settingName
	 * @param value
	 */
	public void setBoolean(String settingName, boolean value){
		Editor editor = settings.edit();
		editor.putBoolean(settingName, value);
		editor.commit();
	}

	/**
	 * Gets the boolean setting identified by settingName 
	 * @param settingName
	 * @return
	 */
	public boolean getBoolean(String settingName){
		// Returning false by default
		if(settingName == null || settingName.isEmpty()){
			return false;
		}
		return settings.getBoolean(settingName, false);
	}

}
