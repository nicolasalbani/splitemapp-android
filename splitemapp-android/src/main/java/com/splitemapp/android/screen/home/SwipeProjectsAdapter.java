package com.splitemapp.android.screen.home;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.splitemapp.android.R;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.createproject.CreateProjectActivity;
import com.splitemapp.android.screen.project.ProjectActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.widget.CustomAlert;
import com.splitemapp.commons.domain.Project;

public class SwipeProjectsAdapter extends RecyclerSwipeAdapter<SwipeProjectsAdapter.ViewHolder> {
	private static final String TAG = SwipeProjectsAdapter.class.getSimpleName();

	private List<Project> mProjects;
	private BaseFragment baseFragment;

	// Provide a suitable constructor (depends on the kind of dataset)
	public SwipeProjectsAdapter(BaseFragment baseFragment) {
		this.baseFragment = baseFragment;
		this.mProjects = getActiveProjectsList(baseFragment);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public SwipeProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_project, parent, false);
		return new ViewHolder(mView);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		// Saving project ID
		viewHolder.project = mProjects.get(position);

		// Gets element from the data set at this position
		// Replaces the contents of the view with that element
		viewHolder.mProjectTitleTextView.setText(mProjects.get(position).getTitle());

		// Setting the project image cover
		baseFragment.setProjectAvatar(viewHolder.mProjectCoverImageView, mProjects.get(position), ImageUtils.IMAGE_QUALITY_MAX);

		// Setting swipe
		viewHolder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
		viewHolder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.mSwipeLayout.findViewById(R.id.h_bottomView));

		// Setting on click listener
		viewHolder.mSwipeLayout.getSurfaceView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Saving the project ID in a global variable
				Globals.setExpenseActivityProjectId(mProjects.get(position).getId());

				// Creating an intent to the ProjectActivity
				Intent intent = new Intent(view.getContext(), ProjectActivity.class);
				view.getContext().startActivity(intent);
			}
		});

		// Setting edit on click listener
		viewHolder.mActionEdit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Saving the project ID in a global variable
				Globals.setCreateProjectActivityProjectId(viewHolder.project.getId());

				// Creating an intent to the Creat Project activity
				Intent intent = new Intent(view.getContext(), CreateProjectActivity.class);
				view.getContext().startActivity(intent);
			}});

		// Setting archive on click listener
		viewHolder.mActionArchive.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// SHowing custom alert to let the user confirm action
				new CustomAlert(baseFragment.getContext()) {
					@Override
					public String getPositiveButtonText() {
						return baseFragment.getResources().getString(R.string.confirmation_positive_text);
					}
					@Override
					public String getNegativeButtonText() {
						return baseFragment.getResources().getString(R.string.confirmation_negative_text);
					}
					@Override
					public String getMessage() {
						return baseFragment.getResources().getString(R.string.confirmation_archive_project);
					}
					@Override
					public void executeOnPositiveAnswer() {
						try {
							baseFragment.getHelper().archiveCurrentUserToProject(viewHolder.project.getId());
							removeItem(viewHolder);
						} catch (SQLException e) {
							Log.e(TAG, "SQLException caught!", e);
						}
					}
					@Override
					public void executeOnNegativeAnswer() {
						// We do nothing
					}
				}.show();

			}});

		// Setting the total value for the project
		float total = baseFragment.getTotalExpenseForProject(mProjects.get(position).getId());
		viewHolder.mProjectTotalValueTextView.setText(String.format("%.2f", total));

	}

	@Override
	public int getItemCount() {
		return mProjects.size();
	}

	@Override
	public int getSwipeLayoutResourceId(int arg0) {
		return R.id.h_swipeLayout;
	}

	/**
	 * Updates Recycler view adding any existing new items to the list
	 * @param project
	 */
	public void updateRecycler(){
		List<Project> updatedList = getActiveProjectsList(baseFragment);

		// We update all projects in the list
		for(Project project:mProjects){
			int position = mProjects.indexOf(project);
			notifyItemChanged(position);
		}

		// We add any new project to the list
		for(Project project:updatedList){
			if(!mProjects.contains(project)){
				int position = getItemCount();
				mProjects.add(position, project);
				notifyItemInserted(position);
			}
		}
	}

	/**
	 * Remove item from Recycler view
	 */
	public void removeItem(ViewHolder viewHolder){
		mProjects.remove(viewHolder.project);
		notifyItemRemoved(viewHolder.getAdapterPosition());
	}

	/**
	 * Returns the whole list of active projects for this user
	 * @return
	 */
	private List<Project> getActiveProjectsList(BaseFragment baseFragment){
		List<Project> projectList = null;

		try {
			projectList = baseFragment.getHelper().getActiveProjectsForLoggedUser();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		return projectList;
	}

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {
		// Holding project object
		Project project;

		// Declaring the swipe layout
		SwipeLayout mSwipeLayout;

		// Declaring all the items in the surface view
		TextView mProjectTitleTextView;
		ImageView mProjectCoverImageView;
		TextView mProjectTotalValueTextView;

		// Declaring all the actions in the bottom view
		ImageView mActionEdit;
		ImageView mActionArchive;

		public ViewHolder(View view) {
			super(view);

			// Getting instance for swipe layout
			mSwipeLayout = (SwipeLayout)view.findViewById(R.id.h_swipeLayout);

			// Getting instances for all surface items
			mProjectTitleTextView = (TextView)view.findViewById(R.id.h_project_title);
			mProjectTotalValueTextView = (TextView)view.findViewById(R.id.h_project_total_value);
			mProjectCoverImageView = (ImageView)view.findViewById(R.id.h_project_cover_imageView);

			// Getting instances for all bottom items
			mActionEdit = (ImageView)view.findViewById(R.id.h_action_edit_imageView);
			mActionArchive = (ImageView)view.findViewById(R.id.h_action_archive_imageView);
		}

	}
}
