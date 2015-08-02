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

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.SwipeLayout.SwipeListener;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.project.ProjectActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.Project;

public class SwipeProjectsAdapter extends RecyclerSwipeAdapter<SwipeProjectsAdapter.ViewHolder> {
	private List<Project> mProjects;
	private BaseFragment baseFragment;

	// Provide a suitable constructor (depends on the kind of dataset)
	public SwipeProjectsAdapter(List<Project> projects, BaseFragment baseFragment) {
		this.mProjects = projects;
		this.baseFragment = baseFragment;
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
	public void onBindViewHolder(ViewHolder viewHolder, final int position) {
		// Gets element from the data set at this position
		// Replaces the contents of the view with that element
		viewHolder.mProjectTitleTextView.setText(mProjects.get(position).getTitle());

		// Setting the project image cover
		baseFragment.setProjectAvatar(viewHolder.mProjectCoverImageView, mProjects.get(position), ImageUtils.IMAGE_QUALITY_MAX);

		// Setting swipe
		viewHolder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
		viewHolder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.mSwipeLayout.findViewById(R.id.h_bottomView));
		viewHolder.mSwipeLayout.addSwipeListener(new SwipeListener() {
			@Override
			public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
			}
			
			@Override
			public void onStartOpen(SwipeLayout layout) {}
			
			@Override
			public void onStartClose(SwipeLayout layout) {}
			
			@Override
			public void onOpen(SwipeLayout layout) {}
			
			@Override
			public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
			
			@Override
			public void onClose(SwipeLayout layout) {}
		});
		
		// Setting on click listener
		viewHolder.mSwipeLayout.getSurfaceView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				//Saving the project ID in a global variable
				Globals.setExpenseActivityProjectId(mProjects.get(position).getId());

				// We create an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(view.getContext(), ProjectActivity.class);
				view.getContext().startActivity(intent);
			}
		});
		
		// Setting edit on click listener
		viewHolder.mActionEdit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				baseFragment.showToast("Edit!");
			}});

		// Setting remove on click listener
		viewHolder.mActionArchive.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				baseFragment.showToast("Remove!");
			}});

		// Setting the total value for the project
		float total = this.baseFragment.getTotalExpenseForProject(mProjects.get(position).getId());
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

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {
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
