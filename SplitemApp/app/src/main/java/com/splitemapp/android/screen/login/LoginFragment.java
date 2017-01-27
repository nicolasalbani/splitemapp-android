package com.splitemapp.android.screen.login;

import java.sql.SQLException;
import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.managecontacts.AddContactsDialog;
import com.splitemapp.android.validator.EmailValidator;
import com.splitemapp.commons.domain.User;

import org.json.JSONObject;

public class LoginFragment extends RestfulFragment {

	private static final String TAG = LoginFragment.class.getSimpleName();

	private Button mLogin;
	private Button mAccountLogin;
    private LoginButton mFacebookLogin;
	private EditText mUserName;
	private EditText mPassword;
	private View mForgotPassword;
	private View mLoginOptionsView;
	private View mAccountLoginView;

	private boolean mIsEmailValid;
	private EditText mEmailEditText;
	private Button mResetButton;

    private CallbackManager callbackManager;
    private AccessToken accessToken;

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

        // We get the reference to the login layouts
        mLoginOptionsView = v.findViewById(R.id.li_login_options_view);
        mAccountLoginView = v.findViewById(R.id.li_account_login_view);

        // Set default visibility
        mLoginOptionsView.setVisibility(View.VISIBLE);
        mAccountLoginView.setVisibility(View.GONE);

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

		// We get the reference to the splitemapp account button and implement a OnClickListener
		mAccountLogin = (Button) v.findViewById(R.id.li_account_login_button);
		mAccountLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoginOptionsView.setVisibility(View.GONE);
				mAccountLoginView.setVisibility(View.VISIBLE);
			}
		});

        // We get the reference to the facebook account button and implement a OnClickListener
        mFacebookLogin = (LoginButton) v.findViewById(R.id.li_facebook_login_button);
        mFacebookLogin.setReadPermissions(Arrays.asList("public_profile", "email"));
        mFacebookLogin.setFragment(this);
        mFacebookLogin.setDefaultAudience(DefaultAudience.FRIENDS);
        mFacebookLogin.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);

        // Creando CallbackManager y usandolo para registrar el callback del boton
        callbackManager = CallbackManager.Factory.create();
        mFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                } else {
                                    // Getting content from Facebook response
                                    String email = me.optString("email");
                                    String name = me.optString("name");
                                    String pictureUrl = me.optJSONObject("picture").optJSONObject("data").optString("url");
                                    //TODO Descargar imagen y enviarla en creacion de cuenta

                                    // Creating SplitemApp account with facebook information
                                    //TODO Chequear si existe una cuenta antes de intentar crearla
                                    createAccount(email, name, accessToken.getToken(), null);
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,picture,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // Do nothing
            }

            @Override
            public void onError(FacebookException exception) {
                // Do nothing
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
				mEmailEditText.addTextChangedListener(new EmailValidator(mEmailEditText,  true, R.drawable.shape_bordered_rectangle, getContext()) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
