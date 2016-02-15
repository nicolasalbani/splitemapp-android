package com.splitemapp.android.screen.balance;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.project.SingleUserExpenseAdapter.ViewHolder.IUserExpenseClickListener;
import com.splitemapp.commons.comparator.SingleUserExpensesComparator;
import com.splitemapp.commons.comparator.UserExpenseComparator;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.SingleUserExpenses;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class ExpenseGroupAdapter extends RecyclerView.Adapter<ExpenseGroupAdapter.ViewHolder> {

	private static final String TAG = ExpenseGroupAdapter.class.getSimpleName();

	private List<UserExpense> mUserExpenseList;
	private Project mCurrentProject;
	private BaseFragment mBaseFragment;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public TextView mFullNameTextView;
		public TextView mFullAmountTextView;
		public RecyclerView.LayoutManager mLayoutManager;
		public IUserExpenseClickListener mClickListener;

		public ViewHolder(View view, IUserExpenseClickListener clickListener) {
			super(view);
			mFullNameTextView = (TextView)view.findViewById(R.id.ue_fullName_textView);
			mFullAmountTextView = (TextView)view.findViewById(R.id.ue_fullAmount_textView);
			
			mClickListener = clickListener;
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			// Calling the custom on click listener
			mClickListener.onItemClick(view, getAdapterPosition());
		}

		public static interface IUserExpenseClickListener {
			public void onItemClick(View view, int position);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public ExpenseGroupAdapter(Project currentProject, BaseFragment baseFragment) {
		this.mCurrentProject = currentProject;
		this.mBaseFragment = baseFragment;
		this.mUserExpenseList = getSingleUserExpenseList();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ExpenseGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_expense_list, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, new IUserExpenseClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				// Expanding or minimizing this user list
				RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.ue_user_expense_list_recyclerView);
				ImageView arrowImageView = (ImageView)view.findViewById(R.id.ue_arrow_imageView);
				switch (recyclerView.getVisibility()){
				case View.VISIBLE:
					mBaseFragment.rotateImageViewAntiClockwise(arrowImageView);
					recyclerView.setVisibility(View.GONE);
					break;
				default :
					mBaseFragment.rotateImageViewClockwise(arrowImageView);
					recyclerView.setVisibility(View.VISIBLE);
					break;
				}
			}
		});
		return viewHolder;
	}

	/**
	 * Updates the content of the recycler
	 */
	public void updateRecycler(){
		// Getting a sorted list of SingleUserExpenses
		mUserExpenseList = getSingleUserExpenseList();

		// We notify that the data set has changed
		notifyDataSetChanged();
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
		viewHolder.mFullNameTextView.setText(mUserExpenseList.get(position).getFullName());

		// Setting the total value for the user
		viewHolder.mFullAmountTextView.setText(String.format("%.2f", mUserExpenseList.get(position).getFullAmount()));
	}

	@Override
	public int getItemCount() {
		return mUserExpenseList.size();
	}

	/**
	 * Returns the whole user expense list for this project
	 * @return
	 */
	private List<UserExpense> getUserExpenseList(){
		List<UserExpense> userExpenseList = null;

		// Getting the UserExpense list from the database
		try {
			userExpenseList = mBaseFragment.getHelper().getUserExpensesByProjectId(mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		
		// Sorting the UserExpense list
		Collections.sort(userExpenseList, new UserExpenseComparator());

		return userExpenseList;
	}

	/**
	 * Returns a list of SingleUserExpenses created upon the provided UserExpense list
	 * @return
	 */
	private List<SingleUserExpenses> getSingleUserExpenseList(){
		List<SingleUserExpenses> singleUserExpenseList = new ArrayList<SingleUserExpenses>();

		List<UserExpense> userExpenseList = getUserExpenseList();

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
					User user = mBaseFragment.getHelper().getUser(uid);
					singleUserExpenseList.add(new SingleUserExpenses(user.getFullName(), filteredUserExpenseList));
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught!", e);
				}
			}
		}
		
		// Sorting the SingleUserExpenseList
		Collections.sort(singleUserExpenseList, new SingleUserExpensesComparator());

		return singleUserExpenseList;
	}

}
