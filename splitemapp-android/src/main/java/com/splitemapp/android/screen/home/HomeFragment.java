package com.splitemapp.android.screen.home;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.screen.SynchronizerFragment;
import com.splitemapp.android.screen.createlist.CreateListActivity;
import com.splitemapp.android.screen.managecontacts.ManageContactsActivity;
import com.splitemapp.android.screen.welcome.WelcomeActivity;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;

public class HomeFragment extends SynchronizerFragment {
	private static final String TAG = HomeFragment.class.getSimpleName();

	private User mCurrentUser;
	private UserContactData mUserContactData;

	private ImageView mAvatar;
	private TextView mFirstName;
	private TextView mEmail;
	private Button mAddNewList;

	private RecyclerView mProjectsRecycler;
	private ProjectsAdapter mProjectsAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

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

		// We populate the first name
		mFirstName = (TextView) v.findViewById(R.id.h_first_name_textView);
		mFirstName.setText(mCurrentUser.getFirstName());

		// We populate the email
		mEmail = (TextView) v.findViewById(R.id.h_email_textView);
		mEmail.setText(mUserContactData.getContactData());

		// We set the user avatar
		mAvatar = (ImageView) v.findViewById(R.id.h_avatar_imageView);
		setUsetAvatar(mAvatar, mCurrentUser, 100);

		// Creating a projects adapter to be used in the recycler view
		mProjectsAdapter = new ProjectsAdapter(getProjectsList());

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

		// We get the reference to the add new list button and implement a OnClickListener
		mAddNewList = (Button) v.findViewById(R.id.h_new_list_button);
		mAddNewList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// We move to the project creation screen
				Intent intent = new Intent(getActivity(), CreateListActivity.class);
				startActivity(intent);
			}
		});

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_home, menu);
		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent nextIntent = null;
		// Handle item selection
		switch (item.getItemId()){
		case R.id.h_logout : 
			// We delete all user sessions
			try {
				getHelper().deleteAllUserSessions();
			} catch (SQLException e) {
				Log.e(TAG, "SQLException caught!", e);
			}
			// We move to the login screen
			nextIntent = new Intent(getActivity(), WelcomeActivity.class);
			startActivity(nextIntent);
			return true;
		case R.id.h_synchronize : 
			// TODO we need to call the new sync intent
			pushProjects();
			//			pullUsers();
			//			pullUserContactDatas();
			//			pullProjects();
			//			pullUserToProjects();
			//			pullGroups();
			//			pullUserToGroups();
			//			pullUserInvites();
			//			pullUserExpenses();

			// We reload the view
			return true;
		case R.id.h_manage_contacts :
			// We move to the login screen
			nextIntent = new Intent(getActivity(), ManageContactsActivity.class);
			startActivity(nextIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Refreshing project list when coming back from the Create List fragment
		for(Project project:getProjectsList()){
			mProjectsAdapter.addItem(project);
		}
	}

	/**
	 * Returns the whole projects list for this user
	 * @return
	 */
	private List<Project> getProjectsList(){
		List<Project> projectList = null;

		try {
			projectList = getHelper().getAllProjectsForLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		return projectList;
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
