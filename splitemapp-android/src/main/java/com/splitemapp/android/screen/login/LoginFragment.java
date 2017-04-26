package com.splitemapp.android.screen.login;

import java.sql.SQLException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.managecontacts.AddContactsDialog;
import com.splitemapp.android.validator.EmailValidator;
import com.splitemapp.commons.domain.User;

public class LoginFragment extends RestfulFragment {

	private static final String TAG = LoginFragment.class.getSimpleName();

	private Button mLogin;
	private EditText mUserName;
	private EditText mPassword;
	private View mForgotPassword;

	private boolean mIsEmailValid;
	private EditText mEmailEditText;
	private Button mResetButton;

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
			startHomeActivity();
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

		// We get the references for the forgot password view
		mForgotPassword = v.findViewById(R.id.li_forgot_password);
		mForgotPassword.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// Setting invalid email by default
				mIsEmailValid = false;

				// Opening add contacts dialog
				final AddContactsDialog addContactDialog = new AddContactsDialog(getActivity()) {
					@Override
					public int getLinearLayoutView() {
						return R.layout.dialog_forgot_password;
					}
				};

				// Getting the email address
				mEmailEditText = (EditText) addContactDialog.findViewById(R.id.fp_email_editText);
				mEmailEditText.addTextChangedListener(new EmailValidator(mEmailEditText,  true, R.drawable.shape_bordered_rectangle) {
					@Override
					public void onValidationAction(boolean isValid) {
						mIsEmailValid = isValid;
						updateActionButton();
					}
				});

				// Creating the OnClickListener for the send button
				mResetButton = (Button) addContactDialog.findViewById(R.id.fp_reset_password_button);
				mResetButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						View emailView = addContactDialog.findViewById(R.id.fp_email_view);
						View successView = addContactDialog.findViewById(R.id.fp_reset_success_view);

						// Calling the reset password task
						sendPasswordReset(mEmailEditText.getText().toString(), emailView, successView);
					}
				});
				updateActionButton();

				addContactDialog.show();
			}
		});

		return v;
	}

	private void updateActionButton(){
		mResetButton.setEnabled(mIsEmailValid);
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
