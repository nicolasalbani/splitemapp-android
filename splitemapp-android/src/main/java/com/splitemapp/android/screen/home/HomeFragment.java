package com.splitemapp.android.screen.home;

import java.sql.SQLException;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.createproject.CreateProjectActivity;
import com.splitemapp.android.screen.managecontacts.ManageContactsActivity;
import com.splitemapp.android.screen.welcome.WelcomeActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;

public class HomeFragment extends RestfulFragment {
	private static final String TAG = HomeFragment.class.getSimpleName();

	private User mCurrentUser;
	private UserContactData mUserContactData;

	private DrawerLayout mDrawerLayout;
	
	private ImageView mNavAvatar;
	private TextView mNavFullName;
	private TextView mNavEmail;
	private ImageView mMainAvatar;
	private TextView mMainFullName;
	private TextView mMainEmail;
	private FloatingActionButton mFab;

	private RecyclerView mProjectsRecycler;
	private SwipeProjectsAdapter mProjectsAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private TextView mEmptyListHintTextView;

	private TextView mLogoutTextView;
	private TextView mManageContactsTextView;
	private TextView mSynchronizeTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We inform that the activity hosting this fragment has an options menu
		setHasOptionsMenu(true);

		// We get the user and user contact data instances
		try {
			mCurrentUser = getHelper().getLoggedUser();
			mUserContactData = getHelper().getLoggedUserContactData();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_home, container, false);

		// We populate the drawer layout
		mDrawerLayout = (DrawerLayout) v.findViewById(R.id.h_drawerLayout);
		
		// We populate the first name in the navigation view
		mNavFullName = (TextView) v.findViewById(R.id.h_nav_full_name_textView);
		mNavFullName.setText(mCurrentUser.getFullName());

		// We populate the email in the navigation view
		mNavEmail = (TextView) v.findViewById(R.id.h_nav_email_textView);
		mNavEmail.setText(mUserContactData.getContactData());

		// We set the user avatar in the navigation view
		mNavAvatar = (ImageView) v.findViewById(R.id.h_nav_avatar_imageView);
		setUsetAvatar(mNavAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);

		// We populate the first name in the main view
		mMainFullName = (TextView) v.findViewById(R.id.h_main_full_name_textView);
		mMainFullName.setText(mCurrentUser.getFullName());

		// We populate the email in the main view
		mMainEmail = (TextView) v.findViewById(R.id.h_main_email_textView);
		mMainEmail.setText(mUserContactData.getContactData());

		// We set the user avatar in the main view
		mMainAvatar = (ImageView) v.findViewById(R.id.h_main_avatar_imageView);
		setUsetAvatar(mMainAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);

		// Creating a projects adapter to be used in the recycler view
		mProjectsAdapter = new SwipeProjectsAdapter(this);

		// We populate the list of projects for this user
		mProjectsRecycler = (RecyclerView) v.findViewById(R.id.h_projects_recyclerView);
		mProjectsRecycler.setAdapter(mProjectsAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mProjectsRecycler.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mProjectsRecycler.setLayoutManager(mLayoutManager);

		// Setting the default animator for the view
		mProjectsRecycler.setItemAnimator(new CustomItemAnimator());

		// Getting the hint if project list is empty
		mEmptyListHintTextView = (TextView) v.findViewById(R.id.h_empty_list_hint_textView);

		// Adding action FABs to the main FAB
		mFab = (FloatingActionButton) v.findViewById(R.id.h_fab);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// We move to the project creation screen
				Intent intent = new Intent(getActivity(), CreateProjectActivity.class);
				startActivity(intent);
			}
		});

		// Setting the logout click listener
		mLogoutTextView = (TextView) v.findViewById(R.id.h_logout_textView);
		mLogoutTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// We delete all user sessions
				try {
					getHelper().deleteAllUserSessions();
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught!", e);
				}
				// We move to the welcome screen
				startActivity(new Intent(getActivity(), WelcomeActivity.class));
			}
		});

		// Setting the synchronize click listener
		mSynchronizeTextView = (TextView) v.findViewById(R.id.h_synchronize_textView);
		mSynchronizeTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// Closing the drawer
				mDrawerLayout.closeDrawers();
				
				// Pulling all tables
				syncAllTables();
			}
		});

		// Setting the manage contacts click listener
		mManageContactsTextView = (TextView) v.findViewById(R.id.h_manage_contacts_textView);
		mManageContactsTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// Closing the drawer
				mDrawerLayout.closeDrawers();
				
				// We move to the login screen
				startActivity( new Intent(getActivity(), ManageContactsActivity.class));
			}
		});

		// Setting a swipe refresh listener
		setSwipeRefresh((SwipeRefreshLayout) v.findViewById(R.id.h_swipe_refresh));
		getSwipeRefresh().setOnRefreshListener(
				new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

						// Synchronizing all tables
						syncAllTables();
					}


				}
				);

		// If this user never synchronized before, we initialize the SyncStatus table
		try {
			if(!getHelper().isSyncInitialized()){
				syncAllTablesFirstTime();
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		return v;
	}
	
	/**
	 * Makes all necessary updates to this fragment
	 */
	private void updateFragment(){
		// Updating the RecyclerView
		mProjectsAdapter.updateRecycler();
		
		// Showing or hiding the empty list hint
		if(mProjectsAdapter.getItemCount() == 0){
			mEmptyListHintTextView.setVisibility(View.VISIBLE);
		} else {
			mEmptyListHintTextView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onRefresh(String response) {
		updateFragment();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateFragment();
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
