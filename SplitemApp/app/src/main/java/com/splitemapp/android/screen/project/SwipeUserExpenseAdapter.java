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

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.expense.ExpenseActivity;
import com.splitemapp.android.screen.expense.ExpenseCategoryMapper;
import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.widget.ConfirmationAlertDialog;
import com.splitemapp.commons.domain.UserExpense;

public class SwipeUserExpenseAdapter extends RecyclerSwipeAdapter<SwipeUserExpenseAdapter.ViewHolder> {

	private static final String TAG = SwipeUserExpenseAdapter.class.getSimpleName();

	private List<UserExpense> mUserExpenseList;
	private RestfulFragment restfulFragment;
	private SingleUserExpenseAdapter singleUserExpenseAdapter;
	private View mView;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {
		// Each data item is a project
		public UserExpense mUserExpense;
		public ImageView mIconImageView;
		public TextView mCategoryTextView;
		public TextView mNotesTextView;
		public TextView mDateTextView;
		public TextView mAmountTextView;

		// Declaring the swipe layout
		SwipeLayout mSwipeLayout;

		// Declaring all the actions in the bottom view
		ImageView mActionArchive;

		public ViewHolder(View view) {
			super(view);
			mIconImageView = (ImageView)view.findViewById(R.id.ue_icon_imageView);
			mCategoryTextView = (TextView)view.findViewById(R.id.ue_category_textView);
			mNotesTextView = (TextView)view.findViewById(R.id.ue_notes_textView);
			mDateTextView = (TextView)view.findViewById(R.id.ue_date_textView);
			mAmountTextView = (TextView)view.findViewById(R.id.ue_amount_textView);
			mActionArchive = (ImageView)view.findViewById(R.id.ue_action_archive_imageView);

			// Getting instance for swipe layout
			mSwipeLayout = (SwipeLayout)view.findViewById(R.id.ue_swipeLayout);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public SwipeUserExpenseAdapter(List<UserExpense> userExpenseList, RestfulFragment restfulFragment, SingleUserExpenseAdapter singleUserExpenseAdapter) {
		this.restfulFragment = restfulFragment;
		this.mUserExpenseList = userExpenseList;
		this.singleUserExpenseAdapter = singleUserExpenseAdapter;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public SwipeUserExpenseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_expense, parent, false);

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView);
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
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		viewHolder.mUserExpense = mUserExpenseList.get(position);

		// Setting category title
		Short expenseCategoryId = viewHolder.mUserExpense.getExpenseCategory().getId();
		viewHolder.mCategoryTextView.setText(ExpenseCategoryMapper.values()[expenseCategoryId-1].getTitleId());

		// Setting notes
		if(viewHolder.mUserExpense.getNote() != null && !viewHolder.mUserExpense.getNote().isEmpty()){
			viewHolder.mNotesTextView.setText("("+viewHolder.mUserExpense.getNote()+")");
		} else {
			viewHolder.mNotesTextView.setText("");
		}

		// Setting date
		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
		String date = dateFormat.format(viewHolder.mUserExpense.getExpenseDate());
		viewHolder.mDateTextView.setText(date);

		// Setting icon which indicates whether this expense was pushed to server already
		try {
			if(restfulFragment.getHelper().isExpensePushed(viewHolder.mUserExpense)){
				viewHolder.mIconImageView.setImageResource(R.drawable.ic_checkbox_marked_circle_24dp);
			} else {
				viewHolder.mIconImageView.setImageResource(R.drawable.ic_checkbox_blank_circle_outline_24dp);
			}
		} catch (SQLException e) {
			// Do nothing
		}

		// Setting swipe
		viewHolder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
		viewHolder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.mSwipeLayout.findViewById(R.id.h_bottomView));

		// Setting on click listener
		viewHolder.mSwipeLayout.getSurfaceView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Creating an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(restfulFragment.getActivity(), ExpenseActivity.class);
				intent.putExtra(BaseTask.EXPENSE_ID_EXTRA, mUserExpenseList.get(position).getId());
				restfulFragment.startActivity(intent);
			}
		});

		// Setting archive on click listener
		viewHolder.mActionArchive.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// Showing custom alert to let the user confirm action
				new ConfirmationAlertDialog(restfulFragment.getContext()) {
					@Override
					public String getPositiveButtonText() {
						return restfulFragment.getResources().getString(R.string.confirmation_positive_text);
					}
					@Override
					public String getNegativeButtonText() {
						return restfulFragment.getResources().getString(R.string.confirmation_negative_text);
					}
					@Override
					public String getMessage() {
						return restfulFragment.getResources().getString(R.string.confirmation_archive_expense);
					}
					@Override
					public void executeOnPositiveAnswer() {
						try {
							restfulFragment.getHelper().archiveUserExpense(viewHolder.mUserExpense);
							removeItem(viewHolder);
							
							// Pushing the changes
							restfulFragment.pushUserExpenses();
						} catch (SQLException e) {
							Log.e(TAG, "SQLException caught!", e);
						}
					}
					@Override
					public void executeOnNegativeAnswer() {
						// We do nothing
					}
				}.show();

			}});

		// Setting amount
		viewHolder.mAmountTextView.setText(String.format("%.2f", viewHolder.mUserExpense.getExpense()));
	}

	/**
	 * Remove item from Recycler view
	 */
	public void removeItem(ViewHolder viewHolder){
		mUserExpenseList.remove(viewHolder.mUserExpense);
		notifyItemRemoved(viewHolder.getAdapterPosition());

		//TODO We need to change the way we show expenses. It needs to be a single recycler with more than one
		// type of container. That way we don't need nested stuff and we can even use animations.
		singleUserExpenseAdapter.updateRecycler();
	}

	@Override
	public int getItemCount() {
		return mUserExpenseList.size();
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.ue_swipeLayout;
	}

}
