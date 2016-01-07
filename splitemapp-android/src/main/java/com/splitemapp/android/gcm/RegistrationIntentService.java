	package com.splitemapp.android.gcm;


import java.sql.SQLException;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.task.PushUserSessionsTask;
import com.splitemapp.commons.constants.ServiceConstants;

public class RegistrationIntentService extends IntentService {

	public DatabaseHelper databaseHelper = null;

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

	private static final String TAG = RegistrationIntentService.class.getSimpleName();

	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			// [START register_for_gcm]
			// Initially this call goes out to the network to retrieve the token, subsequent calls
			// are local.
			// R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
			// See https://developers.google.com/cloud-messaging/android/start for details on this file.
			// [START get_token]
			InstanceID instanceID = InstanceID.getInstance(this);
			String token = instanceID.getToken(ServiceConstants.GCM_DEFAULT_SENDER_ID,
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			// [END get_token]
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
		new PushUserSessionsTask(getHelper()){
			@Override
			public void executeOnSuccess() {
				Log.i(TAG, "GCM token registration success!");
			}
			@Override
			public void executeOnFail() {
				Log.e(TAG, "Failed to register GCM token!");
			}}.execute(); 
	}

}
