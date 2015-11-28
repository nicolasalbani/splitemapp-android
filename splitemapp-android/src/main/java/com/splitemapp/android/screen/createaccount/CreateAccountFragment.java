package com.splitemapp.android.screen.createaccount;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;

public class CreateAccountFragment extends RestfulFragment {
	
	private static final String TAG = CreateAccountFragment.class.getSimpleName();

	private Button mCreateAccount;
	private EditText mEmail;
	private EditText mUserName;
	private EditText mPassword;

	// The view to inflate
	View v;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.fragment_create_account, container, false);

		// We get the references for the user name and password text boxes
		mUserName = (EditText) v.findViewById(R.id.ca_username_editText);
		mEmail = (EditText) v.findViewById(R.id.ca_email_editText);
		mPassword = (EditText) v.findViewById(R.id.ca_password_editText);

		// We get the reference to the login button and implement a OnClickListener
		mCreateAccount = (Button) v.findViewById(R.id.create_account_create_account_button);
		mCreateAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Creating account
				createAccount(mEmail.getText().toString(), mUserName.getText().toString(), mPassword.getText().toString(), null);
			}
		});

		return v;
	}
	
	@Override
	public boolean getCropImage() {
		return true;
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
