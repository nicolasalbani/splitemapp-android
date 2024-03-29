package com.splitemapp.android.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;

public abstract class BaseTask {
	
	public static final String TASK_NAME = "TASK_NAME";
	public static final String PROJECT_ID_EXTRA = "PROJECT_ID_EXTRA";
	public static final String EXPENSE_ID_EXTRA = "EXPENSE_ID_EXTRA";
	public static final String USER_ID_ARRAY_EXTRA = "USER_ID_ARRAY_EXTRA";
	public static final String START_ANIMATION = "start_animation";
	public static final String STOP_ANIMATION = "stop_animation";
	public static final String EXPENSES_PUSHED = "expenses_pushed";
	public static final String NETWORK_ERROR = "network_error";
	public static final String GENERIC_ERROR = "generic_error";
	
	private DatabaseHelper databaseHelper = null;
	private Context context = null;

	public BaseTask(Context context) {
		this.context = context;
	}

	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
	}

	/**
	 * This method calls the OpenHelperManager getHelper static method with the proper DatabaseHelper class reference 
	 * @return DatabaseHelper object which offers DAO for every domain entity
	 */
	public DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		}
		return databaseHelper;
	}
	
	/**
	 * This method sends a broadcast message for the listening fragment to process
	 * @param message
	 */
	public void broadcastMessage(String message, String messageType){
		// Sending the action to the listening fragment
		Intent intent = new Intent(messageType);
		intent.putExtra(ServiceConstants.CONTENT_RESPONSE, message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		
		Log.d("BroadcastSender", "Sending " +messageType+ ": " +message);
	}
	
	/**
	 * Returns the context for this task
	 * @return
	 */
	public Context getContext(){
		return this.context;
	}
	
	/**
	 * Executes a required action on success. This code executes after the processResult method.
	 */
	public void executeOnSuccess(){};

	/**
	 * Executes a required action on fail. This code executes after the processResult method.
	 */
	public void executeOnFail(){};
	
	/**
	 * This method contains the tasks to be executed on the service
	 * @param intent
	 * @throws Exception
	 */
	public abstract void executeService(Intent intent) throws Exception;
}
