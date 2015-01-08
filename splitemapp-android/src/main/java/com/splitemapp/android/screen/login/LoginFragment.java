package com.splitemapp.android.screen.login;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.createaccount.CreateAccountActivity;
import com.splitemapp.commons.domain.User;

public class LoginFragment extends RestfulFragment {

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
		
		// If there is a user logged in already we go directly to the home screen
		User loggedUser = null;
		try {
			loggedUser = getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		
		if(loggedUser != null){
			// We open the home activity class
			startHomeActivity(loggedUser.getId());
		}

		// Otherwise, we inflate the login fragment
		View v = inflater.inflate(R.layout.fragment_login, container, false);

		// We get the references for the user name and password text boxes
		mUserName = (EditText) v.findViewById(R.id.li_username_editText);
		mPassword = (EditText) v.findViewById(R.id.li_password_editText);

		// We get the reference to the login button and implement a OnClickListener
		mLogin = (Button) v.findViewById(R.id.li_login_button);
		mLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					getHelper().deleteAllUserSessions();
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught!", e);
				}
				login(mUserName.getText().toString(), mPassword.getText().toString());
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

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
