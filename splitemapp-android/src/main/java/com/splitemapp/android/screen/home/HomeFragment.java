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
import com.splitemapp.android.screen.settings.SettingsActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.widget.ConfirmationAlertDialog;
import com.splitemapp.commons.domain.User;

public class HomeFragment extends RestfulFragment {
	private static final String TAG = HomeFragment.class.getSimpleName();

	private User mCurrentUser;

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

	private View mLogoutButton;
	private View mManageContactsButton;
	private View mSynchronizeButton;
	private View mSettingsButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We inform that the activity hosting this fragment has an options menu
		setHasOptionsMenu(true);

		// We update the current user entity
		updateCurrentUser();
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
		mNavEmail.setText(mCurrentUser.getUsername());

		// We set the user avatar in the navigation view
		mNavAvatar = (ImageView) v.findViewById(R.id.h_nav_avatar_imageView);
		setUsetAvatarToImageView(mNavAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);

		// We populate the first name in the main view
		mMainFullName = (TextView) v.findViewById(R.id.h_main_full_name_textView);
		mMainFullName.setText(mCurrentUser.getFullName());

		// We populate the email in the main view
		mMainEmail = (TextView) v.findViewById(R.id.h_main_email_textView);
		mMainEmail.setText(mCurrentUser.getUsername());

		// We set the user avatar in the main view
		mMainAvatar = (ImageView) v.findViewById(R.id.h_main_avatar_imageView);
		setUsetAvatarToImageView(mMainAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);

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
		mLogoutButton = (View) v.findViewById(R.id.h_logout_textView);
		mLogoutButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// SHowing custom alert to let the user confirm action
				new ConfirmationAlertDialog(getContext()) {
					@Override
					public String getPositiveButtonText() {
						return getResources().getString(R.string.confirmation_positive_text);
					}
					@Override
					public String getNegativeButtonText() {
						return getResources().getString(R.string.confirmation_negative_text);
					}
					@Override
					public String getMessage() {
						return getResources().getString(R.string.confirmation_logout);
					}
					@Override
					public void executeOnPositiveAnswer() {
						logout();
					}
					@Override
					public void executeOnNegativeAnswer() {
						// We do nothing
					}
				}.show();
			}
		});

		// Setting the synchronize click listener
		mSynchronizeButton = (View) v.findViewById(R.id.h_synchronize_textView);
		mSynchronizeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// Closing the drawer
				mDrawerLayout.closeDrawers();

				// Pulling all tables
				syncAllTables();
			}
		});

		// Setting the settings click listener
		mSettingsButton = (View) v.findViewById(R.id.h_settings_textView);
		mSettingsButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// Closing the drawer
				mDrawerLayout.closeDrawers();

				// Open settings window
				startActivity( new Intent(getActivity(), SettingsActivity.class));
			}
		});

		// Setting the manage contacts click listener
		mManageContactsButton = (View) v.findViewById(R.id.h_manage_contacts_textView);
		mManageContactsButton.setOnClickListener(new OnClickListener(){
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

		return v;
	}

	/**
	 * Updates the current user instance
	 */
	private void updateCurrentUser(){
		// We get the user instance
		try {
			mCurrentUser = getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	/**
	 * Makes all necessary updates to this fragment
	 */
	private void updateFragment(){
		// Updating the RecyclerView
		mProjectsAdapter.updateRecycler(mProjectsRecycler);

		// Showing or hiding the empty list hint
		if(mProjectsAdapter.getItemCount() == 0){
			mEmptyListHintTextView.setVisibility(View.VISIBLE);
		} else {
			mEmptyListHintTextView.setVisibility(View.GONE);
		}

		// Updating the FullName
		updateCurrentUser();
		mMainFullName.setText(mCurrentUser.getFullName());
		mNavFullName.setText(mCurrentUser.getFullName());

		// Updating the Avatar
		setUsetAvatarToImageView(mMainAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);
		setUsetAvatarToImageView(mNavAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);
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
