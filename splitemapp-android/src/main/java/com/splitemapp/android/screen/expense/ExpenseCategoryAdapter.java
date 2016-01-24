package com.splitemapp.android.screen.expense;

import java.sql.SQLException;
import java.util.ArrayList;
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
import com.splitemapp.android.screen.expense.ExpenseCategoryAdapter.ViewHolder.IExpenseCategoryClickListener;
import com.splitemapp.commons.domain.ExpenseCategory;

public abstract class ExpenseCategoryAdapter extends RecyclerView.Adapter<ExpenseCategoryAdapter.ViewHolder> {

	private static final String TAG = ExpenseCategoryAdapter.class.getSimpleName();

	private List<ExpenseCategory> mExpenseCategoryList;
	private short mSelectedCategory;
	private BaseFragment mBaseFragment;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public TextView mTitleTextView;
		public ImageView mIconImageView;
		public IExpenseCategoryClickListener mClickListener;

		public ViewHolder(View view, IExpenseCategoryClickListener clickListener) {
			super(view);
			mTitleTextView = (TextView)view.findViewById(R.id.e_expense_category_title_textView);
			mIconImageView = (ImageView)view.findViewById(R.id.e_expense_category_icon_imageView);

			mClickListener = clickListener;
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			// Calling the custom on click listener
			mClickListener.onItemClick(view, getAdapterPosition());
		}

		public static interface IExpenseCategoryClickListener {
			public void onItemClick(View view, int position);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public ExpenseCategoryAdapter(BaseFragment baseFragment, short selectedCategory) {
		this.mSelectedCategory = selectedCategory;
		this.mBaseFragment = baseFragment;
		this.mExpenseCategoryList = getExpenseCategoryList();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ExpenseCategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_category, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, new IExpenseCategoryClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				onClick(view, position);
			}
		});
		return viewHolder;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// Setting the title
		viewHolder.mTitleTextView.setText(ExpenseCategoryMapper.values()[position].getTitleId());

		// Setting the icon
		viewHolder.mIconImageView.setImageResource(ExpenseCategoryMapper.values()[position].getDrawableId());
		
		// Setting background color for selected category
		if(position == mSelectedCategory){
			viewHolder.itemView.setBackgroundResource(R.color.grey);
		}
	}

	@Override
	public int getItemCount() {
		return mExpenseCategoryList.size();
	}


	/**
	 * Returns a list of SingleUserExpenses created upon the provided UserExpense list
	 * @return
	 */
	private List<ExpenseCategory> getExpenseCategoryList(){
		List<ExpenseCategory> expenseCategoryList = new ArrayList<ExpenseCategory>();

		try {
			expenseCategoryList = mBaseFragment.getHelper().getExpenseCategoryList();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		return expenseCategoryList;
	}

	/**
	 * Code to be executed upon getting an onClick event
	 */
	protected abstract void onClick(View view, int position);
}
