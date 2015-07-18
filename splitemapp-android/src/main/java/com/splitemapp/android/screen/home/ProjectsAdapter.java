package com.splitemapp.android.screen.home;

import java.util.List;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.home.ProjectsAdapter.ViewHolder.IProjectClickListener;
import com.splitemapp.android.screen.project.ProjectActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.Project;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {
	private List<Project> mProjects;
	private BaseFragment baseFragment;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public TextView mProjectTitleTextView;
		public ImageView mProjectCoverImageView;
		public IProjectClickListener mClickListener;
		public TextView mProjectTotalValueTextView;

		public ViewHolder(View view, IProjectClickListener clickListener) {
			super(view);
			mProjectTitleTextView = (TextView)view.findViewById(R.id.h_project_title);
			mProjectTotalValueTextView = (TextView)view.findViewById(R.id.h_project_total_value);
			mProjectCoverImageView = (ImageView)view.findViewById(R.id.h_project_cover_imageView);
			mClickListener = clickListener;
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			// Calling the custom on click listener
			mClickListener.onItemClick(view, getPosition());
		}

		public static interface IProjectClickListener {
			public void onItemClick(View view, int position);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public ProjectsAdapter(List<Project> projects, BaseFragment baseFragment) {
		this.mProjects = projects;
		this.baseFragment = baseFragment;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_project, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, new IProjectClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				//Saving the project ID in a global variable
				Globals.setExpenseActivityProjectId(mProjects.get(position).getId());

				// We create an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(view.getContext(), ProjectActivity.class);
				view.getContext().startActivity(intent);
			}
		});
		return viewHolder;
	}

	/**
	 * Add item to recycler view
	 * @param project
	 */
	public void addItem(Project project){
		if(!mProjects.contains(project)){
			int position = getItemCount();
			mProjects.add(position, project);
			notifyItemInserted(position);
		}
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// Gets element from the dataset at this position
		// Replaces the contents of the view with that element
		viewHolder.mProjectTitleTextView.setText(mProjects.get(position).getTitle());
		
		// Setting the project image cover
		baseFragment.setProjectAvatar(viewHolder.mProjectCoverImageView, mProjects.get(position), ImageUtils.IMAGE_QUALITY_MAX);

		// Setting the total value for the project
		float total = this.baseFragment.getTotalExpenseForProject(mProjects.get(position).getId());
		viewHolder.mProjectTotalValueTextView.setText(String.format("%.2f", total));
	}

	@Override
	public int getItemCount() {
		return mProjects.size();
	}

}
