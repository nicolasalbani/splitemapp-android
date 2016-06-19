package com.splitemapp.android.screen.projectcontacts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.commons.domain.User;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
	
	private static final String TAG = ContactsAdapter.class.getSimpleName();

	private BaseFragment baseFragment;
	private User mCurrentUser;
	List<User> mAllContacts;
	List<User> mListContacts;

	// Provide a suitable constructor (depends on the kind of dataset)
	public ContactsAdapter(BaseFragment baseFragment, List<User> listContacts) {
		this.baseFragment = baseFragment;
		
		// We save the reference to the list of Contacts
		mListContacts = listContacts;
		
		// We add all the users in the local user database (contacts)
		mAllContacts = new ArrayList<User>();
		try {
			mAllContacts = baseFragment.getHelper().getAllUsers();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		
		// We get the user and user contact data instances
		try {
			mCurrentUser = baseFragment.getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contact, parent, false);
		return new ViewHolder(mView);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		// Saving user object
		viewHolder.user = mAllContacts.get(position);

		// Gets element from the data set at this position
		// Replaces the contents of the view with that element
		viewHolder.mUserNameTextView.setText(mAllContacts.get(position).getFullName());

		//Setting the user status icon
		updateUserStatusIcon(viewHolder.mUserAvatarImageView, mAllContacts.get(position));
		
		// Setting OnClickListener to the user row
		viewHolder.mParentView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				User user = mAllContacts.get(position);
				
				//Only users different to current user can be added or removed
				if(!isCurrentUser(user)){
					if(isUserInList(user)){
						removeUserFromList(viewHolder.mUserAvatarImageView, user);
					} else {
						addUserToList(viewHolder.mUserAvatarImageView, user);
					}
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return mAllContacts.size();
	}

	/**
	 * Updates Recycler view adding any existing new items to the list
	 * @param project
	 */
	public void updateRecycler(){
		// Updating users list and refreshing adapter
		notifyDataSetChanged();
	}

	/**
	 * Remove item from Recycler view
	 */
	public void removeItem(ViewHolder viewHolder){
		mAllContacts.remove(viewHolder.user);
		notifyItemRemoved(viewHolder.getAdapterPosition());
	}

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {
		// Holding project object
		User user;

		// Declaring all the items in the surface view
		ImageView mUserAvatarImageView;
		TextView mUserNameTextView;
		RelativeLayout mParentView;

		public ViewHolder(View view) {
			super(view);
			// Getting instances for all surface items
			mUserAvatarImageView = (ImageView)view.findViewById(R.id.pc_user_status_icon);
			mUserNameTextView = (TextView)view.findViewById(R.id.pc_user_full_name);
			mParentView = (RelativeLayout)view.findViewById(R.id.ap_parentView);
		}

	}
	
	private boolean isCurrentUser(User user){
		return user.getId() == mCurrentUser.getId();
	}

	private void updateUserStatusIcon(ImageView userStatusIcon, User user){
		if(mListContacts.contains(user)){
			userStatusIcon.setImageResource(R.drawable.ic_checkbox_marked_circle_48dp);
		} else {
			if(baseFragment.isUserHasAvatar(user)){
				baseFragment.setUsetAvatarToImageView(userStatusIcon, user, 40);
			} else {
				userStatusIcon.setImageResource(R.drawable.ic_checkbox_blank_circle_outline_48dp);
			}
		}
	}

	private boolean isUserInList(User user){
		return mListContacts.contains(user);
	}

	private void addUserToList(ImageView view, User user){
		// Adding the user to the list
		mListContacts.add(user);

		// Updating the status icon as active
		updateUserStatusIcon(view, user);
	}

	private void removeUserFromList(ImageView view, User user){
		// Removing the user from the list
		mListContacts.remove(user);

		// Updating the status icon as active
		updateUserStatusIcon(view, user);
	}
}
