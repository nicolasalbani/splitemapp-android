package com.splitemapp.android.screen.settings;

import java.sql.SQLException;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.utils.PreferencesManager;
import com.splitemapp.android.validator.EmptyValidator;
import com.splitemapp.android.validator.PasswordConfirmValidator;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;

public class SettingsFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = SettingsFragment.class.getSimpleName();

	private User mCurrentUser;

	private ImageView mAvatarImageView;
	private TextView mUserInitialsTextView;
	private EditText mFullNameEditText;
	private TextView mEmailTextView;

	private SwitchCompat mNewProjectSwitchCompat;
	private SwitchCompat mNewExpenseSwitchCompat;
	private SwitchCompat mUpdatedProjectSwitchCompat;

	private Button mAskAQuestionButton;
	private EditText mMessageEditText;
	private Button mSendButton;

	private Button mChangePasswordButton;
	private Button mChangeButton;
	private EditText mCurrentPasswordText;
	private EditText mNewPasswordText;
	private EditText mNewPasswordConfirmationText;
	private boolean isCurrentPasswordValid;
	private boolean isNewPasswordConfirmationValid;

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
		mUserInitialsTextView = (TextView) v.findViewById(R.id.s_initials_textView);
		setUsetAvatarToImageView(mAvatarImageView, mUserInitialsTextView, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);
		mAvatarImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openImageSelector(mAvatarImageView.getWidth(), mAvatarImageView.getHeight());
			}
		});

		// We populate the first name in the main view
		mFullNameEditText = (EditText) v.findViewById(R.id.s_full_name_editText);
		mFullNameEditText.setText(mCurrentUser.getFullName());
		mFullNameEditText.addTextChangedListener(new EmptyValidator(mFullNameEditText, false, getContext()) {
			@Override
			public void onValidationAction(boolean isValid) {
				if(isValid){
					setDoneActionEnabled();
				} else {
					setDoneActionDisabled();
				}
			}
		});

		// We populate the email in the main view
		mEmailTextView  = (TextView) v.findViewById(R.id.s_email_textView);
		mEmailTextView.setText(mCurrentUser.getUsername());

		// We set the switch compat
		mNewProjectSwitchCompat = (SwitchCompat) v.findViewById(R.id.s_new_project_switch);
		mNewProjectSwitchCompat.setChecked(getPrefsManager().getBoolean(PreferencesManager.NOTIFY_NEW_PROJECT));

		// We set the switch compat
		mNewExpenseSwitchCompat = (SwitchCompat) v.findViewById(R.id.s_new_expense_switch);
		mNewExpenseSwitchCompat.setChecked(getPrefsManager().getBoolean(PreferencesManager.NOTIFY_NEW_EXPENSE));

		// We set the switch compat
		mUpdatedProjectSwitchCompat = (SwitchCompat) v.findViewById(R.id.s_updated_cover_switch);
		mUpdatedProjectSwitchCompat.setChecked(getPrefsManager().getBoolean(PreferencesManager.NOTIFY_UPDATED_PROJECT_COVER));

		// We set the Ask a question button
		mChangePasswordButton = (Button) v.findViewById(R.id.s_change_password_button);
		mChangePasswordButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// Opening dialog
				final ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(getActivity()) {
					@Override
					public int getLinearLayoutView() {
						return R.layout.dialog_change_password;
					}
				};

				// Getting the current password
				mCurrentPasswordText = (EditText) changePasswordDialog.findViewById(R.id.pc_current_password_editText);
				mCurrentPasswordText.addTextChangedListener(new EmptyValidator(mCurrentPasswordText, true, getContext()) {
					@Override
					public void onValidationAction(boolean isValid) {
						isCurrentPasswordValid = isValid;
						updateChangePasswordButton();
					}
				});

				// Getting the new password
				mNewPasswordText = (EditText) changePasswordDialog.findViewById(R.id.pc_new_password_editText);
				mNewPasswordConfirmationText = (EditText) changePasswordDialog.findViewById(R.id.pc_confirm_password_editText);
				mNewPasswordText.addTextChangedListener(new PasswordConfirmValidator(mNewPasswordText, mNewPasswordConfirmationText, true, getContext()) {
					@Override
					public void onValidationAction(boolean isValid) {
						isNewPasswordConfirmationValid = isValid;
						updateChangePasswordButton();
					}
				});
				mNewPasswordConfirmationText.addTextChangedListener(new PasswordConfirmValidator(mNewPasswordText, mNewPasswordConfirmationText, true, getContext()) {
					@Override
					public void onValidationAction(boolean isValid) {
						isNewPasswordConfirmationValid = isValid;
						updateChangePasswordButton();
					}
				});
				
				// Creating the OnClickListener for the send button
				mChangeButton = (Button) changePasswordDialog.findViewById(R.id.cp_change_password_button);
				mChangeButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						View changePassView = changePasswordDialog.findViewById(R.id.pc_password_change_view);
						View successView = changePasswordDialog.findViewById(R.id.cp_change_success_view);

						changePassword(mCurrentPasswordText.getText().toString(), mNewPasswordText.getText().toString(), changePassView, successView);
					}
				});
				mChangeButton.setEnabled(false);
				
				changePasswordDialog.show();
			}
		});

		// We set the Ask a question button
		mAskAQuestionButton = (Button) v.findViewById(R.id.s_ask_a_question_button);
		mAskAQuestionButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// Opening dialog
				final QuestionsDialog questionsDialog = new QuestionsDialog(getActivity()) {
					@Override
					public int getLinearLayoutView() {
						return R.layout.dialog_questions;
					}
				};

				// Getting the message
				mMessageEditText = (EditText) questionsDialog.findViewById(R.id.aaq_message_EditText);
				mMessageEditText.addTextChangedListener(new EmptyValidator(mMessageEditText, true, R.drawable.shape_bordered_rectangle, getContext()) {
					@Override
					public void onValidationAction(boolean isValid) {
						mSendButton.setEnabled(isValid);
					}
				});

				// Creating the OnClickListener for the send button
				mSendButton = (Button) questionsDialog.findViewById(R.id.aaq_send_button);
				mSendButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						View messageView = questionsDialog.findViewById(R.id.aaq_message_view);
						View successView = questionsDialog.findViewById(R.id.aaq_success_view);

						sendQuestion(mMessageEditText.getText().toString(), messageView, successView);
					}
				});
				mSendButton.setEnabled(false);

				questionsDialog.show();
			}
		});

		return v;
	}

	private void updateChangePasswordButton(){
		mChangeButton.setEnabled(isCurrentPasswordValid && isNewPasswordConfirmationValid);
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

		// Updating notification preferences
		getPrefsManager().setBoolean(PreferencesManager.NOTIFY_NEW_PROJECT, mNewProjectSwitchCompat.isChecked());
		getPrefsManager().setBoolean(PreferencesManager.NOTIFY_NEW_EXPENSE, mNewExpenseSwitchCompat.isChecked());
		getPrefsManager().setBoolean(PreferencesManager.NOTIFY_UPDATED_PROJECT_COVER, mUpdatedProjectSwitchCompat.isChecked());

		getActivity().finish();
	}
}
