package com.splitemapp.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;

public abstract class BaseIntentService extends IntentService {

	public BaseIntentService(String name) {
		super(name);
	}

	private DatabaseHelper databaseHelper = null;
	private LocalBroadcastManager broadcaster = null;

	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
	}

	/**
	 * This method calls the OpenHelperManager getHelper static method with the proper DatabaseHelper class reference 
	 * @return DatabaseHelper object which offers DAO for every domain entity
	 */
	public DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getApplicationContext(), DatabaseHelper.class);
		}
		return databaseHelper;
	}
	
	/**
	 * This method sends a broadcast message for the listening fragment to process
	 * @param response
	 */
	public void broadcastMessage(String response){
		// Sending the action to the listening fragment
		Intent intent = new Intent(ServiceConstants.REST_MESSAGE);
		intent.putExtra(ServiceConstants.CONTENT_RESPONSE, response);
		broadcaster.sendBroadcast(intent);
	}
	
	/**
	 * Executes a required action on start.
	 */
	public void executeOnStart(){};
	
	/**
	 * Executes a required action on success. This code executes after the processResult method.
	 */
	public void executeOnSuccess(){};

	/**
	 * Executes a required action on fail. This code executes after the processResult method.
	 */
	public void executeOnFail(){};
	
}
