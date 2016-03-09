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
	
	public static final String SHOW_ACTIVE_PROJECTS = "SHOW_ACTIVE_PROJECTS";
	public static final String SHOW_ARCHIVED_PROJECTS = "SHOW_ARCHIVED_PROJECTS";
	public static final String SHOW_MONTHLY_PROJECTS = "SHOW_MONTHLY_PROJECTS";
	public static final String SHOW_ONE_TIME_PROJECTS = "SHOW_ONE_TIME_PROJECTS";
	
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

			// Initializing notification settings 
			setBoolean(NOTIFY_NEW_EXPENSE, true);
			setBoolean(NOTIFY_NEW_PROJECT, true);
			setBoolean(NOTIFY_UPDATED_PROJECT_COVER, true);
			
			// Initializing project filter settings
			setBoolean(SHOW_ACTIVE_PROJECTS, true);
			setBoolean(SHOW_ARCHIVED_PROJECTS, false);
			setBoolean(SHOW_MONTHLY_PROJECTS, true);
			setBoolean(SHOW_ONE_TIME_PROJECTS, true);
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
