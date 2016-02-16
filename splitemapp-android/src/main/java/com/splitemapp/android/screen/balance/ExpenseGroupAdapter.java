package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.balance.ExpenseGroupAdapter.ViewHolder.IExpenseGroupClickListener;
import com.splitemapp.android.screen.expense.ExpenseCategoryMapper;
import com.splitemapp.commons.comparator.UserExpenseComparator;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.UserExpense;

public class ExpenseGroupAdapter extends RecyclerView.Adapter<ExpenseGroupAdapter.ViewHolder> {

	private static final String TAG = ExpenseGroupAdapter.class.getSimpleName();
	
	private static final int MAX_BAR_SIZE = 100;

	private List<ExpenseGroup> mExpenseGroupList;
	private Project mCurrentProject;
	private BaseFragment mBaseFragment;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public ImageView mIconImageView;
		public View mBarView;
		public TextView mAmountTextView;
		public RecyclerView.LayoutManager mLayoutManager;
		public IExpenseGroupClickListener mClickListener;

		public ViewHolder(View view, IExpenseGroupClickListener clickListener) {
			super(view);
			mIconImageView = (ImageView)view.findViewById(R.id.b_icon_imageView);
			mBarView = view.findViewById(R.id.b_bar_view);
			mAmountTextView = (TextView)view.findViewById(R.id.b_amount_textView);
			
			mClickListener = clickListener;
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			// Calling the custom on click listener
			mClickListener.onItemClick(view, getAdapterPosition());
		}

		public static interface IExpenseGroupClickListener {
			public void onItemClick(View view, int position);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public ExpenseGroupAdapter(Project currentProject, BaseFragment baseFragment) {
		this.mCurrentProject = currentProject;
		this.mBaseFragment = baseFragment;
		this.mExpenseGroupList = getExpenseGroupList();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ExpenseGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, new IExpenseGroupClickListener() {
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
		mExpenseGroupList = getExpenseGroupList();

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
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Setting the size of the bar and percentage value
		try {
			// Calculating percentage
			BigDecimal totalExpenseValue = mBaseFragment.getHelper().getTotalExpenseValueByProjectId(mCurrentProject.getId());
			int percentage = mExpenseGroupList.get(position).getAmount().divide(totalExpenseValue).intValue()*100;
			
			// Setting bar width
			viewHolder.mBarView.getLayoutParams().width = percentage * MAX_BAR_SIZE;
			
			// Setting percentage value
			viewHolder.mAmountTextView.setText(percentage);
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught while calculating total expense value", e);
		}
	}

	@Override
	public int getItemCount() {
		return mExpenseGroupList.size();
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
	private List<ExpenseGroup> getExpenseGroupList(){
		List<ExpenseGroup> expenseGroupList = new ArrayList<ExpenseGroup>();

		List<UserExpense> userExpenseList = getUserExpenseList();
		
		//TODO implement logic to create the ExpenseGroup list
		for(ExpenseCategoryMapper expenseCategoryMapper:ExpenseCategoryMapper.values()){
			for(UserExpense userExpense:userExpenseList){
				if(userExpense.getExpenseCategory().getId().equals(expenseCategoryMapper.getExpenseCategoryId())){
					
				}
			}
		}

		return expenseGroupList;
	}

}
