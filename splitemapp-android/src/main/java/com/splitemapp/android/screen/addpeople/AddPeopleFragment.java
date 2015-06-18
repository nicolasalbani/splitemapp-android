package com.splitemapp.android.screen.addpeople;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragmentWithActionbar;
import com.splitemapp.commons.domain.User;

public class AddPeopleFragment extends BaseFragmentWithActionbar {

	private static final String TAG = AddPeopleFragment.class.getSimpleName();

	private List<User> mContacts;
	private User mCurrentUser;

	private ListView mContactsList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We get the user and user contact data instances
		try {
			mCurrentUser = getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		// We add all the users in the local user database (contacts)
		List<User> allContacts = null;
		try {
			allContacts = getHelper().getUserDao().queryForAll();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		mContacts = new ArrayList<User>();
		for(User user:allContacts){
			mContacts.add(user);
		}

		UserAdapter userAdapter = new UserAdapter(mContacts);

		// We populate the list of contacts not yet added to the project
		mContactsList = (ListView) v.findViewById(R.id.ap_contacts_listView);
		mContactsList.setAdapter(userAdapter);
		mContactsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//If we weren't given a view, inflate one
				if (view == null){
					view = getActivity().getLayoutInflater().inflate(R.layout.list_item_contact, parent, false);
				}

				User user = mContacts.get(position);

				//Only users different to current user can be added or removed
				if(!isCurrentUser(user)){
					//Setting the user status icon based on the previous status icon
					ImageView userStatusIcon = (ImageView)view.findViewById(R.id.ap_user_status_icon);
					if(Globals.getCreateListActivityUserList().contains(user)){
						userStatusIcon.setImageResource(R.drawable.contact_status_inactive);
						Globals.getCreateListActivityUserList().remove(user);
					} else {
						userStatusIcon.setImageResource(R.drawable.contact_status_active);
						Globals.getCreateListActivityUserList().add(user);
					}
				}
			}
		});

		return v;
	}

	private class UserAdapter extends ArrayAdapter<User>{

		public UserAdapter(List<User> users){
			super(getActivity(), 0, users);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//If we weren't given a view, inflate one
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_contact, parent, false);
			}

			//Configure the view for this User
			User user = getItem(position);

			//Setting the user avatar
			ImageView userAvatar = (ImageView)convertView.findViewById(R.id.ap_user_avatar);
			setUsetAvatar(userAvatar, user, 40);

			//Setting the user first name
			TextView userFirstName = (TextView)convertView.findViewById(R.id.ap_user_first_name);
			userFirstName.setText(user.getFirstName());

			//Setting the user status icon
			ImageView userStatusIcon = (ImageView)convertView.findViewById(R.id.ap_user_status_icon);
			if(Globals.getCreateListActivityUserList().contains(user)){
				userStatusIcon.setImageResource(R.drawable.contact_status_active);
			} else {
				userStatusIcon.setImageResource(R.drawable.contact_status_inactive);
			}

			return convertView;
		}
	}

	private boolean isCurrentUser(User user){
		return user.getId() == mCurrentUser.getId();
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_add_people;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.ap_title;
	}

	@Override
	protected void doneAction() {
		getActivity().finish();
	}
}
