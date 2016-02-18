package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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

	private static final int DIVISION_PRESICION = 4;

	private List<ExpenseGroup> mExpenseGroupList;
	private Project mCurrentProject;
	private BaseFragment mBaseFragment;
	private View mView;
	private BigDecimal mTotalExpenseValue;
	private BigDecimal mMaxCategoryExpenseValue;
	private int mFullBarSize;

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
		this.mTotalExpenseValue = getTotalExpenseValue();
		this.mMaxCategoryExpenseValue = getMaxCategoryExpenseValue();
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
				//TODO switch from one type of view to the other (total amount vs percentage)
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

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// Gets element from the dataset at this position
		// Replaces the contents of the view with that element
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Setting bar width
		if(mFullBarSize == 0){
			mFullBarSize = viewHolder.mBarView.getLayoutParams().width;
		}
		viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * mMaxCategoryExpenseValue);

		// Calculating total percentage
		float totalPercentage = mExpenseGroupList.get(position).getAmount().divide(mTotalExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();

		// Setting percentage value
		viewHolder.mAmountTextView.setText(String.valueOf((int)(totalPercentage*100)));
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
		for(ExpenseCategoryMapper expenseCategoryMapper:ExpenseCategoryMapper.values()){
			// Creating new ExpenseGroup object
			ExpenseGroup expenseGroup = new ExpenseGroup();

			// Getting the category icon
			Drawable categoryIcon = ContextCompat.getDrawable(mBaseFragment.getContext(), expenseCategoryMapper.getDrawableId());
			expenseGroup.setDrawable(categoryIcon);

			// Getting the category expenses
			BigDecimal totalExpense = new BigDecimal(0);
			for(UserExpense userExpense:userExpenseList){
				int expenseCategoryId = userExpense.getExpenseCategory().getId().intValue();
				if(expenseCategoryId == expenseCategoryMapper.getExpenseCategoryId()){
					totalExpense = totalExpense.add(userExpense.getExpense());
				}
			}
			expenseGroup.setAmount(totalExpense);

			// We only add this entry to the list if there are expenses for it
			if(totalExpense.signum()>0){
				expenseGroupList.add(expenseGroup);
			}
		}

		return expenseGroupList;
	}

	/**
	 * Returns the total expense value for this project
	 * @return
	 */
	private BigDecimal getTotalExpenseValue(){
		BigDecimal mTotalExpenseValue = null;
		
		// Obtaining total expense value
		try {
			mTotalExpenseValue = mBaseFragment.getHelper().getTotalExpenseValueByProjectId(mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught while calculating total expense value", e);
		}
		
		return mTotalExpenseValue;
	}
	
	/**
	 * Returns the max expense value category-wise
	 * @return
	 */
	private BigDecimal getMaxCategoryExpenseValue(){
		BigDecimal maxCategoryExpenseValue = new BigDecimal(0);
		
		for(ExpenseGroup expenseGroup:mExpenseGroupList){
			if(expenseGroup.getAmount().compareTo(maxCategoryExpenseValue)>0){
				maxCategoryExpenseValue = expenseGroup.getAmount();
			}
		}
		
		return maxCategoryExpenseValue;
	}
}
