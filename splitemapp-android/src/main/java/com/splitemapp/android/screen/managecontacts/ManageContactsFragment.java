package com.splitemapp.android.screen.managecontacts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.User;

public class ManageContactsFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = ManageContactsFragment.class.getSimpleName();

	private List<User> mContacts;
	private ListView mContactsList;

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
		
		return v;
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
			setUsetAvatarToImageView(userAvatarResource, user, ImageUtils.IMAGE_QUALITY_MAX);

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
