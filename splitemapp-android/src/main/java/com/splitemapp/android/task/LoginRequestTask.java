package com.splitemapp.android.task;

import java.sql.SQLException;

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
import com.splitemapp.commons.domain.dto.request.LoginRequest;
import com.splitemapp.commons.domain.dto.response.LoginResponse;
import com.splitemapp.commons.utils.Utils;

/**
 * Login tasks which enables the user to login for the first time and authenticate against the remote server
 * @author nicolas
 *
 */
public abstract class LoginRequestTask extends BaseAsyncTask<Void, Void, LoginResponse> {
	private String userName;
	private String password;

	public LoginRequestTask(DatabaseHelper databaseHelper, String userName, String password){
		super(databaseHelper);
		
		this.userName = userName;
		this.password = password;
	}

	private String getLoggingTag(){
		return getClass().getSimpleName();
	}
	
	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	@Override
	public LoginResponse doInBackground(Void... params) {
		try {
			// Creating the login request
			LoginRequest loginRequest = new LoginRequest();
			loginRequest.setDevice(Constants.DEVICE);
			loginRequest.setOsVersion(Constants.OS_VERSION);
			loginRequest.setUsername(userName);
			loginRequest.setPassword(Utils.hashPassword(password));

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.LOGIN_PATH, loginRequest, LoginResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void onPostExecute(LoginResponse loginResponse) {
		boolean success = false;

		// Validating the response
		if(loginResponse != null){
			success = loginResponse.getSuccess();
		}

		// We show the status toast if it failed
		if(!success){
			executeOnFail();
		} else {
			// Saving the information returned by the back-end
			try {
				// Reconstructing the user status object
				UserStatus userStatus = new UserStatus(loginResponse.getUserStatusDTO());

				// Reconstructing the user object
				User user = new User(userStatus, loginResponse.getUserDTO());
				databaseHelper.createOrUpdateUser(user);

				// Clearing all previous session records
				databaseHelper.deleteAllUserSessions();

				// Reconstructing the user session object
				UserSession userSession = new UserSession(user, loginResponse.getUserSessionDTO());
				databaseHelper.createOrUpdateUserSession(userSession);

				// Reconstructing the user contact data object
				UserContactDataDTO userContactDataDTO = loginResponse.getUserContactDataDTO();
				User ucdUpdatedBy = databaseHelper.getUser(userContactDataDTO.getUpdatedBy().longValue());
				User ucdPushedBy = databaseHelper.getUser(userContactDataDTO.getPushedBy().longValue());
				UserContactData userContactData = new UserContactData(user,ucdUpdatedBy,ucdPushedBy,userContactDataDTO);
				// Replacing user contact data if email already exists
				UserContactData existingUserContactData = databaseHelper.getUserContactData(userContactData.getContactData());
				if(existingUserContactData != null){
					userContactData.setId(existingUserContactData.getId());
				}
				databaseHelper.createOrUpdateUserContactData(userContactData);

				// Reconstructing the user avatar object
				UserAvatarDTO userAvatarDTO = loginResponse.getUserAvatarDTO();
				User uaUpdatedBy = databaseHelper.getUser(userAvatarDTO.getUpdatedBy().longValue());
				User uaPushedBy = databaseHelper.getUser(userAvatarDTO.getPushedBy().longValue());
				UserAvatar userAvatar = new UserAvatar(user,uaUpdatedBy,uaPushedBy,userAvatarDTO);
				// Replacing user avatar if it already exists
				UserAvatar existingUserAvatar = databaseHelper.getUserAvatarByUserId(user.getId());
				if(existingUserAvatar != null){
					userAvatar.setId(existingUserAvatar.getId());
				}
				databaseHelper.createOrUpdateUserAvatar(userAvatar);

				// Opening the home activity class
				executeOnSuccess();
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
			}
		}
	}
}
