package com.splitemapp.android.screen.login;

import java.sql.SQLException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.createaccount.CreateAccountActivity;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.android.screen.home.HomeFragment;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.request.LoginRequest;
import com.splitemapp.commons.domain.dto.response.LoginResponse;
import com.splitemapp.commons.utils.Utils;

public class LoginFragment extends BaseFragment {

	private static final String TAG = LoginFragment.class.getSimpleName();

	private Button mLogin;
	private EditText mUserName;
	private EditText mPassword;
	private TextView mSignUp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_login, container, false);

		// We get the references for the user name and password text boxes
		mUserName = (EditText) v.findViewById(R.id.li_username_editText);
		mPassword = (EditText) v.findViewById(R.id.li_password_editText);

		// We get the reference to the login button and implement a OnClickListener
		mLogin = (Button) v.findViewById(R.id.li_login_button);
		mLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginRequestTask().execute();
			}
		});
		
		// We get the reference to the sign up button and implement a OnClickListener
		mSignUp = (TextView) v.findViewById(R.id.li_sign_up_link);
		mSignUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CreateAccountActivity.class);
				startActivity(intent);
			}
		});

		return v;
	}

	private class LoginRequestTask extends AsyncTask<Void, Void, LoginResponse> {

		@Override
		protected LoginResponse doInBackground(Void... params) {
			try {
				// We create the login request
				LoginRequest loginRequest = new LoginRequest();
				loginRequest.setDevice(Constants.DEVICE);
				loginRequest.setOsVersion(Constants.OS_VERSION);
				loginRequest.setUsername(mUserName.getText().toString());
				loginRequest.setPassword(Utils.hashPassword(mPassword.getText().toString()));
				
				// We call the rest service and send back the login response
				return callRestService(ServiceConstants.LOGIN_PATH, loginRequest, LoginResponse.class);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
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
					getHelper().updateSyncPullAt(User.class, createOrUpdate);
					
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
					getHelper().updateSyncPullAt(UserContactData.class, createOrUpdate);
					
					// We open the home activity class
					Intent intent = new Intent(getActivity(), HomeActivity.class);
					intent.putExtra(HomeFragment.EXTRA_USER_ID, user.getId());
					startActivity(intent);
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught while getting UserSession", e);
				}
			}
		}
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
