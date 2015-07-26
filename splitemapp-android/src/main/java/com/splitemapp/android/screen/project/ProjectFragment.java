package com.splitemapp.android.screen.project;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.expense.ExpenseActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.UserExpense;

public class ProjectFragment extends BaseFragment {

	private static final String TAG = ProjectFragment.class.getSimpleName();

	private Project mCurrentProject;

	private TextView mProjectTitle;
	private ImageView mProjectCoverImage;
	private FloatingActionButton mFab;

	private View v;

	private RecyclerView mSingleUserExpenseRecycler;
	private SingleUserExpenseAdapter mSingleUserExpenseAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We get the current user and project instances
		try {
			mCurrentProject = getHelper().getProjectById(Globals.getExpenseActivityProjectId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.fragment_project, container, false);

		// Populating the project title
		mProjectTitle = (TextView) v.findViewById(R.id.p_project_title_textView);
		mProjectTitle.setText(mCurrentProject.getTitle());

		// Populating the project cover image
		mProjectCoverImage = (ImageView) v.findViewById(R.id.p_project_cover_image_imageView);
		mProjectCoverImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// in onCreate or any event where your want the user to select a file
				openImageSelector(getProjectCoverImageWidth(), getProjectCoverImageHeight());
			}
		});
		setProjectCoverImage(mProjectCoverImage, mCurrentProject, ImageUtils.IMAGE_QUALITY_MAX);

		// Creating a single user expense adapter to be used in the recycler view
		mSingleUserExpenseAdapter = new SingleUserExpenseAdapter(getUserExpenseList(), this);

		// We populate the list of projects for this user
		mSingleUserExpenseRecycler = (RecyclerView) v.findViewById(R.id.p_expense_list_recyclerView);
		mSingleUserExpenseRecycler.setAdapter(mSingleUserExpenseAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mSingleUserExpenseRecycler.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mSingleUserExpenseRecycler.setLayoutManager(mLayoutManager);

		// Setting the default animator for the view
		mSingleUserExpenseRecycler.setItemAnimator(new CustomItemAnimator());

		// Adding FABs on click listener
		mFab = (FloatingActionButton) v.findViewById(R.id.p_fab);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Moving to the expense creation screen
				Intent intent = new Intent(getActivity(), ExpenseActivity.class);
				Globals.setExpenseActivityProjectId(mCurrentProject.getId());
				startActivity(intent);
			}
		});

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		// We populate the list of projects for this user
		mSingleUserExpenseAdapter = new SingleUserExpenseAdapter(getUserExpenseList(), this);
		mSingleUserExpenseRecycler.setAdapter(mSingleUserExpenseAdapter);
	}

	/**
	 * Returns the whole user expense list for this project
	 * @return
	 */
	private List<UserExpense> getUserExpenseList(){
		List<UserExpense> userExpenseList = null;

		try {
			userExpenseList = getHelper().getUserExpenseDao().queryForEq(TableField.USER_EXPENSE_PROJECT_ID, mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		return userExpenseList;
	}

	@Override
	public void executeOnImageSelection(Bitmap selectedBitmap) {
		// Updating project image on screen
		mProjectCoverImage.setImageBitmap(selectedBitmap);

		// Persisting selected image to database
		try {
			ProjectCoverImage projectCoverImage = getHelper().getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, mCurrentProject.getId()).get(0);
			projectCoverImage.setAvatarData(ImageUtils.bitmapToByteArray(selectedBitmap,ImageUtils.IMAGE_QUALITY_MAX));
			getHelper().getProjectCoverImageDao().createOrUpdate(projectCoverImage);
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

}
