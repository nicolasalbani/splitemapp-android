package com.splitemapp.android.task;

import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.request.CreateAccountRequest;
import com.splitemapp.commons.domain.dto.response.CreateAccountResponse;
import com.splitemapp.commons.utils.Utils;

/**
 * Create account task which creates a new account for the user
 * @author nicolas
 *
 */
public class CreateAccountRequestTask extends AsyncTask<Void, Void, CreateAccountResponse> {
	private DatabaseHelper databaseHelper;
	private BaseFragment baseFragment;
	private String email;
	private String userName;
	private String password;
	private byte[] avatar;

	public CreateAccountRequestTask(DatabaseHelper databaseHelper, BaseFragment baseFragment, String email, String userName, String password, byte[] avatar) {
		this.databaseHelper = databaseHelper;
		this.baseFragment = baseFragment;
		this.email = email;
		this.userName = userName;
		this.password = password;
		this.avatar = avatar;
	}
	
	String getLoggingTag(){
		return getClass().getSimpleName();
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
	
	/**
	 * Executes a required action on success. This code executes after the processResult method.
	 */
	protected void executeOnSuccess(){};

	@Override
	public void onPostExecute(CreateAccountResponse createAccountResponse) {
		boolean success = false;

		// We validate the response
		if(createAccountResponse != null){
			success = createAccountResponse.getSuccess();
		}

		// We show the status toast if it failed
		if(!success){
			baseFragment.showToast("Create Account Failed!");
		}

		// We save the user and session information returned by the backend
		try {
			if(success){
				// We reconstruct the UserStatus object
				UserStatus userStatus = new UserStatus(createAccountResponse.getUserStatusDTO());

				// We reconstruct the User object
				User user = new User(userStatus, createAccountResponse.getUserDTO());
				databaseHelper.createOrUpdateUser(user);

				// We reconstruct the UserContactData object
				UserContactData userContactData = new UserContactData(user,createAccountResponse.getUserContactDataDTO());
				databaseHelper.createOrUpdateUserContactData(userContactData);

				// We reconstruct the UserAvatar object
				UserAvatar userAvatar = new UserAvatar(user, createAccountResponse.getUserAvatarDTO());
				databaseHelper.createOrUpdateUserAvatar(userAvatar);

				// We execute tasks on success
				executeOnSuccess();
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
		}
	}
}
