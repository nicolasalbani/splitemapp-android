package com.splitemapp.android.service.gcm;


import java.sql.SQLException;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.splitemapp.android.service.BaseIntentService;
import com.splitemapp.commons.constants.Action;
import com.splitemapp.commons.constants.ServiceConstants;

public class RegistrationIntentService extends BaseIntentService {

	private static final String TAG = RegistrationIntentService.class.getSimpleName();

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
