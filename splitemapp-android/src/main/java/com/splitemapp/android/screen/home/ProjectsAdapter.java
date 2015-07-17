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
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.home.ProjectsAdapter.ViewHolder.IProjectClickListener;
import com.splitemapp.android.screen.project.ProjectActivity;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.UserExpense;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {
	private List<Project> mProjects;
	private DatabaseHelper databaseHelper;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public TextView mProjectTitleTextView;
		public IProjectClickListener mClickListener;
		public TextView mProjectTotalValueTextView;

		public ViewHolder(View view, IProjectClickListener clickListener) {
			super(view);
			mProjectTitleTextView = (TextView)view.findViewById(R.id.h_project_title);
			mProjectTotalValueTextView = (TextView)view.findViewById(R.id.h_project_total_value);
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
	public ProjectsAdapter(List<Project> projects, DatabaseHelper databaseHelper) {
		this.mProjects = projects;
		this.databaseHelper = databaseHelper;
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

		// Setting the total value for the project
		try {
			float total = databaseHelper.getAllUserExpenseForProject(mProjects.get(position).getId());
			viewHolder.mProjectTotalValueTextView.setText(String.format("%.2f", total));
		} catch (SQLException e) {
			Log.e("ProjectsAdapter", "SQLException caught!", e);
		}
	}

	@Override
	public int getItemCount() {
		return mProjects.size();
	}

}
