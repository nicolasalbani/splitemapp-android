package com.splitemapp.android.task;

import java.sql.SQLException;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.dto.request.LogoutRequest;
import com.splitemapp.commons.domain.dto.response.ServiceResponse;

/**
 * Login tasks which enables the user to login for the first time and authenticate against the remote server
 * @author nicolas
 *
 */
public abstract class LogoutRequestTask extends BaseAsyncTask<Void, Void, ServiceResponse> {

	public LogoutRequestTask(DatabaseHelper databaseHelper){
		super(databaseHelper);
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
			// Creating the login request
			LogoutRequest logoutRequest = new LogoutRequest();
			logoutRequest.setToken(databaseHelper.getCurrentUserSession().getToken());

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.LOGOUT_PATH, logoutRequest, ServiceResponse.class);
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
				databaseHelper.clearDatabase();
				
				// Executing code on success
				executeOnSuccess();
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while clearing the database", e);
			}
		}
	}
}
