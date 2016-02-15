package com.splitemapp.android.screen.balance;

import java.sql.SQLException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.RestfulFragmentWithTransparentActionbar;
import com.splitemapp.android.screen.createproject.CreateProjectActivity;
import com.splitemapp.android.screen.expense.ExpenseActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.widget.ConfirmationAlertDialog;
import com.splitemapp.android.widget.ListAlertDialog;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;

public class BalanceFragment extends RestfulFragmentWithTransparentActionbar {

	private static final String TAG = BalanceFragment.class.getSimpleName();

	private Project mCurrentProject;

	private TextView mProjectTitle;
	private ImageView mProjectCoverImage;
	private FloatingActionButton mFab;

	private TextView mEmptyListHintTextView;

	private RecyclerView mSingleUserExpenseRecycler;
	private ExpenseGroupAdapter mSingleUserExpenseAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We get the current user and project instances
		try {
			mCurrentProject = getHelper().getProject(Globals.getExpenseActivityProjectId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		// Populating the project title
		mProjectTitle = (TextView) v.findViewById(R.id.p_project_title_textView);
		mProjectTitle.setText(mCurrentProject.getTitle());

		// Populating the project cover image
		mProjectCoverImage = (ImageView) v.findViewById(R.id.p_project_cover_image_imageView);
		setProjectCoverImage(mProjectCoverImage, mCurrentProject, ImageUtils.IMAGE_QUALITY_MAX);

		// Creating a single user expense adapter to be used in the recycler view
		mSingleUserExpenseAdapter = new ExpenseGroupAdapter(mCurrentProject, this);

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

		// Getting the hint if project list is empty
		mEmptyListHintTextView = (TextView) v.findViewById(R.id.p_empty_list_hint_textView);

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

		// Setting a swipe refresh listener
		setSwipeRefresh((SwipeRefreshLayout) v.findViewById(R.id.p_swipe_refresh));
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
	 * Makes all necessary updates to this fragment
	 */
	private void updateFragment(){
		try {
			mCurrentProject = getHelper().getProject(Globals.getExpenseActivityProjectId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// Updating the RecyclerView
		mSingleUserExpenseAdapter.updateRecycler();

		// Showing or hiding the empty list hint
		if(mSingleUserExpenseAdapter.getItemCount() == 0){
			mEmptyListHintTextView.setVisibility(View.VISIBLE);
		} else {
			mEmptyListHintTextView.setVisibility(View.GONE);
		}

		// Updating project title
		mProjectTitle.setText(mCurrentProject.getTitle());

		// Updating project image
		setProjectCoverImage(mProjectCoverImage, mCurrentProject, ImageUtils.IMAGE_QUALITY_MAX);
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
	public void executeOnImageSelection(Bitmap selectedBitmap) {
		// Updating project image on screen
		mProjectCoverImage.setImageBitmap(selectedBitmap);

		// Persisting selected image to database
		try {
			ProjectCoverImage projectCoverImage = getHelper().getProjectCoverImageByProject(mCurrentProject.getId());
			projectCoverImage.setAvatarData(ImageUtils.bitmapToByteArray(selectedBitmap,ImageUtils.IMAGE_QUALITY_MAX));
			getHelper().updateProjectCoverImage(projectCoverImage);

			// Pushing the changes
			pushProjectCoverImages();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_project;
	}

	@Override
	protected void menuAction() {
		ListAlertDialog listAlertDialog = new ListAlertDialog(getActivity()) {
			@Override
			public int getLinearLayoutView() {
				return R.layout.options_project;
			}
		};

		// Setting OnClickListener for archive action
		listAlertDialog.findViewById(R.id.p_option_archive).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Showing custom alert to let the user confirm action
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
						return getResources().getString(R.string.confirmation_archive_project);
					}
					@Override
					public void executeOnPositiveAnswer() {
						try {
							getHelper().archiveCurrentUserToProject(mCurrentProject.getId());
							getActivity().finish();
						} catch (SQLException e) {
							Log.e(TAG, "SQLException caught!", e);
						}
					}
					@Override
					public void executeOnNegativeAnswer() {
						// We do nothing
					}
				}.show();
			}
		});;

		// Setting OnClickListener for archive action
		listAlertDialog.findViewById(R.id.p_option_change_cover).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Opening image selector to choose a new cover
				openImageSelector(getProjectCoverImageWidth(), getProjectCoverImageHeight());
			}
		});;

		// Setting OnClickListener for archive action
		listAlertDialog.findViewById(R.id.p_option_edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Saving the project ID in a global variable
				Globals.setCreateProjectActivityProjectId(mCurrentProject.getId());

				// Creating an intent to the Create Project activity
				Intent intent = new Intent(getContext(), CreateProjectActivity.class);
				getContext().startActivity(intent);
			}
		});;
		
		// Showing alert dialog
		listAlertDialog.show();
	}

}