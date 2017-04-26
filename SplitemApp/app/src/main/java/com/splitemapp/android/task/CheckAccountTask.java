package com.splitemapp.android.task;

import android.util.Log;

import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.request.CheckAccountRequest;
import com.splitemapp.commons.domain.dto.request.LoginRequest;
import com.splitemapp.commons.domain.dto.response.CheckAccountResponse;
import com.splitemapp.commons.domain.dto.response.LoginResponse;
import com.splitemapp.commons.utils.Utils;

import java.sql.SQLException;

/**
 * Login tasks which enables the user to login for the first time and authenticate against the remote server
 * @author nicolas
 *
 */
public abstract class CheckAccountTask extends BaseAsyncTask<Void, Void, CheckAccountResponse> {
	private String userName;

	public CheckAccountTask(DatabaseHelper databaseHelper, String userName){
		super(databaseHelper);

		this.userName = userName;
	}

	private String getLoggingTag(){
		return getClass().getSimpleName();
	}
	
	@Override
	public CheckAccountResponse doInBackground(Void... params) {
		try {
			// Creating the login request
			CheckAccountRequest checkAccountRequest = new CheckAccountRequest();
			checkAccountRequest.setUsername(userName);

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.CHECK_ACCOUNT_PATH, checkAccountRequest, CheckAccountResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

}
