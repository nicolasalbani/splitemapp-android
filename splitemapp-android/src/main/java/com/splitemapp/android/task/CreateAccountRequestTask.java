package com.splitemapp.android.task;

import java.sql.SQLException;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.request.CreateAccountRequest;
import com.splitemapp.commons.domain.dto.response.CreateAccountResponse;
import com.splitemapp.commons.utils.Utils;

/**
 * Create account task which creates a new account for the user
 * @author nicolas
 *
 */
public abstract class CreateAccountRequestTask extends BaseAsyncTask<Void, Void, CreateAccountResponse> {
	private String email;
	private String userName;
	private String password;
	private byte[] avatar;

	public CreateAccountRequestTask(DatabaseHelper databaseHelper, String email, String userName, String password, byte[] avatar) {
		super(databaseHelper);
		
		this.email = email;
		this.userName = userName;
		this.password = password;
		this.avatar = avatar;
	}

	String getLoggingTag(){
		return getClass().getSimpleName();
	}
	
	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	@Override
	public CreateAccountResponse doInBackground(Void... params) {
		try {
			// We create the login request
			CreateAccountRequest createAccountRequest = new CreateAccountRequest();
			createAccountRequest.setEmail(email);
			createAccountRequest.setFullName(userName);
			createAccountRequest.setPassword(Utils.hashPassword(password));
			createAccountRequest.setIpAddress(NetworkUtils.getIpAddress());
			createAccountRequest.setAvatar(avatar);

			// We call the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.CREATE_ACCOUNT_PATH, createAccountRequest, CreateAccountResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void onPostExecute(CreateAccountResponse response) {
		boolean success = false;

		// We validate the response
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
			// Saving the information returned by the back-end
			try {
				// We reconstruct the UserStatus object
				UserStatus userStatus = new UserStatus(response.getUserStatusDTO());

				// We reconstruct the User object
				User user = new User(userStatus, response.getUserDTO());
				databaseHelper.createOrUpdateUser(user);

				// We reconstruct the UserContactData object
				UserContactDataDTO userContactDataDTO = response.getUserContactDataDTO();
				User ucdUpdatedBy = databaseHelper.getUser(userContactDataDTO.getUpdatedBy().longValue());
				User ucdPushedBy = databaseHelper.getUser(userContactDataDTO.getPushedBy().longValue());
				UserContactData userContactData = new UserContactData(user,ucdUpdatedBy,ucdPushedBy,userContactDataDTO);
				databaseHelper.createOrUpdateUserContactData(userContactData);

				// We reconstruct the UserAvatar object
				UserAvatarDTO userAvatarDTO = response.getUserAvatarDTO();
				User uaUpdatedBy = databaseHelper.getUser(userAvatarDTO.getUpdatedBy().longValue());
				User uaPushedBy = databaseHelper.getUser(userAvatarDTO.getPushedBy().longValue());
				UserAvatar userAvatar = new UserAvatar(user,uaUpdatedBy,uaPushedBy,userAvatarDTO);
				databaseHelper.createOrUpdateUserAvatar(userAvatar);

				// We execute tasks on success
				executeOnSuccess();
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
			}
		}
	}
}
