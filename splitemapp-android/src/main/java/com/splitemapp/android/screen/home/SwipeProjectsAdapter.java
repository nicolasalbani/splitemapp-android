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
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.createproject.CreateProjectActivity;
import com.splitemapp.android.screen.project.ProjectActivity;
import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.android.utils.PreferencesManager;
import com.splitemapp.android.widget.ConfirmationAlertDialog;
import com.splitemapp.commons.domain.Project;

public class SwipeProjectsAdapter extends RecyclerSwipeAdapter<SwipeProjectsAdapter.ViewHolder> {
	private static final String TAG = SwipeProjectsAdapter.class.getSimpleName();

	private List<Project> mProjects;
	private RestfulFragment restfulFragment;

	// Provide a suitable constructor (depends on the kind of dataset)
	public SwipeProjectsAdapter(RestfulFragment restfulFragment) {
		this.restfulFragment = restfulFragment;
		this.mProjects = getProjectsList(restfulFragment);
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

		// Getting the title directly from the database since android doesn't update the ViewHolder payload
		String projectTitle = restfulFragment.getProjectTitle(mProjects.get(position).getId());
		viewHolder.mProjectTitleTextView.setText(projectTitle);
		
		// Setting the total value for the project
		float total = restfulFragment.getTotalExpenseForProject(mProjects.get(position).getId());
		viewHolder.mProjectTotalValueTextView.setText(String.format("%.2f", total));

		// Setting the project image cover
		restfulFragment.setProjectAvatar(viewHolder.mProjectCoverImageView, mProjects.get(position).getId(), ImageUtils.IMAGE_QUALITY_MAX);

		// Setting swipe
		viewHolder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
		viewHolder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.mSwipeLayout.findViewById(R.id.h_bottomView));

		// Setting on click listener
		viewHolder.mSwipeLayout.getSurfaceView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Creating an intent to the ProjectActivity
				Intent intent = new Intent(view.getContext(), ProjectActivity.class);
				intent.putExtra(BaseTask.PROJECT_ID_EXTRA, mProjects.get(position).getId());
				view.getContext().startActivity(intent);
			}
		});

		// Setting edit on click listener
		viewHolder.mActionEdit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Creating an intent to the Create Project activity
				Intent intent = new Intent(view.getContext(), CreateProjectActivity.class);
				intent.putExtra(BaseTask.PROJECT_ID_EXTRA, viewHolder.project.getId());
				view.getContext().startActivity(intent);
			}});

		// Setting archive on click listener
		viewHolder.mActionArchive.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Showing custom alert to let the user confirm action
				new ConfirmationAlertDialog(restfulFragment.getContext()) {
					@Override
					public String getPositiveButtonText() {
						return restfulFragment.getResources().getString(R.string.confirmation_positive_text);
					}
					@Override
					public String getNegativeButtonText() {
						return restfulFragment.getResources().getString(R.string.confirmation_negative_text);
					}
					@Override
					public String getMessage() {
						return restfulFragment.getResources().getString(R.string.confirmation_archive_project);
					}
					@Override
					public void executeOnPositiveAnswer() {
						try {
							restfulFragment.getHelper().archiveCurrentUserToProject(viewHolder.project.getId());
							removeItem(viewHolder);
							
							// Pushing the changes
							restfulFragment.pushUserToProjects();
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
	public void updateRecycler(RecyclerView mProjectsRecycler){
		List<Project> updatedList = getProjectsList(restfulFragment);

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
		
		// We remove any archived projects from the list
		removeArchivedProjects(updatedList);
	}
	
	private void removeArchivedProjects(List<Project> updatedList){
		// We remove any archived projects from the list
		for(Project project:mProjects){
			if(!updatedList.contains(project)){
				int position = mProjects.indexOf(project);
				mProjects.remove(position);
				notifyItemRemoved(position);
				removeArchivedProjects(updatedList);
				break;
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
	private List<Project> getProjectsList(BaseFragment baseFragment){
		List<Project> projectList = null;

		try {
			boolean showOpenProjects = baseFragment.getPrefsManager().getBoolean(PreferencesManager.SHOW_ACTIVE_PROJECTS);
			boolean showArchivedProjects = baseFragment.getPrefsManager().getBoolean(PreferencesManager.SHOW_ARCHIVED_PROJECTS);
			boolean showMonthlyProjects = baseFragment.getPrefsManager().getBoolean(PreferencesManager.SHOW_MONTHLY_PROJECTS);
			boolean showOneTimeProjects = baseFragment.getPrefsManager().getBoolean(PreferencesManager.SHOW_ONE_TIME_PROJECTS);
			
			projectList = baseFragment.getHelper().getProjectsForLoggedUser(showOpenProjects, showArchivedProjects, showMonthlyProjects, showOneTimeProjects);
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
