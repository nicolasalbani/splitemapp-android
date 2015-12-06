package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dialog.CustomProgressDialog;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.request.CreateAccountRequest;
import com.splitemapp.commons.domain.dto.request.LoginRequest;
import com.splitemapp.commons.domain.dto.request.SynchronizeContactsRequest;
import com.splitemapp.commons.domain.dto.response.CreateAccountResponse;
import com.splitemapp.commons.domain.dto.response.LoginResponse;
import com.splitemapp.commons.domain.dto.response.SynchronizeContactsResponse;
import com.splitemapp.commons.rest.RestUtils;
import com.splitemapp.commons.utils.Utils;

public abstract class RestfulFragment extends BaseFragment{

	private CustomProgressDialog waitDialog = null;

	static{
		// We initialize logging
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
	}

	/**
	 * Shows the progress indicator
	 */
	public void showProgressIndicator(){
		if(waitDialog == null || !waitDialog.isShowing()){
			waitDialog = CustomProgressDialog.show(getContext());
		}
	}

	/**
	 * Hides the progress indicator
	 */
	public void hideProgressIndicator(){
		if(waitDialog != null && waitDialog.isShowing()){
			waitDialog.dismiss();
		}
	}

	/**
	 * Creates an asynchronous new account request
	 * @param email	String containing the email address
	 * @param userName String containing the user name
	 * @param password String containing the password
	 */
	public void createAccount(String email, String userName, String password, byte[] avatar){
		new CreateAccountRequestTask(email, userName, password, avatar).execute();
	}

	/**
	 * Creates an asynchronous login request
	 * @param userName String containing the user name
	 * @param password String containing the password
	 */
	public void login(String userName, String password){
		new LoginRequestTask(userName, password).execute();
	}

	/**
	 * Create an asynchronous synchronize contacts request
	 * @param contactsEmailAddressList List containing contacts email addresses
	 */
	public void synchronizeContacts(List<String> contactsEmailAddressList){
		new SynchronizeContactsRequestTask(contactsEmailAddressList).execute();
	}

	/**
	 * 
	 * @param servicePath String containing the rest service name
	 * @param request <E> The request object used in the rest service call
	 * @param responseType <T> The response class that the rest service call is supposed to return
	 * @return	<T> An instance of the response type specified as a parameter
	 */
	public <E,T> T callRestService(String servicePath, E request, Class<T> responseType){
		// We create the url based on the provider serviceName
		String serviceUrl = "http://"+Constants.BACKEND_HOST+":"+Constants.BACKEND_PORT+"/"+Constants.BACKEND_PATH+servicePath;

		return RestUtils.callRestService(serviceUrl, request, responseType);
	}

	/**
	 * Create account task which creates a new account for the user
	 * @author nicolas
	 *
	 */
	private class CreateAccountRequestTask extends AsyncTask<Void, Void, CreateAccountResponse> {
		private String email;
		private String userName;
		private String password;
		private byte[] avatar;

		public CreateAccountRequestTask(String email, String userName, String password, byte[] avatar) {
			this.email = email;
			this.userName = userName;
			this.password = password;
			this.avatar = avatar;
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
				return callRestService(ServiceConstants.CREATE_ACCOUNT_PATH, createAccountRequest, CreateAccountResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			// We show the progress indicator
			showProgressIndicator();
		}

		@Override
		public void onPostExecute(CreateAccountResponse createAccountResponse) {
			boolean success = false;

			// We validate the response
			if(createAccountResponse != null){
				success = createAccountResponse.getSuccess();
			}

			// We hide the progress indicator
			hideProgressIndicator();

			// We show the status toast if it failed
			if(!success){
				showToast("Create Account Failed!");
			}

			// We save the user and session information returned by the backend
			try {
				if(success){
					// We reconstruct the UserStatus object
					UserStatus userStatus = new UserStatus(createAccountResponse.getUserStatusDTO());

					// We reconstruct the User object
					User user = new User(userStatus, createAccountResponse.getUserDTO());
					getHelper().createOrUpdateUser(user);

					// We reconstruct the UserContactData object
					UserContactData userContactData = new UserContactData(user,createAccountResponse.getUserContactDataDTO());
					getHelper().createOrUpdateUserContactData(userContactData);

					// We reconstruct the UserAvatar object
					UserAvatar userAvatar = new UserAvatar(user, createAccountResponse.getUserAvatarDTO());
					getHelper().createOrUpdateUserAvatar(userAvatar);

					// We login
					login(email, password);
				}
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
			}
		}
	}

	/**
	 * Login tasks which enables the user to login for the first time and authenticate against the remote server
	 * @author nicolas
	 *
	 */
	private class LoginRequestTask extends AsyncTask<Void, Void, LoginResponse> {
		private String userName;
		private String password;

