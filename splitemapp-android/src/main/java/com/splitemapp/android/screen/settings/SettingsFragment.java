package com.splitemapp.android.screen.settings;

import java.sql.SQLException;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;

public class SettingsFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = SettingsFragment.class.getSimpleName();

	private User mCurrentUser;

	private ImageView mAvatarImageView;
	private EditText mFullNameEditText;
	private TextView mEmailTextView;
	
	private boolean avatarChanged;
	private byte[] mAvatarData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We get the user instance
		try {
			mCurrentUser = getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = super.onCreateView(inflater, container, savedInstanceState);

		// We set the user avatar in the navigation view
		mAvatarImageView = (ImageView) v.findViewById(R.id.s_avatar_imageView);
		setUsetAvatar(mAvatarImageView, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);
		mAvatarImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openImageSelector(mAvatarImageView.getWidth(), mAvatarImageView.getHeight());
			}
		});

		// We populate the first name in the main view
		mFullNameEditText = (EditText) v.findViewById(R.id.s_full_name_editText);
		mFullNameEditText.setText(mCurrentUser.getFullName());

		// We populate the email in the main view
		mEmailTextView  = (TextView) v.findViewById(R.id.s_email_textView);
		mEmailTextView.setText(mCurrentUser.getUsername());

		return v;
	}

	@Override
	public void executeOnImageSelection(Bitmap selectedBitmap) {
		// Updating change flag
		avatarChanged = true;
		
		// Setting the avatar data
		mAvatarData = ImageUtils.bitmapToByteArray(selectedBitmap, ImageUtils.IMAGE_QUALITY_MAX);
		
		// Updating project image on screen
		mAvatarImageView.setImageBitmap(selectedBitmap);
	}
	
	@Override
	public boolean getCropImage() {
		return true;
	}

	@Override
	protected void onRefresh(String response) {
		// Refreshing contacts list after making the sync
		refreshFragment();
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_settings;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.s_title;
	}

	@Override
	protected void doneAction() {
		// If full name changed we persist the change
		if(!mCurrentUser.getFullName().equals(mFullNameEditText.getText().toString())){
			try {
				mCurrentUser.setFullName(mFullNameEditText.getText().toString());
				getHelper().updateUser(mCurrentUser);
				pushUsers();
			} catch (SQLException e) {
				Log.e(TAG, "SQLException caught while updating User entity!", e);
			}
		}
		
		// If avatar changed we persist the change
		if(avatarChanged){
			UserAvatar userAvatar;
			try {
				userAvatar = getHelper().getUserAvatarByUserId(mCurrentUser.getId());
				userAvatar.setAvatarData(mAvatarData);
				getHelper().updateUserAvatar(userAvatar);
				pushUserAvatars();
			} catch (SQLException e) {
				Log.e(TAG, "SQLException caught while updating UserAvatar entity!", e);
			}
		}
		
		getActivity().finish();
	}
}
