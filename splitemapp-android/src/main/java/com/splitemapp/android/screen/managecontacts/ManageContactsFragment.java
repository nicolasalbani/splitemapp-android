package com.splitemapp.android.screen.managecontacts;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.User;

public class ManageContactsFragment extends RestfulFragment {

	private static final String TAG = ManageContactsFragment.class.getSimpleName();

	private List<User> mContacts;
	private User mCurrentUser;
	private Button mSynchronizeContacts;
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

		View v = inflater.inflate(R.layout.fragment_manage_contacts, container, false);

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
		mContactsList = (ListView) v.findViewById(R.id.mc_contacts_listView);
		mContactsList.setAdapter(userAdapter);
		mContactsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//TODO Do something when clicking a user in the list
			}
		});

		// We get the reference to the synchronize contacts button and implement a OnClickListener
		mSynchronizeContacts = (Button) v.findViewById(R.id.mc_synchronize_contacts_button);
		mSynchronizeContacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				List<String> contactsEmailAddressList = getContactsEmailAddressList();
				synchronizeContacts(contactsEmailAddressList);
				
				// Refreshing contacts list after making the sync
				((BaseAdapter) mContactsList.getAdapter()).notifyDataSetChanged(); 
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_manage_contact, parent, false);
			}

			//Configure the view for this User
			User user = getItem(position);

			//Setting the user avatar
			ImageView userAvatar = (ImageView)convertView.findViewById(R.id.mc_user_avatar);
			byte[] avatar = user.getAvatar();
			if(avatar != null){
				userAvatar.setImageBitmap(ImageUtils.getCroppedBitmap(ImageUtils.byteArrayToBitmap(avatar,10)));
			} else {
				userAvatar.setImageResource(R.drawable.avatar_placeholder);
			}

			//Setting the user first name
			TextView userFirstName = (TextView)convertView.findViewById(R.id.mc_user_first_name);
			userFirstName.setText(user.getFirstName());

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
}