		public LoginRequestTask(String userName, String password){
			this.userName = userName;
			this.password = password;
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
				return callRestService(ServiceConstants.LOGIN_PATH, loginRequest, LoginResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			// We show the progress indicator
			showProgressIndicator();
		}

		@Override
		public void onPostExecute(LoginResponse loginResponse) {
			boolean success = false;

			// Validating the response
			if(loginResponse != null){
				success = loginResponse.getSuccess();
			}

			// We hide the progress indicator
			hideProgressIndicator();

			// We show the status toast if it failed
			if(!success){
				showToast("Login Failed!");
			}

			// Saving the user and session information returned by the backend
			try {
				if(success){
					// Reconstructing the user status object
					UserStatus userStatus = new UserStatus(loginResponse.getUserStatusDTO());

					// Reconstructing the user object
					User user = new User(userStatus, loginResponse.getUserDTO());
					getHelper().createOrUpdateUser(user);

					// Clearing all previous session records
					getHelper().deleteAllUserSessions();

					// Reconstructing the user session object
					UserSession userSession = new UserSession(user, loginResponse.getUserSessionDTO());
					getHelper().createOrUpdateUserSession(userSession);

					// Reconstructing the user contact data object
					UserContactData userContactData = new UserContactData(user, loginResponse.getUserContactDataDTO());
					// Replacing user contact data if email already exists
					UserContactData existingUserContactData = getHelper().getUserContactData(userContactData.getContactData());
					if(existingUserContactData != null){
						userContactData.setId(existingUserContactData.getId());
					}
					getHelper().createOrUpdateUserContactData(userContactData);

					// Reconstructing the user avatar object
					UserAvatar userAvatar = new UserAvatar(user, loginResponse.getUserAvatarDTO());
					// Replacing user avatar if it already exists
					UserAvatar existingUserAvatar = getHelper().getUserAvatarByUserId(user.getId());
					if(existingUserAvatar != null){
						userAvatar.setId(existingUserAvatar.getId());
					}
					getHelper().createOrUpdateUserAvatar(userAvatar);

					// Opening the home activity class
					startHomeActivity(user.getId());
				}
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
			}
		}
	}

	/**
	 * Synchronize contacts task which queries the remote server for user information
	 * @author nicolas
	 *
	 */
	private class SynchronizeContactsRequestTask extends AsyncTask<Void, Void, SynchronizeContactsResponse> {
		private List<String> contactsEmailAddressList;

		public SynchronizeContactsRequestTask(List<String> contactsEmailAddressList) {
			this.contactsEmailAddressList = contactsEmailAddressList;
		}

		@Override
		public SynchronizeContactsResponse doInBackground(Void... params) {
			try {
				// We create the login request
				SynchronizeContactsRequest synchronizeContactsRequest = new SynchronizeContactsRequest();
				synchronizeContactsRequest.setContactsEmailAddressList(contactsEmailAddressList);

				// We call the rest service and send back the synchronize contacts
				return callRestService(ServiceConstants.SYNCHRONIZE_CONTACTS_PATH, synchronizeContactsRequest, SynchronizeContactsResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			// We show the progress indicator
			showProgressIndicator();
		}

		@Override
		public void onPostExecute(SynchronizeContactsResponse synchronizeContactsResponse) {
			boolean success = false;

			// Validating the response
			if(synchronizeContactsResponse != null){
				success = synchronizeContactsResponse.getSuccess();
			}

			// We hide the progress indicator
			hideProgressIndicator();

			// We show the status toast if it failed
			if(!success){
				showToast("Synchronize Contacts Failed!");
			}

			// Saving the user and user contact data information returned by the backend
			try {
				getHelper().updateSyncStatusPullAt(User.class, success);
				getHelper().updateSyncStatusPullAt(UserContactData.class, success);
				getHelper().updateSyncStatusPullAt(UserAvatar.class, success);
				if(success){
					for(UserDTO userDTO:synchronizeContactsResponse.getUserDTOList()){
						// Reconstructing the user status object
						UserStatus userStatus = getHelper().getUserStatus(userDTO.getUserStatusId().shortValue());

						// Reconstructing the user object
						User user = new User(userStatus, userDTO);
						getHelper().createOrUpdateUser(user);

						// Reconstructing the user contact data object
						for(UserContactDataDTO userContactDataDTO:synchronizeContactsResponse.getUserContactDataDTOList()){
							// Matching the appropriate user contact data
							if(userDTO.getId() == userContactDataDTO.getUserId()){
								UserContactData userContactData = new UserContactData(user,userContactDataDTO);
								getHelper().createOrUpdateUserContactData(userContactData);
							}
						}

						// Reconstructing the user avatar data object
						for(UserAvatarDTO userAvatarDTO:synchronizeContactsResponse.getUserAvatarDTOList()){
							// Matching the appropriate user avatar
							if(userDTO.getId() == userAvatarDTO.getUserId()){
								UserAvatar userAvatar = new UserAvatar(user, userAvatarDTO);
								getHelper().createOrUpdateUserAvatar(userAvatar);
							}
						}
					}
				}
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while synchronizing contacts", e);
			}
		}
	}
}
