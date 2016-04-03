package com.splitemapp.android.task;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.dto.request.InviteRequest;
import com.splitemapp.commons.domain.dto.response.ServiceResponse;

/**
 * Questions tasks which enables the user to send a question to the remote server
 * @author nicolas
 *
 */
public abstract class InviteTask extends BaseAsyncTask<Void, Void, ServiceResponse> {

	String email;
	
	public InviteTask(DatabaseHelper databaseHelper, String email){
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
	public ServiceResponse doInBackground(Void... params) {
		try {
			// Creating the login request
			InviteRequest request = new InviteRequest();
			request.setToken(databaseHelper.getCurrentUserSession().getToken());
			request.setEmail(email);

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.INVITE_PATH, request, ServiceResponse.class);
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
			// Executing code on success
			executeOnSuccess();
		}
	}
}
