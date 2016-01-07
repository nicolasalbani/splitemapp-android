package com.splitemapp.android.screen.project;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import com.splitemapp.android.R;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.expense.ExpenseActivity;
import com.splitemapp.android.screen.project.UserExpenseAdapter.ViewHolder.IUserExpenseClickListener;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.UserExpense;

public class UserExpenseAdapter extends RecyclerView.Adapter<UserExpenseAdapter.ViewHolder> {
	private static final String TAG = UserExpenseAdapter.class.getSimpleName();

	private List<UserExpense> mUserExpenseList;
	private BaseFragment baseFragment;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public ImageView mIconImageView;
		public TextView mCategoryTextView;
		public TextView mDateTextView;
		public TextView mAmountTextView;
		public IUserExpenseClickListener mClickListener;

		public ViewHolder(View view, IUserExpenseClickListener clickListener) {
			super(view);
			mIconImageView = (ImageView)view.findViewById(R.id.ue_icon_imageView);
			mCategoryTextView = (TextView)view.findViewById(R.id.ue_category_textView);
			mDateTextView = (TextView)view.findViewById(R.id.ue_date_textView);
			mAmountTextView = (TextView)view.findViewById(R.id.ue_amount_textView);
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
	public UserExpenseAdapter(List<UserExpense> userExpenseList, BaseFragment baseFragment) {
		this.baseFragment = baseFragment;
		this.mUserExpenseList = userExpenseList;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public UserExpenseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_expense, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, new IUserExpenseClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				// Creating an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(baseFragment.getActivity(), ExpenseActivity.class);
				Globals.setExpenseActivityExpenseId(mUserExpenseList.get(position).getId());
				baseFragment.startActivity(intent);
			}
		});
		return viewHolder;
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
		UserExpense userExpense = mUserExpenseList.get(position);
		
		// Setting category title
		try {
			Short expenseCategoryId = userExpense.getExpenseCategory().getId();
			ExpenseCategory expenseCategory = baseFragment.getHelper().getExpenseCategory(expenseCategoryId.shortValue());
			viewHolder.mCategoryTextView.setText(expenseCategory.getTitle());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// Setting date
		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
		String date = dateFormat.format(userExpense.getExpenseDate());
		viewHolder.mDateTextView.setText(date);

		// Setting amount
		viewHolder.mAmountTextView.setText(String.format("%.2f", userExpense.getExpense()));
	}

	@Override
	public int getItemCount() {
		return mUserExpenseList.size();
	}

}
