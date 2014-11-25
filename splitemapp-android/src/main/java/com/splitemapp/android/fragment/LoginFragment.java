package com.splitemapp.android.fragment;

import java.sql.SQLException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.domain.dto.LoginRequest;
import com.splitemapp.android.domain.dto.LoginResponse;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.utils.Utils;

public class LoginFragment extends BaseFragment {

	private static final String TAG = LoginFragment.class.getSimpleName();

	private Button mLogin;
	private EditText mUserName;
	private EditText mPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_login, container, false);

		// We get the references for the user name and password text boxes
		mUserName = (EditText) v.findViewById(R.id.user_name);
		mPassword = (EditText) v.findViewById(R.id.password);

		// We get the reference to the login button and implement a OnClickListener
		mLogin = (Button) v.findViewById(R.id.login_button);
		mLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginRequestTask().execute();
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
				return callRestService(Constants.LOGIN_SERVICE, loginRequest, LoginResponse.class);
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
					
					// We reconstruct the user session object
					UserSession userSession = new UserSession(user, loginResponse.getUserSessionDTO());
					getHelper().getUserSessionDao().createOrUpdate(userSession);
					getHelper().updateSyncPullAt(UserSession.class, createOrUpdate);
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught while getting UserSession", e);
				}
			}
		}
	}
}
