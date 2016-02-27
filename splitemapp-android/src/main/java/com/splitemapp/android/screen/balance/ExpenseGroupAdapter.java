package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.balance.ExpenseGroupAdapter.ViewHolder.IExpenseGroupClickListener;
import com.splitemapp.android.screen.expense.ExpenseCategoryMapper;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.comparator.UserExpenseComparator;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserToProject;

public class ExpenseGroupAdapter extends RecyclerView.Adapter<ExpenseGroupAdapter.ViewHolder> {

	private static final String TAG = ExpenseGroupAdapter.class.getSimpleName();

	private static final int DIVISION_PRESICION = 4;

	private static BigDecimal mTotalExpenseValue;
	private static DecimalFormat mExpenseAmountFormat;

	private List<ExpenseGroup> mExpenseGroupList;
	private Project mCurrentProject;
	private BaseFragment mBaseFragment;
	private View mView;
	private Calendar mCalendar;
	private BalanceMode mBalanceMode;
	private BigDecimal mMaxGroupExpenseValue;
	private boolean mShowPrimaryView;
	private int mFullBarSize;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		// Each data item is a project
		public BalanceMode mBalanceMode;
		public boolean mShowPrimaryView;
		public boolean mInitializedSeekBar;
		public String identifier;
		public ImageView mIconImageView;
		public View mBarView;
		public TextView mAmountTextView;
		public BigDecimal mAmount;
		public TextView mShareTextView;
		public TextView mShareBalanceTextView;
		public SeekBar mSeekBar;
		public IExpenseGroupClickListener mClickListener;

