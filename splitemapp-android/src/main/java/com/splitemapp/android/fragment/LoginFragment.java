package com.splitemapp.android.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.domain.dto.LoginRequest;
import com.splitemapp.android.domain.dto.LoginResponse;

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
				LoginRequest loginRequest = new LoginRequest();
				loginRequest.setDevice(Constants.DEVICE);
				loginRequest.setUsername(mUserName.getText().toString());
				loginRequest.setPassword(mPassword.getText().toString());
				return callRestService(Constants.LOGIN_SERVICE, loginRequest, LoginResponse.class);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(LoginResponse loginResponse) {
			Log.i(TAG,"FirstName		: " +loginResponse.getFirstName());
			Log.i(TAG,"LastName			: " +loginResponse.getLastName());
			Log.i(TAG,"SessionToken		: " +loginResponse.getSessionToken());
			Log.i(TAG,"Username			: " +loginResponse.getUsername());
			Log.i(TAG,"UserId			: " +loginResponse.getUserId());
			Log.i(TAG,"ChangePassword	: " +loginResponse.getChangePassword());
			Log.i(TAG,"IsNewDevice		: " +loginResponse.getIsNewDevice());
			
			//TODO Evaluate the loginResponse and do stuff
		}
	}
}
