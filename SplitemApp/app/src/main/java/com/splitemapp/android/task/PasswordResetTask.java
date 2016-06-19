package com.splitemapp.android.task;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.dto.request.PasswordResetRequest;
import com.splitemapp.commons.domain.dto.response.PasswordResetResponse;

/**
 * Password reset task that enables the user to request a password reset email
 * @author nicolas
 *
 */
public abstract class PasswordResetTask extends BaseAsyncTask<Void, Void, PasswordResetResponse> {

	String email;

	public PasswordResetTask(DatabaseHelper databaseHelper, String email){
		super(databaseHelper);
		this.email = email;
	}

	private String getLoggingTag(){
		return getClass().getSimpleName();
	}

	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	@Override
	public PasswordResetResponse doInBackground(Void... params) {
		try {
			// Creating the login request
			PasswordResetRequest request = new PasswordResetRequest();
			request.setUsername(email);

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.PASSWORD_RESET_PATH, request, PasswordResetResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void onPostExecute(PasswordResetResponse response) {

		// Validating the response
		if(response == null){
			executeOnFail(ServiceConstants.ERROR_MESSAGE_NETWORK_ERROR);
			return;
		}

		// We always show the sent message for security reasons
		executeOnSuccess();
	}
}
