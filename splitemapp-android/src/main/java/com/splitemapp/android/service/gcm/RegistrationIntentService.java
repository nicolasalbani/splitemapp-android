package com.splitemapp.android.service.gcm;


import java.sql.SQLException;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.Action;
import com.splitemapp.commons.constants.ServiceConstants;

public class RegistrationIntentService extends IntentService {

	private static final String TAG = RegistrationIntentService.class.getSimpleName();
	private DatabaseHelper databaseHelper = null;


	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
	}

	/**
	 * This method calls the OpenHelperManager getHelper static method with the proper DatabaseHelper class reference 
	 * @return DatabaseHelper object which offers DAO for every domain entity
	 */
	public DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
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
		intent.putExtra(ServiceConstants.CONTENT_ACTION, response);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	
	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			InstanceID instanceID = InstanceID.getInstance(this);
			String token = instanceID.getToken(ServiceConstants.GCM_DEFAULT_SENDER_ID,
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

			Log.i(TAG, "GCM Registration Token: " + token);

			// Saves GCM token to DB and registers it to remote server
			saveAndRegisterToken(token);

			// [END register_for_gcm]
		} catch (Exception e) {
			Log.e(TAG, "Failed to complete token refresh", e);
		}
	}

	/**
	 * Persist registration to third-party servers.
	 *
	 * Modify this method to associate the user's GCM registration token with any server-side account
	 * maintained by your application.
	 *
	 * @param gcmToken The new token.
	 * @throws SQLException 
	 */
	private void saveAndRegisterToken(String gcmToken) throws SQLException {
		// Saving the GCM token into the database
		getHelper().setGcmToken(gcmToken);

		// Registering the GCM token to the remote server
		broadcastMessage(Action.REGISTER_GCM);
	}
}
