package com.splitemapp.android.screen.createlist;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.android.screen.home.HomeFragment;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableFieldCod;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;

public class CreateListFragment extends BaseFragment {

	public static final String EXTRA_USER_ID = "com.splitemapp.android.user_id";

	private static final String TAG = CreateListFragment.class.getSimpleName();

	private List<User> mUsers;
	private User mCurrentUser;

	private EditText mListName;
	private Spinner mListType;
	private EditText mListBudget;
	private ListView mMembersList;
	private Button mCreateList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getActivity().getIntent().getExtras();

		// We get the user and user contact data instances
		Long userId = (Long)arguments.getSerializable(EXTRA_USER_ID);
		mCurrentUser = getCurrentUser(userId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_create_list, container, false);

		// We get the list name field
		mListName = (EditText) v.findViewById(R.id.cl_list_name_editText);

		// We get the list budget field
		mListBudget = (EditText) v.findViewById(R.id.cl_budget_editText);
		
		// We get and populate the spinner
		mListType = (Spinner) v.findViewById(R.id.cl_list_type_spinner);
		try {
			Dao<ProjectType,Integer> projectTypeDao = getHelper().getProjectTypeDao();
			List<ProjectType> projectTypes = projectTypeDao.queryForAll();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_spinner );
			for(ProjectType projectType:projectTypes){
				adapter.add(projectType.getCod());
			}
			mListType.setAdapter(adapter);
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// We only add the current user to the users list at first
		mUsers = new ArrayList<User>();
		mUsers.add(mCurrentUser);

		UserAdapter userAdapter = new UserAdapter(mUsers);

		// We populate the list of projects for this user
		mMembersList = (ListView) v.findViewById(R.id.cl_users_listView);
		mMembersList.setAdapter(userAdapter);

		// We get the reference to the add new list button and implement a OnClickListener
		mCreateList = (Button) v.findViewById(R.id.cl_new_list_button);
		mCreateList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					// We get the project active status
					Dao<ProjectStatus,Integer> projectStatusDao = getHelper().getProjectStatusDao();
					ProjectStatus projectActiveStatus = projectStatusDao.queryForEq(TableField.ALTER_TABLE_COD, TableFieldCod.PROJECT_STATUS_ACTIVE).get(0);
					
					// We get the project type
					Dao<ProjectType,Integer> projectTypeDao = getHelper().getProjectTypeDao();
					ProjectType projectType = projectTypeDao.queryForEq(TableField.PROJECT_TYPE_COD, mListType.getSelectedItem()).get(0);
					
					// We create the project instance
					Project project = new Project();
					project.setBudget(new BigDecimal(mListBudget.getText().toString()));
					project.setProjectStatus(projectActiveStatus);
					project.setProjectType(projectType);
					project.setTitle(mListName.getText().toString());
					
					// We save the project in the database
					Dao<Project,Integer> projectDao = getHelper().getProjectDao();
					projectDao.create(project);
					
					// We move back to the home screen
					Intent intent = new Intent(getActivity(), HomeActivity.class);
					intent.putExtra(HomeFragment.EXTRA_USER_ID, mCurrentUser.getId());
					startActivity(intent);
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught!", e);
				}
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_user, parent, false);
			}

			//Configure the view for this User
			User user = getItem(position);

			TextView userFirstName = (TextView)convertView.findViewById(R.id.cl_user_first_name);
			userFirstName.setText(user.getFirstName());

			return convertView;
		}
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

}
