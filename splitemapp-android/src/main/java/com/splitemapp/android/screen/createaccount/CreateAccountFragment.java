package com.splitemapp.android.screen.createaccount;

import java.sql.SQLException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.domain.dto.CreateAccountRequest;
import com.splitemapp.android.domain.dto.CreateAccountResponse;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.commons.constants.ServicePath;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.utils.Utils;

public class CreateAccountFragment extends BaseFragment {

	private static final String TAG = CreateAccountFragment.class.getSimpleName();

	private static final int SELECT_PICTURE = 1;

	private ImageView mAvatar;
	private Button mCreateAccount;
	private EditText mUserName;
	private EditText mEmail;
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

		// We get the reference to the avatar image
		mAvatar = (ImageView) v.findViewById(R.id.ca_avatar_imageView);
		mAvatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// in onCreate or any event where your want the user to select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
			}
		});

		// We get the reference to the login button and implement a OnClickListener
		mCreateAccount = (Button) v.findViewById(R.id.create_account_create_account_button);
		mCreateAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new CreateAccountRequestTask().execute();
			}
		});

		return v;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PICTURE  && null != data) {
			// We get the image path
			Uri uri = data.getData();
			String imagePath = getImagePath(uri);

			// We decode scaled bitmap to avoid out of memory errors
			Bitmap scaledBitmap = decodeScaledBitmap(imagePath, Constants.AVATAR_WIDTH, Constants.AVATAR_HEIGHT);
			
			// We set the avatar image
			Bitmap croppedBitmap = getCroppedBitmap(scaledBitmap);
			mAvatar.setImageBitmap(croppedBitmap);
		}
	}

	private class CreateAccountRequestTask extends AsyncTask<Void, Void, CreateAccountResponse> {

		@Override
		protected CreateAccountResponse doInBackground(Void... params) {
			try {
				// We create the login request
				CreateAccountRequest createAccountRequest = new CreateAccountRequest();
				createAccountRequest.setEmail(mEmail.getText().toString());
				createAccountRequest.setUsername(mUserName.getText().toString());
				createAccountRequest.setPassword(Utils.hashPassword(mPassword.getText().toString()));
				createAccountRequest.setIpAddress(getIpAddress());

				// We call the rest service and send back the login response
				return callRestService(ServicePath.CREATE_ACCOUNT, createAccountRequest, CreateAccountResponse.class);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
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
					getHelper().updateSyncPullAt(User.class, createOrUpdate);

					// We reconstruct the user contact data object
					UserContactData userContactData = new UserContactData(user,createAccountResponse.getUserContactDataDTO());
					getHelper().getUserContactDataDao().createOrUpdate(userContactData);
					getHelper().updateSyncPullAt(UserContactData.class, createOrUpdate);
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught while getting UserSession", e);
				}
			}
		}
	}
}
