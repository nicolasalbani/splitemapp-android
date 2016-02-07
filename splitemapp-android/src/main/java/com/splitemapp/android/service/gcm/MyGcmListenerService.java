package com.splitemapp.android.service.gcm;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.android.utils.PreferencesManager;
import com.splitemapp.commons.constants.Action;
import com.splitemapp.commons.constants.ServiceConstants;

public class MyGcmListenerService extends GcmListenerService {

	private static int notificationCount = 0;
	private static final String TAG = MyGcmListenerService.class.getSimpleName();
	private LocalBroadcastManager broadcaster = null;
	private PreferencesManager preferencesManager = null;

	@Override
	public void onCreate() {
		super.onCreate();
		broadcaster = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void attachBaseContext(Context base) {
		preferencesManager = new PreferencesManager(base);
		super.attachBaseContext(base);
	}

	/**
	 * Called when message is received.
	 *
	 * @param from SenderID of the sender.
	 * @param data Data bundle containing message data as key/value pairs.
	 *             For Set of keys use data.keySet().
	 */
	@Override
	public void onMessageReceived(String from, Bundle data) {
		String sender = data.getString("sender");
		String action = data.getString("action");
		String projectName = data.getString("projectName");
		String projectId = data.getString("projectId");

		// Logging
		Log.d(TAG, "sender: " + sender);
		Log.d(TAG, "action: " + action);
		Log.d(TAG, "projectName: " + projectName);
		Log.d(TAG, "projectId: " + projectId);

		// Showing notification if the proper setting is enabled
		boolean notifyNewProject = preferencesManager.getBoolean(PreferencesManager.NOTIFY_NEW_PROJECT);
		boolean notifyNewExpense = preferencesManager.getBoolean(PreferencesManager.NOTIFY_NEW_EXPENSE);
		boolean notifyUpdatedProjectCover = preferencesManager.getBoolean(PreferencesManager.NOTIFY_UPDATED_PROJECT_COVER);

		if((action.equals(Action.ADD_USER_TO_PROJECT) && notifyNewProject) || 
				action.equals(Action.ADD_USER_EXPENSE) && notifyNewExpense || 
				action.equals(Action.UPDATE_PROJECT_COVER_IMAGE) && notifyUpdatedProjectCover){
			// Crafting message
			StringBuilder message = new StringBuilder();
			message.append(sender+ " ");
			if(action.equals(Action.ADD_USER_TO_PROJECT)){
				message.append(getResources().getString(R.string.notification_new_project));
			} else if (action.equals(Action.ADD_USER_EXPENSE)){
				message.append(getResources().getString(R.string.notification_new_expense));
			} else if (action.equals(Action.UPDATE_PROJECT_COVER_IMAGE)){
				message.append(getResources().getString(R.string.notification_updated_project_cover));
			}
			message.append(" " +projectName);

			// Showing notification
			showNotification(message.toString());
		}


		// Sending the action to the listening fragment
		Intent intent = new Intent(ServiceConstants.GCM_MESSAGE);
		intent.putExtra(ServiceConstants.CONTENT_ACTION, action);
		intent.putExtra(ServiceConstants.PROJECT_ID, projectId);
		broadcaster.sendBroadcast(intent);
	}

	/**
	 * Create and show a simple notification containing the received GCM message.
	 *
	 * @param message GCM message received.
	 */
	private void showNotification(String message) {
		Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(android.R.drawable.ic_popup_sync)
		.setContentTitle(Constants.APPLICATION_NAME)
		.setContentText(message)
		.setAutoCancel(true)
		.setSound(defaultSoundUri)
		.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(notificationCount++, notificationBuilder.build());
	}
}
