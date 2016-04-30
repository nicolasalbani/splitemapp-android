package com.splitemapp.android.task;

import java.sql.SQLException;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.dto.request.ChangePasswordRequest;
import com.splitemapp.commons.domain.dto.response.ServiceResponse;

/**
 * Change password task which enables the user to change the current password
 * @author nicolas
 *
 */
public abstract class ChangePasswordTask extends BaseAsyncTask<Void, Void, ServiceResponse> {
	
	private String currentPassword;
	private String newPassword;

	public ChangePasswordTask(DatabaseHelper databaseHelper, String currentPassword, String newPassword){
		super(databaseHelper);
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
	}

	private String getLoggingTag(){
		return getClass().getSimpleName();
	}
	
	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	@Override
	public ServiceResponse doInBackground(Void... params) {
		try {
			// Creating the change password request
			ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
			changePasswordRequest.setToken(databaseHelper.getCurrentUserSession().getToken());
			changePasswordRequest.setCurrentPassword(currentPassword);
			changePasswordRequest.setNewPassword(newPassword);

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.CHANGE_PASSWORD_PATH, changePasswordRequest, ServiceResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void onPostExecute(ServiceResponse response) {
		boolean success = false;

		// Validating the response
		if(response != null){
			success = response.getSuccess();
		} else {
			executeOnFail(ServiceConstants.ERROR_MESSAGE_NETWORK_ERROR);
			return;
		}

		// We show the status toast if it failed
		if(!success){
			executeOnFail(response.getMessage());
		} else {
			try {
				// Clearing all tables in database
				User user = databaseHelper.getUser(databaseHelper.getCurrentUserSession().getUser().getId());
				user.setPassword(newPassword);
				databaseHelper.updateUser(user);
				
				// Executing code on success
				executeOnSuccess();
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while clearing the database", e);
			}
		}
	}
}
