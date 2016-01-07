package com.splitemapp.android.gcm;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.commons.constants.Action;

public class MyGcmListenerService extends GcmListenerService {

	private static int notificationCount = 0;
	private static final String TAG = MyGcmListenerService.class.getSimpleName();

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
		String details = data.getString("details");

		// Logging
		Log.d(TAG, "sender: " + sender);
		Log.d(TAG, "action: " + action);
		Log.d(TAG, "details: " + details);

		// Showing notification
		StringBuilder message = new StringBuilder();
		message.append(sender);
		message.append(" has " +action);
		message.append(" " +details);

		showNotification(message.toString());

		// Calling the proper pull task based on the action
		if(action.equals(Action.ADD_PROJECT) || action.equals(Action.UPDATE_PROJECT)){

		}
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
