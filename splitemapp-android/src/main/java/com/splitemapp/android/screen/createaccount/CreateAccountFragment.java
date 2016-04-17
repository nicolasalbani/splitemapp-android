package com.splitemapp.android.screen.createaccount;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.validator.EmailValidator;
import com.splitemapp.android.validator.EmptyValidator;
import com.splitemapp.android.validator.PasswordValidator;

public class CreateAccountFragment extends RestfulFragment {
	
	private static final String TAG = CreateAccountFragment.class.getSimpleName();

	private Button mCreateAccount;
	private EditText mEmail;
	private EditText mUserName;
	private EditText mPassword;
	
	private boolean mIsPasswordValid;
	private boolean mIsEmailValid;
	private boolean mIsUserNameValid;

	// The view to inflate
	View v;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mIsPasswordValid = false;
		mIsEmailValid = false;
		mIsUserNameValid = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.fragment_create_account, container, false);

		// We assign a validator to the user name field
		mUserName = (EditText) v.findViewById(R.id.ca_username_editText);
		mUserName.addTextChangedListener(new EmptyValidator(mUserName, true) {
			@Override
			public void onValidationAction(boolean isValid) {
				mIsUserNameValid = isValid;
				updateActionButton();
			}
		});
		
		// We assign a validator to the email field
		mEmail = (EditText) v.findViewById(R.id.ca_email_editText);
		mEmail.addTextChangedListener(new EmailValidator(mEmail, true) {
			@Override
			public void onValidationAction(boolean isValid) {
				mIsEmailValid = isValid;
				updateActionButton();
			}
		});
		
		// We assign a validator to the password field
		mPassword = (EditText) v.findViewById(R.id.ca_password_editText);
		mPassword.addTextChangedListener(new PasswordValidator(mPassword, true){
			@Override
			public void onValidationAction(boolean isValid) {
				mIsPasswordValid = isValid;
				updateActionButton();
			}
		});

		// We get the reference to the login button and implement a OnClickListener
		mCreateAccount = (Button) v.findViewById(R.id.create_account_create_account_button);
		mCreateAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Creating account
				createAccount(mEmail.getText().toString(), mUserName.getText().toString(), mPassword.getText().toString(), null);
			}
		});
		updateActionButton();

		return v;
	}
	
	private void updateActionButton(){
		mCreateAccount.setEnabled(mIsEmailValid && mIsPasswordValid && mIsUserNameValid);
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
