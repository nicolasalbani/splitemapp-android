package com.splitemapp.android.screen.createproject;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

	private BaseFragment baseFragment;
	private List<User> mUsers;

	// Provide a suitable constructor (depends on the kind of dataset)
	public UsersAdapter(BaseFragment baseFragment) {
		this.baseFragment = baseFragment;
		this.mUsers = Globals.getCreateProjectActivityUserList();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
		return new ViewHolder(mView);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		// Saving user object
		viewHolder.user = mUsers.get(position);
		
		// Gets element from the data set at this position
		// Replaces the contents of the view with that element
		viewHolder.mUserNameTextView.setText(mUsers.get(position).getFullName());
		
		viewHolder.mUserEmailTextView.setText(mUsers.get(position).getUsername());

		// Setting the project image cover
		baseFragment.setUsetAvatar(viewHolder.mUserAvatarImageView, mUsers.get(position), ImageUtils.IMAGE_QUALITY_MAX);
	}

	@Override
	public int getItemCount() {
		return mUsers.size();
	}

	/**
	 * Updates Recycler view adding any existing new items to the list
	 * @param project
	 */
	public void updateRecycler(){
		// Updating users list and refreshing adapter
		mUsers = Globals.getCreateProjectActivityUserList();
		notifyDataSetChanged();
	}

	/**
	 * Remove item from Recycler view
	 */
	public void removeItem(ViewHolder viewHolder){
		mUsers.remove(viewHolder.user);
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
		TextView mUserEmailTextView;

		public ViewHolder(View view) {
			super(view);
			// Getting instances for all surface items
			mUserAvatarImageView = (ImageView)view.findViewById(R.id.cp_user_avatar);
			mUserNameTextView = (TextView)view.findViewById(R.id.cp_user_name);
			mUserEmailTextView = (TextView)view.findViewById(R.id.cp_user_email);
		}

	}
}