		public ViewHolder(View view, BalanceMode balanceMode, boolean showPrimaryView, IExpenseGroupClickListener clickListener) {
			super(view);
			mBalanceMode = balanceMode;
			mShowPrimaryView = showPrimaryView;
			mInitializedSeekBar = false;
			mIconImageView = (ImageView)view.findViewById(R.id.b_icon_imageView);
			mAmountTextView = (TextView)view.findViewById(R.id.b_amount_textView);
			mBarView = view.findViewById(R.id.b_bar_view);
			mShareTextView = (TextView)view.findViewById(R.id.b_share_textView);
			mShareBalanceTextView = (TextView)view.findViewById(R.id.b_share_balance_textView);
			mSeekBar = (SeekBar)view.findViewById(R.id.b_seekBar);

			if(mSeekBar != null){
				mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						mShareTextView.setVisibility(View.INVISIBLE);
						mIconImageView.setVisibility(View.VISIBLE);
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						mIconImageView.setVisibility(View.INVISIBLE);
						mShareTextView.setVisibility(View.VISIBLE);
					}
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						mShareTextView.setText(seekBar.getProgress() + "%");
						updateShareBalance(seekBar, mAmount, mShareBalanceTextView);
					}
				});
			}

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
	public ExpenseGroupAdapter(Project currentProject, BaseFragment baseFragment, Calendar calendar, BalanceMode balanceMode) {
		this.mCurrentProject = currentProject;
		this.mBaseFragment = baseFragment;
		this.mCalendar = calendar;
		this.mBalanceMode = balanceMode;
		this.mExpenseGroupList = getExpenseGroupList();
		mTotalExpenseValue = getTotalExpenseValue();
		this.mMaxGroupExpenseValue = getMaxGroupExpenseValue();
		this.mShowPrimaryView = true;

		// Setting the expense amount format
		mExpenseAmountFormat = new DecimalFormat();
		mExpenseAmountFormat.setMaximumFractionDigits(Constants.MAX_DIGITS_AFTER_DECIMAL);
		mExpenseAmountFormat.setMinimumFractionDigits(Constants.MAX_DIGITS_AFTER_DECIMAL);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ExpenseGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Creating a new view
		switch (mBalanceMode) {
		case CATEGORY: 
			if(mShowPrimaryView){
				mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_category_primary, parent, false);
			} else {
				mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_category_secondary, parent, false);
			}
			break;
		case USER:
			if(mShowPrimaryView){
				mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_user_primary, parent, false);
			} else {
				mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_user_secondary, parent, false);
			}
			break;
		case DATE:
			mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_date, parent, false);
			break;
		}

		// Creating a new view holder
		ViewHolder viewHolder = new ViewHolder(mView, mBalanceMode, mShowPrimaryView, new IExpenseGroupClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				if(mBalanceMode != BalanceMode.DATE){
					// Switching view from primary to secondary
					switchView();
					// Updating recycler view
					updateRecycler();
				}
			}
		});
		return viewHolder;
	}

	/**
	 * Switches view from primary to secondary
	 */
	public void switchView(){
		if(mShowPrimaryView){
			mShowPrimaryView = false;
		} else {
			mShowPrimaryView = true;
		}
	}

	/**
	 * Updates the content of the recycler
	 */
	public void updateRecycler(){
		// Notify of all the ViewHolders that are going to be removed
		notifyItemRangeRemoved(0, getItemCount());

		// Getting a sorted list of SingleUserExpenses
		mExpenseGroupList = getExpenseGroupList();

		// Notify of all the ViewHolders that are going to be removed
		notifyItemRangeInserted(0, getItemCount());
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// Workaround for when we get a viewHolder that is not supposed to be binding
		if(mShowPrimaryView != viewHolder.mShowPrimaryView){
			return;
		}

		// Setting the icon drawable
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Calculating total and relative percentage
		viewHolder.mAmount = mExpenseGroupList.get(position).getAmount();
		float totalPercentage = 0;
		if(mTotalExpenseValue.doubleValue() != 0){
			totalPercentage = viewHolder.mAmount.divide(mTotalExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}
		float relativePercentage = 0;
		if(mMaxGroupExpenseValue.doubleValue() != 0){
			relativePercentage = viewHolder.mAmount.divide(mMaxGroupExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}

		// Setting the full bar size only once
		if(mFullBarSize == 0){
			mFullBarSize = viewHolder.mBarView.getLayoutParams().width;
		}

		if(viewHolder.mBalanceMode == BalanceMode.CATEGORY){
			// Setting bar size
			viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * relativePercentage);
			if(viewHolder.mShowPrimaryView){
				// Setting percentage
				viewHolder.mAmountTextView.setText(String.valueOf((int)(totalPercentage*100))+"%");
			} else {
				// Setting amount
				viewHolder.mAmountTextView.setText("$"+ mExpenseAmountFormat.format(viewHolder.mAmount));
			}
		} else if (viewHolder.mBalanceMode == BalanceMode.USER){
			if(viewHolder.mShowPrimaryView){
				// Setting bar size and percentage
				viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * relativePercentage);
				viewHolder.mAmountTextView.setText(String.valueOf((int)(totalPercentage*100))+"%");
			} else {
				// Setting total amount
				viewHolder.mAmountTextView.setText("$"+ mExpenseAmountFormat.format(viewHolder.mAmount));

				// Setting seekbar progress for the first time
				if(!viewHolder.mInitializedSeekBar){
					viewHolder.mInitializedSeekBar = true;
					int expenseShare = mExpenseGroupList.get(position).getUserToProject().getExpensesShare().intValue();
					viewHolder.mSeekBar.setProgress(expenseShare);
				}

				// Setting balance amount and color
				updateShareBalance(viewHolder.mSeekBar, viewHolder.mAmount, viewHolder.mShareBalanceTextView);
			}
		}
	}

	/**
	 * Updates the textView with the appropriate share balance
	 * @param seekBar
	 * @param amount
	 * @param textView
	 */
	public static void updateShareBalance(SeekBar seekBar, BigDecimal amount, TextView textView){
		BigDecimal balance = mTotalExpenseValue.multiply(new BigDecimal((float)seekBar.getProgress()/100)).subtract(amount);
		textView.setText("$"+ mExpenseAmountFormat.format(balance.abs()));
		if(balance.signum()>0){
			textView.setTextColor(Color.RED);
		} else {
			textView.setTextColor(Color.GREEN);
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
			userExpenseList = mBaseFragment.getHelper().getUserExpensesByProjectId(mCurrentProject.getId(), mCalendar);
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// Sorting the UserExpense list
		Collections.sort(userExpenseList, new UserExpenseComparator());

		return userExpenseList;
	}

	/**
	 * Returns the ExpenseGroup list based on the selected balance mode
	 * @return
	 */
	private List<ExpenseGroup> getExpenseGroupList(){
		List<ExpenseGroup> expenseGroupList = new ArrayList<ExpenseGroup>();

		if(mBalanceMode == BalanceMode.CATEGORY){
			expenseGroupList = getCategoryExpenseGroupList();
		} else if (mBalanceMode == BalanceMode.USER){
			expenseGroupList = getUserExpenseGroupList();
		} else if (mBalanceMode == BalanceMode.DATE){
			expenseGroupList = getDateExpenseGroupList();
		}

		return expenseGroupList;
	}

	/**
	 * Returns a list of ExpenseGroup items organized by USER
	 * @return
	 */
	private List<ExpenseGroup> getUserExpenseGroupList(){
		List<ExpenseGroup> expenseGroupList = new ArrayList<ExpenseGroup>();

		List<UserExpense> userExpenseList = getUserExpenseList();
		try {
			for(User user:mBaseFragment.getHelper().getAllUsers()){
				// Creating new ExpenseGroup object
				ExpenseGroup expenseGroup = new ExpenseGroup();

				// Getting the user icon
				byte[] avatarData = mBaseFragment.getHelper().getUserAvatarByUserId(user.getId()).getAvatarData();
				if(avatarData!=null){
					Drawable userAvatar =ImageUtils.byteArrayToCroppedDrawable(avatarData, ImageUtils.IMAGE_QUALITY_MAX, mBaseFragment.getResources());
					expenseGroup.setDrawable(userAvatar);
				} else {
					expenseGroup.setDrawable(ContextCompat.getDrawable(mBaseFragment.getContext(), R.drawable.ic_avatar_placeholder_80dp));
				}

				// Getting the userToProject relationship
				UserToProject userToProject = mBaseFragment.getHelper().getUserToProject(mCurrentProject.getId(), user.getId());
				expenseGroup.setUserToProject(userToProject);

				// Getting the user expenses
				BigDecimal totalExpense = new BigDecimal(0);
				for(UserExpense userExpense:userExpenseList){
					if(userExpense.getUser().getId().equals(user.getId())){
						totalExpense = totalExpense.add(userExpense.getExpense());
					}
				}
				expenseGroup.setAmount(totalExpense);

				// We add this entry to the list
				expenseGroupList.add(expenseGroup);
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught while getting user expense group", e);
		}

		return expenseGroupList;
	}

	/**
	 * Returns a list of ExpenseGroup items organized by DATE
	 * @return
	 */
	private List<ExpenseGroup> getDateExpenseGroupList(){
		List<ExpenseGroup> expenseGroupList = new ArrayList<ExpenseGroup>();

		//TODO implement

		return expenseGroupList;
	}

	/**
	 * Returns a list of ExpenseGroup items organized by CATEGORY
	 * @return
	 */
	private List<ExpenseGroup> getCategoryExpenseGroupList(){
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
			mTotalExpenseValue = mBaseFragment.getHelper().getTotalExpenseValueByProjectId(mCurrentProject.getId(), mCalendar);
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught while calculating total expense value", e);
		}

		return mTotalExpenseValue;
	}

	/**
	 * Returns the max expense value category-wise
	 * @return
	 */
	private BigDecimal getMaxGroupExpenseValue(){
		BigDecimal maxCategoryExpenseValue = new BigDecimal(0);

		for(ExpenseGroup expenseGroup:mExpenseGroupList){
			if(expenseGroup.getAmount().compareTo(maxCategoryExpenseValue)>0){
				maxCategoryExpenseValue = expenseGroup.getAmount();
			}
		}

		return maxCategoryExpenseValue;
	}
}
