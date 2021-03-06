package com.splitemapp.android.screen.project;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.project.SingleUserExpenseAdapter.ViewHolder.IUserExpenseClickListener;
import com.splitemapp.android.widget.LinearLayoutManager;
import com.splitemapp.commons.comparator.SingleUserExpensesComparator;
import com.splitemapp.commons.comparator.UserExpenseComparator;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.SingleUserExpenses;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class SingleUserExpenseAdapter extends RecyclerView.Adapter<SingleUserExpenseAdapter.ViewHolder> {

	private static final String TAG = SingleUserExpenseAdapter.class.getSimpleName();

	private List<SingleUserExpenses> mSingleUserExpenseList;
	private Project mCurrentProject;
	private Calendar mCalendar;
	private RestfulFragment mRestfulFragment;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public TextView mFullNameTextView;
		public TextView mFullAmountTextView;
		public RecyclerView mUserExpenseRecyclerView;
		public SwipeUserExpenseAdapter mUserExpenseAdapter;
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
			mClickListener.onItemClick(view, getAdapterPosition());
		}

		public static interface IUserExpenseClickListener {
			public void onItemClick(View view, int position);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public SingleUserExpenseAdapter(Project currentProject, RestfulFragment restfulFragment, Calendar calendar) {
		this.mCurrentProject = currentProject;
		this.mRestfulFragment = restfulFragment;
		this.mCalendar = calendar;
		this.mSingleUserExpenseList = getSingleUserExpenseList();
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
				RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.ue_user_expense_list_recyclerView);
				ImageView arrowImageView = (ImageView)view.findViewById(R.id.ue_arrow_imageView);
				switch (recyclerView.getVisibility()){
				case View.VISIBLE:
					mRestfulFragment.rotateImageViewAntiClockwise(arrowImageView);
					recyclerView.setVisibility(View.GONE);
					break;
				default :
					mRestfulFragment.rotateImageViewClockwise(arrowImageView);
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
		mSingleUserExpenseList = getSingleUserExpenseList();

		// We notify that the data set has changed
		notifyDataSetChanged();
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// Gets element from the dataset at this position
		// Replaces the contents of the view with that element
		viewHolder.mFullNameTextView.setText(mSingleUserExpenseList.get(position).getFullName());

		// Setting the total value for the user
		viewHolder.mFullAmountTextView.setText(String.format("%.2f", mSingleUserExpenseList.get(position).getFullAmount()));

		// Creating a single user expense adapter to be used in the recycler view
		viewHolder.mUserExpenseAdapter = new SwipeUserExpenseAdapter(mSingleUserExpenseList.get(position).getExpenseList(), mRestfulFragment, this);

		// We populate the list of user expenses for this user
		viewHolder.mUserExpenseRecyclerView.setAdapter(viewHolder.mUserExpenseAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		viewHolder.mUserExpenseRecyclerView.setHasFixedSize(true);

		// Using a linear layout manager
		viewHolder.mLayoutManager = new LinearLayoutManager(mRestfulFragment.getActivity());
		viewHolder.mUserExpenseRecyclerView.setLayoutManager(viewHolder.mLayoutManager);
	}

	@Override
	public int getItemCount() {
		return mSingleUserExpenseList.size();
	}

	/**
	 * Returns the whole user expense list for this project
	 * @return
	 */
	private List<UserExpense> getActiveUserExpensesForCurrentProject(Calendar calendar){
		List<UserExpense> userExpenseList = null;

		// Getting the UserExpense list from the database
		try {
			userExpenseList = mRestfulFragment.getHelper().getActiveUserExpensesByProjectId(mCurrentProject.getId(), calendar);
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

		List<UserExpense> userExpenseList = getActiveUserExpensesForCurrentProject(mCalendar);

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
					User user = mRestfulFragment.getHelper().getUser(uid);
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
