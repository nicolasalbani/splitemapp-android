package com.splitemapp.android.screen.home;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.dto.request.PullAllSyncRequest;
import com.splitemapp.commons.domain.dto.response.PullAllSyncResponse;
import com.splitemapp.commons.utils.Utils;

public class HomeFragment extends BaseFragment {

	public static final String EXTRA_USER_ID = "com.splitemapp.android.user_id";

	private static final String TAG = HomeFragment.class.getSimpleName();

	private List<Project> mProjects;
	private User mUser;
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
		
		// We get the user and user contact data instances
		Long userId = (Long)arguments.getSerializable(EXTRA_USER_ID);
		mUser = getLoggedUser(userId);
		mUserContactData = getLoggedUserCD(mUser);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_home, container, false);

		// We populate the first name
		mFirstName = (TextView) v.findViewById(R.id.h_first_name_textView);
		mFirstName.setText(mUser.getFirstName());

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

		// We get the reference to the add new list button and implement a OnClickListener
		mAddNewList = (Button) v.findViewById(R.id.h_new_list_button);
		mAddNewList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new PullAllSyncTask().execute();
			}
		});

		return v;
	}

	private UserContactData getLoggedUserCD(User loggedUser){
		UserContactData userContactData = null;

		try {
			Dao<UserContactData, Integer> userContactDataDao = getHelper().getUserContactDataDao();
			for(UserContactData ucd:userContactDataDao){
				if(ucd.getUser().getId().equals(loggedUser.getId())){
					userContactData = ucd;
				}
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		return userContactData;
	}

	private User getLoggedUser(Long userId){
		User user = null;
		try {
			Dao<User,Integer> userDao = getHelper().getUserDao();
			for(User u:userDao){
				if(userId.equals(u.getId())){
					user = u;
				}
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		return user;
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

	private class PullAllSyncTask extends AsyncTask<Void, Void, PullAllSyncResponse> {

		@Override
		protected PullAllSyncResponse doInBackground(Void... params) {
			try {
				// We create the login request
				PullAllSyncRequest pullAllSyncRequest = new PullAllSyncRequest();
				pullAllSyncRequest.setLastPullSuccessAt(Utils.dateToString(new Date(100),ServiceConstants.DATE_FORMAT));
				UserSession userSession = getHelper().getUserSessionDao().queryForAll().get(0);
				pullAllSyncRequest.setToken(userSession.getToken());

				// We call the rest service and send back the login response
				return callRestService(ServiceConstants.PULL_ALL_SYNC_PATH, pullAllSyncRequest, PullAllSyncResponse.class);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(PullAllSyncResponse pullAllSyncResponse) {
			boolean loginSuccess = false;

			// We validate the response
			if(pullAllSyncResponse != null){
				loginSuccess = pullAllSyncResponse.getSuccess();
			}

			// We show the status toast
			showToast(loginSuccess ? "PullAllSync Successful!" : "PullAllSync Failed!");

			// We save the user and session information returned by the backend
			if(loginSuccess){
			}
		}
	}

}
