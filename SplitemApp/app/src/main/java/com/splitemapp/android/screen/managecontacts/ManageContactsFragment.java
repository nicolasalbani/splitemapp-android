package com.splitemapp.android.screen.managecontacts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.validator.EmailValidator;
import com.splitemapp.commons.domain.User;

public class ManageContactsFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = ManageContactsFragment.class.getSimpleName();

	private List<User> mContacts;
	private ListView mContactsList;
	private View mAddContactView;

	private Button mSearchButton;
	private Button mInviteButton;
	private EditText mEmailEditText;

	private boolean mIsEmailValid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = super.onCreateView(inflater, container, savedInstanceState);

		// We add all the users in the local user database (contacts)
		List<User> allContacts = null;
		try {
			allContacts = getHelper().getAllUsers();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		mContacts = new ArrayList<User>();
		for(User user:allContacts){
			mContacts.add(user);
		}

		UserAdapter userAdapter = new UserAdapter(mContacts);

		// We populate the list of contacts not yet added to the project
		mContactsList = (ListView) v.findViewById(R.id.mc_contacts_listView);
		mContactsList.setAdapter(userAdapter);
		mContactsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//TODO Do something when clicking a user in the list
			}
		});

		// Setting the OnClickListener for the add contact view
		mAddContactView = v.findViewById(R.id.mc_add_contact_view);
		mAddContactView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// Setting invalid email by default
				mIsEmailValid = false;
				
				// Opening add contacts dialog
				final AddContactsDialog addContactDialog = new AddContactsDialog(getActivity()) {
					@Override
					public int getLinearLayoutView() {
						return R.layout.dialog_add_contacts;
					}
				};

				// Getting the email address
				mEmailEditText = (EditText) addContactDialog.findViewById(R.id.ac_email_editText);
				mEmailEditText.addTextChangedListener(new EmailValidator(mEmailEditText,  true, R.drawable.shape_bordered_rectangle) {
					@Override
					public void onValidationAction(boolean isValid) {
						mIsEmailValid = isValid;
						updateActionButton();
					}
				});

				// Creating the OnClickListener for the send button
				mSearchButton = (Button) addContactDialog.findViewById(R.id.ac_search_button);
				mSearchButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						View emailView = addContactDialog.findViewById(R.id.ac_email_view);
						View successView = addContactDialog.findViewById(R.id.ac_add_success_view);
						View notFoundView = addContactDialog.findViewById(R.id.ac_not_found_view);

						// Calling the add contact task
						addContact(mEmailEditText.getText().toString(), emailView, successView, notFoundView, getCurrentFragment());
					}
				});
				updateActionButton();

				// Creating the OnClickListener for the invite button
				mInviteButton = (Button) addContactDialog.findViewById(R.id.ac_invite_button);
				mInviteButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						View notFoundView = addContactDialog.findViewById(R.id.ac_not_found_view);
						View successView = addContactDialog.findViewById(R.id.ac_invite_success_view);

						// Calling the invite service and update the dialog
						sendInvite(mEmailEditText.getText().toString(), notFoundView, successView);
					}
				});

				addContactDialog.show();
			}
		});

		// Setting a swipe refresh listener
		setSwipeRefresh((SwipeRefreshLayout) v.findViewById(R.id.mc_swipe_refresh));
		getSwipeRefresh().setOnRefreshListener(
				new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

						// Synchronizing all contacts
						syncContacts();
					}
				}
				);
		
		// Disabling DONE action
		setDoneActionGone();

		return v;
	}

	private void updateActionButton(){
		mSearchButton.setEnabled(mIsEmailValid);
	}

	private ManageContactsFragment getCurrentFragment(){
		return this;
	}

	private class UserAdapter extends ArrayAdapter<User>{

		public UserAdapter(List<User> users){
			super(getActivity(), 0, users);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// If we weren't given a view, inflate one
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_user, parent, false);
			}

			// Configure the view for this User
			User user = getItem(position);

			// Setting the user avatar
			ImageView userAvatarResource = (ImageView)convertView.findViewById(R.id.cp_user_avatar);
			TextView userInitialsTextView = (TextView)convertView.findViewById(R.id.cp_initials_textView);
			setUsetAvatarToImageView(userAvatarResource, userInitialsTextView, user, ImageUtils.IMAGE_QUALITY_MAX);

			// Setting the user full name
			TextView userFullName = (TextView)convertView.findViewById(R.id.cp_user_name);
			userFullName.setText(user.getFullName());

			// Setting the user e-mail address
			TextView userEmail = (TextView)convertView.findViewById(R.id.cp_user_email);
			userEmail.setText(user.getUsername());

			return convertView;
		}
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
		return R.layout.fragment_manage_contacts;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.mc_title;
	}

	@Override
	protected void doneAction() {
		getActivity().finish();
	}
}
