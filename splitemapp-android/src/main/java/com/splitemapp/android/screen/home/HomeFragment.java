package com.splitemapp.android.screen.home;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.createlist.CreateListActivity;
import com.splitemapp.android.screen.login.LoginActivity;
import com.splitemapp.android.screen.project.ProjectActivity;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;

public class HomeFragment extends BaseFragment {
	private static final String TAG = HomeFragment.class.getSimpleName();

	private List<Project> mProjects;
	private User mCurrentUser;
	private UserContactData mUserContactData;

	private ImageView mAvatar;
	private TextView mFirstName;
	private TextView mEmail;
	private ListView mProjectsList;
	private Button mAddNewList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getActivity().getIntent().getExtras();
		
		// We inform that the activity hosting this fragment has an options menu
		setHasOptionsMenu(true);

		// We get the user and user contact data instances
		Long userId = (Long)arguments.getSerializable(Constants.EXTRA_USER_ID);
		mCurrentUser = getUserById(userId);
		mUserContactData = getUserContactData(userId);
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
		//TODO set this to the actual user avatar!!
		mAvatar.setImageResource(R.drawable.avatar_placeholder);

		// We get the list of existing projects and create the project list adapter
		try {
			mProjects = getHelper().getProjectDao().queryForAll();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		ProjectAdapter projectAdapter = new ProjectAdapter(mProjects);

		// We populate the list of projects for this user
		mProjectsList = (ListView) v.findViewById(R.id.h_projects_listView);
		mProjectsList.setAdapter(projectAdapter);
		mProjectsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// We create an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(getActivity(), ProjectActivity.class);
				intent.putExtra(Constants.EXTRA_USER_ID, mCurrentUser.getId());
				intent.putExtra(Constants.EXTRA_PROJECT_ID, mProjects.get(position).getId());
				startActivity(intent);
			}
		});

		// We get the reference to the add new list button and implement a OnClickListener
		mAddNewList = (Button) v.findViewById(R.id.h_new_list_button);
		mAddNewList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// We move to the project creation screen
				Intent intent = new Intent(getActivity(), CreateListActivity.class);
				intent.putExtra(Constants.EXTRA_USER_ID, mCurrentUser.getId());
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
		// Handle item selection
		switch (item.getItemId()){
		case R.id.h_logout : 
			// We delete all user sessions
			deleteAllUserSessions();
			// We move to the login screen
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class ProjectAdapter extends ArrayAdapter<Project>{

		public ProjectAdapter(List<Project> projects){
			super(getActivity(), 0, projects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//If we weren't given a view, inflate one
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_project, parent, false);
			}

			//Configure the view for this Project
			Project project = getItem(position);

			TextView projectTitleTextView = (TextView)convertView.findViewById(R.id.h_project_title);
			projectTitleTextView.setText(project.getTitle());

			return convertView;
		}
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
