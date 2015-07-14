package com.splitemapp.android.screen.createlist;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragmentWithActionbar;
import com.splitemapp.android.utils.EconomicUtils;
import com.splitemapp.android.widget.CustomFloatingActionButton;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableFieldCod;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;

public class CreateListFragment extends BaseFragmentWithActionbar {

	private static final String TAG = CreateListFragment.class.getSimpleName();

	private User mCurrentUser;

	private EditText mListName;
	private Spinner mListType;
	private EditText mListBudget;
	private ListView mMembersList;
	private FloatingActionButton mFab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We initialize a new list
		Globals.setCreateListActivityUserList(new ArrayList<User>());

		// We get the user and user contact data instances
		try {
			mCurrentUser = getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// We only add the current user to the users list at first
		Globals.getCreateListActivityUserList().add(mCurrentUser);	

		Globals.setCreateListFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		// We get the list name field
		mListName = (EditText) v.findViewById(R.id.cl_list_name_editText);

		// We get the list budget field
		mListBudget = (EditText) v.findViewById(R.id.cl_budget_editText);

		// We get and populate the spinner
		mListType = (Spinner) v.findViewById(R.id.cl_list_type_spinner);
		try {
			Dao<ProjectType,Short> projectTypeDao = getHelper().getProjectTypeDao();
			List<ProjectType> projectTypes = projectTypeDao.queryForAll();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_spinner );
			for(ProjectType projectType:projectTypes){
				adapter.add(projectType.getCod());
			}
			mListType.setAdapter(adapter);
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// Setting the global create list user list to the user adapter
		UserAdapter userAdapter = new UserAdapter(Globals.getCreateListActivityUserList());

		// Populating the list of projects for this user
		mMembersList = (ListView) v.findViewById(R.id.cl_users_listView);
		mMembersList.setAdapter(userAdapter);

		// Adding action FABs to the main FAB
		mFab = (FloatingActionButton) v.findViewById(R.id.cl_fab);
		CustomFloatingActionButton customFloatingActionButton = new CustomFloatingActionButton(getActivity(), mFab);

		// Adding add contact FAB
		customFloatingActionButton.addActionFab(getActivity(), "Add contact", R.drawable.action_fab, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});

		// Adding add image cover FAB
		customFloatingActionButton.addActionFab(getActivity(), "Add cover image", R.drawable.action_fab, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});

		return v;
	}

	public static int dpToPx(Context context, float dp) {
		// Reference http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) ((dp * scale) + 0.5f);
	}

	public void createList(){
		try {
			// Getting the project active status
			Dao<ProjectStatus,Short> projectStatusDao = getHelper().getProjectStatusDao();
			ProjectStatus projectActiveStatus = projectStatusDao.queryForEq(TableField.ALTER_TABLE_COD, TableFieldCod.PROJECT_STATUS_ACTIVE).get(0);

			// Getting the project type
			Dao<ProjectType,Short> projectTypeDao = getHelper().getProjectTypeDao();
			ProjectType projectType = projectTypeDao.queryForEq(TableField.PROJECT_TYPE_COD, mListType.getSelectedItem()).get(0);

			// Saving the project in the database
			Project project = new Project();
			project.setBudget(new BigDecimal(mListBudget.getText().toString()));
			project.setProjectStatus(projectActiveStatus);
			project.setProjectType(projectType);
			project.setTitle(mListName.getText().toString());
			getHelper().getProjectDao().create(project);

			// Creating empty project image cover
			ProjectCoverImage projectCoverImage = new ProjectCoverImage();
			projectCoverImage.setProject(project);
			getHelper().getProjectCoverImageDao().create(projectCoverImage);

			// Getting the user to project active status
			Dao<UserToProjectStatus,Short> userToProjectStatusDao = getHelper().getUserToProjectStatusDao();
			UserToProjectStatus userToProjectActiveStatus = userToProjectStatusDao.queryForEq(TableField.ALTER_TABLE_COD, TableFieldCod.USER_TO_PROJECT_STATUS_ACTIVE).get(0);

			// Saving user to project relationships
			for(User user:Globals.getCreateListActivityUserList()){
				UserToProject userToProject = new UserToProject();
				userToProject.setUserToProjectStatus(userToProjectActiveStatus);
				userToProject.setProject(project);
				userToProject.setUser(user);
				userToProject.setExpensesShare(EconomicUtils.calulateShare(Globals.getCreateListActivityUserList().size()));
				getHelper().getUserToProjectDao().create(userToProject);
			}

			// Resetting the global create list user list
			Globals.setCreateListActivityUserList(new ArrayList<User>());

			// Moving back to the home screen
			getActivity().finish();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	private class UserAdapter extends ArrayAdapter<User>{

		public UserAdapter(List<User> users){
			super(getActivity(), 0, users);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//If we weren't given a view, inflate one
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_user, parent, false);
			}

			//Configure the view for this User
			User user = getItem(position);

			// Setting the user name
			TextView userName = (TextView)convertView.findViewById(R.id.cl_user_name);
			userName.setText(user.getFirstName() + " " + user.getLastName());

			// Getting the existing user contact data from the user
			UserContactData userContactData = null;
			Set<UserContactData> userContactDatas = user.getUserContactDatas();
			for(UserContactData ucd:userContactDatas){
				userContactData = ucd;
			}

			// Setting the user email
			TextView userEmail = (TextView)convertView.findViewById(R.id.cl_user_email);
			userEmail.setText(userContactData.getContactData());

			//Setting the user avatar
			ImageView userAvatar = (ImageView)convertView.findViewById(R.id.cl_user_avatar);
			setUsetAvatar(userAvatar, user, 40);

			return convertView;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Refreshing member list when coming back from the Add People fragment
		((BaseAdapter) mMembersList.getAdapter()).notifyDataSetChanged(); 
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_create_list;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.cl_title;
	}

	@Override
	protected void doneAction() {
		createList();
	}

}
