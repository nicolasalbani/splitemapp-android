package com.splitemapp.android.screen.createproject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.screen.balance.ProjectTypeMapper;
import com.splitemapp.android.screen.projectcontacts.ProjectContactsActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.widget.CustomFloatingActionButton;
import com.splitemapp.android.widget.DecimalDigitsInputFilter;
import com.splitemapp.commons.constants.TableFieldCod;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;

public class CreateProjectFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = CreateProjectFragment.class.getSimpleName();

	private User mCurrentUser;
	private byte[] mAvatarData;

	private EditText mProjectTitle;
	private Spinner mProjectType;
	private EditText mProjectBudget;
	private DecimalFormat mProjectBudgetFormat;
	private FloatingActionButton mFab;

	private RecyclerView mMembersRecycler;
	private ContactsAdapter mUsersAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private AppBarLayout appBarLayout;
	boolean showingProjectName = false;

	private Project mProjectToEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We initialize a new user list
		Globals.setCreateProjectActivityUserList(new ArrayList<User>());

		// We get the user and user contact data instances
		try {
			mCurrentUser = getHelper().getLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// Setting the project budget format
		mProjectBudgetFormat = new DecimalFormat();
		mProjectBudgetFormat.setMaximumFractionDigits(Constants.MAX_DIGITS_AFTER_DECIMAL);
		mProjectBudgetFormat.setMinimumFractionDigits(Constants.MAX_DIGITS_AFTER_DECIMAL);

		if(isNewProject()){
			// We only add the current user to the users list at first
			Globals.getCreateProjectActivityUserList().add(mCurrentUser);	
		} else {
			try {
				// Saving the project instance to edit
				mProjectToEdit = getHelper().getProject(Globals.getCreateProjectActivityProjectId());

				// Getting the user list associated to that project
				List<User> activeUsersByProjectId = getHelper().getActiveUsersByProjectId(Globals.getCreateProjectActivityProjectId());
				for(User user:activeUsersByProjectId){
					Globals.getCreateProjectActivityUserList().add(user);
				}
			} catch (SQLException e) {
				Log.e(TAG, "SQLException caught!", e);
			}
		}

		Globals.setCreateProjectFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		// We get the project name field
		mProjectTitle = (EditText) v.findViewById(R.id.cp_project_name_editText);

		// We get the project budget field
		mProjectBudget = (EditText) v.findViewById(R.id.cp_budget_editText);
		mProjectBudget.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(Constants.MAX_DIGITS_BEFORE_DECIMAL,Constants.MAX_DIGITS_AFTER_DECIMAL)});

		// We get and populate the spinner
		mProjectType = (Spinner) v.findViewById(R.id.cp_project_type_spinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_spinner );
		try {
			List<ProjectType> projectTypes = getHelper().getAllProjectTypes();
			for(ProjectType projectType:projectTypes){
				spinnerAdapter.add(ProjectTypeMapper.getString(getContext(), projectType.getCod()));
			}
			mProjectType.setAdapter(spinnerAdapter);
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// If we are editing this project, we populate all fields
		if(!isNewProject()){
			mProjectTitle.setText(mProjectToEdit.getTitle());
			mProjectBudget.setText(mProjectBudgetFormat.format(mProjectToEdit.getBudget()));
			String projectTypeString = ProjectTypeMapper.getString(getContext(), mProjectToEdit.getProjectType().getCod());
			int position = spinnerAdapter.getPosition(projectTypeString);
			mProjectType.setSelection(position);
		}

		// Setting the global create list user list to the user adapter
		mUsersAdapter = new ContactsAdapter(this);

		// Populating the list of members for this project
		mMembersRecycler = (RecyclerView) v.findViewById(R.id.cp_users_recyclerView);
		mMembersRecycler.setAdapter(mUsersAdapter);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mMembersRecycler.setLayoutManager(mLayoutManager);

		// We enable showing the title in this particular screen
		appBarLayout = (AppBarLayout) v.findViewById(R.id.cp_appBarLayout);
		appBarLayout.addOnOffsetChangedListener(new OnOffsetChangedListener(){
			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
				if(verticalOffset < -120 && !showingProjectName){
					mCancel.setText(mProjectTitle.getText().toString());
					showingProjectName = true;
				} else if (verticalOffset >= -120 && showingProjectName) {
					mCancel.setText(getTitleResourceId());
					showingProjectName = false;
				}
				Log.i(TAG, "onOffsetChanged: " +verticalOffset);
			}});

		// Adding action FABs to the main FAB
		mFab = (FloatingActionButton) v.findViewById(R.id.cp_fab);
		CustomFloatingActionButton customFloatingActionButton = new CustomFloatingActionButton(getActivity(), mFab);

		// Adding add contact FAB
		customFloatingActionButton.addActionFab(getActivity(), "Contacts", R.drawable.action_fab_contacts, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Opening add people screen
				Intent intent = new Intent(getActivity(), ProjectContactsActivity.class);
				startActivity(intent);
			}
		});

		// Adding add image cover FAB
		customFloatingActionButton.addActionFab(getActivity(), "Cover image", R.drawable.action_fab_camera, new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Opening image selector
				openImageSelector(getProjectCoverImageWidth(), getProjectCoverImageHeight());
			}
		});

		return v;
	}

	@Override
	public void executeOnImageSelection(Bitmap selectedBitmap) {
		mAvatarData = ImageUtils.bitmapToByteArray(selectedBitmap, ImageUtils.IMAGE_QUALITY_MAX);
	}

	private void createProject(){
		try {
			// Getting the project active status
			ProjectStatus projectActiveStatus = getHelper().getProjectStatus(TableFieldCod.PROJECT_STATUS_ACTIVE);

			// Getting the project type
			ProjectType projectType = getHelper().getProjectType(ProjectTypeMapper.getCod(getContext(), mProjectType.getSelectedItem().toString()));

			// Saving the project in the database
			Project project = null;
			project = new Project();
			project.setBudget(new BigDecimal(mProjectBudget.getText().toString()));
			project.setProjectStatus(projectActiveStatus);
			project.setProjectType(projectType);
			project.setTitle(mProjectTitle.getText().toString());
			getHelper().persistProject(project);

			// Creating project image cover
			ProjectCoverImage projectCoverImage = new ProjectCoverImage();
			projectCoverImage = new ProjectCoverImage();
			projectCoverImage.setProject(project);
			if(mAvatarData != null){
				projectCoverImage.setAvatarData(mAvatarData);
			}
			getHelper().persistProjectCoverImage(projectCoverImage);

			// Saving user to project relationships
			getHelper().updateProjectContacts(project, Globals.getCreateProjectActivityUserList());

			// Pushing the changes
			pushProjects();
			pushProjectCoverImages();
			pushUserToProjects();

			// Resetting the global create project - user list
			Globals.setCreateProjectActivityUserList(new ArrayList<User>());

			// Resetting the global create project - project id
			Globals.setCreateProjectActivityProjectId(null);

		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	private void updateProject(){
		try {
			// Getting the project type
			ProjectType projectType = getHelper().getProjectType(ProjectTypeMapper.getCod(getContext(), mProjectType.getSelectedItem().toString()));

			// Updating the project in the database
			Project project = mProjectToEdit;
			project.setBudget(new BigDecimal(mProjectBudget.getText().toString()));
			project.setProjectType(projectType);
			project.setTitle(mProjectTitle.getText().toString());
			getHelper().updateProject(project);

			// Updating project image cover
			ProjectCoverImage projectCoverImage = new ProjectCoverImage();
			projectCoverImage = getHelper().getProjectCoverImageByProjectId(project.getId());
			if(mAvatarData != null){
				projectCoverImage.setAvatarData(mAvatarData);
			}
			projectCoverImage.setUpdatedAt(new Date());
			getHelper().updateProjectCoverImage(projectCoverImage);

			// Updating user to project relationships
			getHelper().updateProjectContacts(project, Globals.getCreateProjectActivityUserList());

			// Pushing the changes
			pushProjects();
			pushProjectCoverImages();
			pushUserToProjects();

			// Resetting the global create project - user list
			Globals.setCreateProjectActivityUserList(new ArrayList<User>());

			// Resetting the global create project - project id
			Globals.setCreateProjectActivityProjectId(null);

		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	/**
	 * Returns boolean indicating whether this is a new project or we are editing one
	 * @return
	 */
	private boolean isNewProject(){
		Long createProjectActivityProjectId = Globals.getCreateProjectActivityProjectId();
		if(createProjectActivityProjectId == null){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Refreshing member list when coming back from the Add People fragment
		mUsersAdapter.updateRecycler(); 
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_create_project;
	}

	@Override
	protected int getTitleResourceId() {
		if(isNewProject()){
			return R.string.cp_new_title;
		} else {
			return R.string.cp_edit_title;
		}
	}

	@Override
	protected void doneAction() {
		// Creating or updating project
		if(isNewProject()){
			createProject();

			// We just finish the activity so that it animates new row
			getActivity().finish();
		} else {
			updateProject();

			// We simulate onBackPressed so we create a new intent and update everything
			getActivity().onBackPressed();
		}
	}

}
