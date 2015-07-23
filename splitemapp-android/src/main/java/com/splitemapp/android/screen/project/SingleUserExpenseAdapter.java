package com.splitemapp.android.screen.project;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.domain.SingleUserExpenseList;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.project.SingleUserExpenseAdapter.ViewHolder.IUserExpenseClickListener;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class SingleUserExpenseAdapter extends RecyclerView.Adapter<SingleUserExpenseAdapter.ViewHolder> {
	private static final String TAG = SingleUserExpenseAdapter.class.getSimpleName();

	private List<SingleUserExpenseList> mSingleUserExpenseList;
	private BaseFragment baseFragment;
	private View mView;
	
	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public TextView mFullNameTextView;
		public TextView mFullAmountTextView;
		public RecyclerView mUserExpenseRecyclerView;
		public UserExpenseAdapter mUserExpenseAdapter;
		public RecyclerView.LayoutManager mLayoutManager;
		public IUserExpenseClickListener mClickListener;

		public ViewHolder(View view, IUserExpenseClickListener clickListener) {
			super(view);
			mFullNameTextView = (TextView)view.findViewById(R.id.ue_fullName_textView);
			mFullAmountTextView = (TextView)view.findViewById(R.id.ue_fullAmount_textView);
			mUserExpenseRecyclerView = (RecyclerView)view.findViewById(R.id.ue_user_expense_list_recyclerView);
			mClickListener = clickListener;
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			// Calling the custom on click listener
			mClickListener.onItemClick(view, getPosition());
		}

		public static interface IUserExpenseClickListener {
			public void onItemClick(View view, int position);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public SingleUserExpenseAdapter(List<UserExpense> userExpenseList, BaseFragment baseFragment) {
		this.baseFragment = baseFragment;
		this.mSingleUserExpenseList = getSingleUserExpenseList(userExpenseList);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public SingleUserExpenseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_expense_list, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, new IUserExpenseClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				// Expanding or minimizing this user list
				RecyclerView recyclerView = (RecyclerView)mView.findViewById(R.id.ue_user_expense_list_recyclerView);
				switch (recyclerView.getVisibility()){
				case View.VISIBLE:
					recyclerView.setVisibility(View.GONE);
					break;
				default :
					recyclerView.setVisibility(View.VISIBLE);
					break;
				}
			}
		});
		return viewHolder;
	}

	public List<SingleUserExpenseList> getSingleUserExpenseList(List<UserExpense> userExpenseList){
		List<SingleUserExpenseList> singleUserExpenseList = new ArrayList<SingleUserExpenseList>();

		// Creating the users ID list
		List<Long> userIdList = new ArrayList<Long>();
		for(UserExpense userExpense:userExpenseList){
			if(!userIdList.contains(userExpense.getUser().getId())){
				userIdList.add(userExpense.getUser().getId());
			}
		}

		// Populating the SingleUserExpenseList
		for(Long userId:userIdList){
			List<UserExpense> filteredUserExpenseList = new ArrayList<UserExpense>();
			for(UserExpense userExpense:userExpenseList){
				if(userExpense.getUser().getId().equals(userId)){
					filteredUserExpenseList.add(userExpense);
				}
			}
			if(filteredUserExpenseList.size()>0){
				try {
					Long uid = filteredUserExpenseList.get(0).getUser().getId();
					User user = baseFragment.getHelper().getUserById(uid);
					singleUserExpenseList.add(new SingleUserExpenseList(user.getFullName(), filteredUserExpenseList));
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught!", e);
				}
			}
		}

		return singleUserExpenseList;
	}

	/**
	 * Add item to recycler view
	 * @param project
	 */
	//	public void addItem(Project project){
	//		if(!mProjects.contains(project)){
	//			int position = getItemCount();
	//			mProjects.add(position, project);
	//			notifyItemInserted(position);
	//		}
	//	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// Gets element from the dataset at this position
		// Replaces the contents of the view with that element
		viewHolder.mFullNameTextView.setText(mSingleUserExpenseList.get(position).getFullName());

		// Setting the total value for the user
		viewHolder.mFullAmountTextView.setText(String.format("%.2f", mSingleUserExpenseList.get(position).getFullAmount()));

		// Creating a single user expense adapter to be used in the recycler view
		viewHolder.mUserExpenseAdapter = new UserExpenseAdapter(mSingleUserExpenseList.get(position).getExpenseList(), baseFragment);

		// We populate the list of user expenses for this user
		viewHolder.mUserExpenseRecyclerView.setAdapter(viewHolder.mUserExpenseAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		viewHolder.mUserExpenseRecyclerView.setHasFixedSize(true);

		// Using a linear layout manager
		viewHolder.mLayoutManager = new LinearLayoutManager(baseFragment.getActivity());
		viewHolder.mUserExpenseRecyclerView.setLayoutManager(viewHolder.mLayoutManager);

		// Setting the default animator for the view
		viewHolder.mUserExpenseRecyclerView.setItemAnimator(new CustomItemAnimator());
	}

	@Override
	public int getItemCount() {
		return mSingleUserExpenseList.size();
	}

}
