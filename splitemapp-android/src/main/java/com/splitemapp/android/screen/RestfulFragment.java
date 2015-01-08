package com.splitemapp.android.screen;

import java.sql.SQLException;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.request.CreateAccountRequest;
import com.splitemapp.commons.domain.dto.request.LoginRequest;
import com.splitemapp.commons.domain.dto.response.CreateAccountResponse;
import com.splitemapp.commons.domain.dto.response.LoginResponse;
import com.splitemapp.commons.utils.Utils;

public abstract class RestfulFragment extends BaseFragment{

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
	 * Creates an asynchronous new account request
	 * @param email	String containing the email address
	 * @param userName String containing the user name
	 * @param password String containing the password
	 */
	protected void createAccount(String email, String userName, String password){
		new CreateAccountRequestTask(email, userName, password).execute();
	}

	/**
	 * Creates an asynchronous login request
	 * @param userName String containing the user name
	 * @param password String containing the password
	 */
	protected void login(String userName, String password){
		new LoginRequestTask(userName, password).execute();
	}

	/**
	 * 
	 * @param servicePath String containing the rest service name
	 * @param request <E> The request object used in the rest service call
	 * @param responseType <T> The response class that the rest service call is supposed to return
	 * @return	<T> An instance of the response type specified as a parameter
	 */
	protected <E,T> T callRestService(String servicePath, E request, Class<T> responseType){
		// We create the url based on the provider serviceName
		String url = "http://"+Constants.BACKEND_HOST+":"+Constants.BACKEND_PORT+"/"+Constants.BACKEND_PATH+servicePath;

		// We get an instance of the spring framework RestTemplate and configure wrapping the root XML element
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

		// We use old version of request factory that uses HTTPClient instead of HttpURLConnection to avoid bugs
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());  

		// We make the POST rest service call
		T response = restTemplate.postForObject(url, request, responseType);
		return response;
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

		public CreateAccountRequestTask(String email, String userName, String password) {
			this.email = email;
			this.userName = userName;
			this.password = password;
		}

		@Override
		protected CreateAccountResponse doInBackground(Void... params) {
			try {
				// We create the login request
				CreateAccountRequest createAccountRequest = new CreateAccountRequest();
				createAccountRequest.setEmail(email);
				createAccountRequest.setUsername(userName);
				createAccountRequest.setPassword(Utils.hashPassword(password));
				createAccountRequest.setIpAddress(NetworkUtils.getIpAddress());

				// We call the rest service and send back the login response
				return callRestService(ServiceConstants.CREATE_ACCOUNT_PATH, createAccountRequest, CreateAccountResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(CreateAccountResponse createAccountResponse) {
			boolean createAccountSuccess = false;

			// We validate the response
			if(createAccountResponse != null){
				createAccountSuccess = createAccountResponse.getSuccess();
			}

			// We show the status toast
			showToast(createAccountSuccess ? "Create Account Successful!" : "Create Account Failed!");

			// We save the user and session information returned by the backend
			if(createAccountSuccess){
				try {
					// We reconstruct the user status object
					UserStatus userStatus = new UserStatus(createAccountResponse.getUserStatusDTO());

					// We reconstruct the user object
					User user = new User(userStatus, createAccountResponse.getUserDTO());
					CreateOrUpdateStatus createOrUpdate = getHelper().getUserDao().createOrUpdate(user);
					getHelper().updateSyncStatusPullAt(User.class, createOrUpdate);

					// We reconstruct the user contact data object
					UserContactData userContactData = new UserContactData(user,createAccountResponse.getUserContactDataDTO());
					createOrUpdate = getHelper().getUserContactDataDao().createOrUpdate(userContactData);
					getHelper().updateSyncStatusPullAt(UserContactData.class, createOrUpdate);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
				}
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
		protected LoginResponse doInBackground(Void... params) {
			try {
				// We create the login request
				LoginRequest loginRequest = new LoginRequest();
				loginRequest.setDevice(Constants.DEVICE);
				loginRequest.setOsVersion(Constants.OS_VERSION);
				loginRequest.setUsername(userName);
				loginRequest.setPassword(Utils.hashPassword(password));

				// We call the rest service and send back the login response
				return callRestService(ServiceConstants.LOGIN_PATH, loginRequest, LoginResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(LoginResponse loginResponse) {
			boolean loginSuccess = false;

			// We validate the response
			if(loginResponse != null){
				loginSuccess = loginResponse.getSuccess();
			}

			// We show the status toast
			showToast(loginSuccess ? "Login Successful!" : "Login Failed!");

			// We save the user and session information returned by the backend
			if(loginSuccess){
				try {
					// We reconstruct the user status object
					UserStatus userStatus = new UserStatus(loginResponse.getUserStatusDTO());

					// We reconstruct the user object
					User user = new User(userStatus, loginResponse.getUserDTO());
					CreateOrUpdateStatus createOrUpdate = getHelper().getUserDao().createOrUpdate(user);
					getHelper().updateSyncStatusPullAt(User.class, createOrUpdate);

					// We clear all previous session records
					for(UserSession userSession:getHelper().getUserSessionDao().queryForAll()){
						getHelper().getUserSessionDao().delete(userSession);
					}

					// We reconstruct the user session object
					UserSession userSession = new UserSession(user, loginResponse.getUserSessionDTO());
					createOrUpdate = getHelper().getUserSessionDao().createOrUpdate(userSession);

					// We reconstruct the user contact data object
					UserContactData userContactData = new UserContactData(user, loginResponse.getUserContactDataDTO());
					createOrUpdate = getHelper().getUserContactDataDao().createOrUpdate(userContactData);
					getHelper().updateSyncStatusPullAt(UserContactData.class, createOrUpdate);

					// We open the home activity class
					startHomeActivity(user.getId());
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while getting UserSession", e);
				}
			}
		}
	}
}
