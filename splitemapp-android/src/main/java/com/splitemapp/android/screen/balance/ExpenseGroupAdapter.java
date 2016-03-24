package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.expense.ExpenseAmountFormat;
import com.splitemapp.android.screen.expense.ExpenseCategoryMapper;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.comparator.UserExpenseComparator;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserToProject;

public class ExpenseGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final String TAG = ExpenseGroupAdapter.class.getSimpleName();

	private static final int DIVISION_PRESICION = 4;
	private static final int CATEGORY_PRIMARY = 1;
	private static final int CATEGORY_SECONDARY = 2;
	private static final int USER_PRIMARY = 3;
	private static final int USER_SECONDARY = 4;
	private static final int DATE = 5;
	private static final String CURRENCY_SIGN = "$";

	private static BigDecimal mTotalExpenseValue;
	private static ExpenseAmountFormat mExpenseAmountFormat;

	private List<? extends ExpenseGroup> mExpenseGroupList;
	private Project mCurrentProject;
	private BaseFragment mBaseFragment;
	private View mView;
	private Calendar mCalendar;
	private BalanceMode mBalanceMode;

	private BigDecimal mMaxGroupExpenseValue;
	private boolean mShowPrimaryView;
	private int mFullBarSize;


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
		mExpenseAmountFormat = new ExpenseAmountFormat();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder viewHolder = null;

		// Creating a new view
		switch (viewType) {
		case CATEGORY_PRIMARY: 
			mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_category_primary, parent, false);

			viewHolder = new ViewHolderCategoryPrimary(mView, new IExpenseGroupClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					// Switching view from primary to secondary
					switchView();
					// Updating recycler view
					updateRecycler();
				}
			});

			break;
		case CATEGORY_SECONDARY: 
			mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_category_secondary, parent, false);

			viewHolder = new ViewHolderCategorySecondary(mView, new IExpenseGroupClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					// Switching view from primary to secondary
					switchView();
					// Updating recycler view
					updateRecycler();
				}
			});

			break;
		case USER_PRIMARY:
			mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_user_primary, parent, false);

			viewHolder = new ViewHolderUserPrimary(mView, new IExpenseGroupClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					// Switching view from primary to secondary
					switchView();
					// Updating recycler view
					updateRecycler();
				}
			});

			break;
		case USER_SECONDARY:
			mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_user_secondary, parent, false);

			viewHolder = new ViewHolderUserSecondary(mView, mTotalExpenseValue, mExpenseAmountFormat, new IExpenseGroupClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					// Switching view from primary to secondary
					switchView();
					// Updating recycler view
					updateRecycler();
				}
			});

			break;
		case DATE:
			mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense_group_date, parent, false);
			viewHolder = new ViewHolderDate(mView);
			break;
		}

		return viewHolder;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		switch (viewHolder.getItemViewType()) {
		case CATEGORY_PRIMARY: 
			configureCategoryPrimaryViewHolder((ViewHolderCategoryPrimary)viewHolder, position);
			break;
		case CATEGORY_SECONDARY: 
			configureCategorySecondaryViewHolder((ViewHolderCategorySecondary)viewHolder, position);
			break;
		case USER_PRIMARY:
			configureUserPrimaryViewHolder((ViewHolderUserPrimary)viewHolder, position);
			break;
		case USER_SECONDARY:
			configureUserSecondaryViewHolder((ViewHolderUserSecondary)viewHolder, position);
			break;
		case DATE:
			configureDateViewHolder((ViewHolderDate)viewHolder, position);
			break;
		}

		return;
	}

	@Override
	public int getItemCount() {
		return mExpenseGroupList.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (mExpenseGroupList.get(position) instanceof ExpenseGroupCategory) {
			if(((ExpenseGroupCategory)mExpenseGroupList.get(position)).isShowPrimary()){
				return CATEGORY_PRIMARY;
			} else {
				return CATEGORY_SECONDARY;
			}
		} else if (mExpenseGroupList.get(position) instanceof ExpenseGroupUser) {
			if(((ExpenseGroupUser)mExpenseGroupList.get(position)).isShowPrimary()){
				return USER_PRIMARY;
			} else {
				return USER_SECONDARY;
			}
		} else if (mExpenseGroupList.get(position) instanceof ExpenseGroupDate) {
			return DATE;
		}

		return -1;
	}

	/**
	 * Configures a Category view holder
	 * @param viewHolder
	 * @param position
	 */
	private void configureCategoryPrimaryViewHolder(ViewHolderCategoryPrimary viewHolder, int position){
		// Setting the icon drawable
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Calculating total and relative percentage
		float totalPercentage = 0;
		BigDecimal amount = mExpenseGroupList.get(position).getAmount();
		if(mTotalExpenseValue.doubleValue() != 0){
			totalPercentage = amount.divide(mTotalExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}
		float relativePercentage = 0;
		if(mMaxGroupExpenseValue.doubleValue() != 0){
			relativePercentage = amount.divide(mMaxGroupExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}

		// Setting the full bar size only once
		if(mFullBarSize == 0){
			mFullBarSize = viewHolder.mBarView.getLayoutParams().width;
		}

		// Setting bar size
		viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * relativePercentage);
		// Setting percentage
		viewHolder.mAmountTextView.setText(String.valueOf((int)(totalPercentage*100))+"%");
	}

	/**
	 * Configures a Category view holder
	 * @param viewHolder
	 * @param position
	 */
	private void configureCategorySecondaryViewHolder(ViewHolderCategorySecondary viewHolder, int position){
		// Setting the icon drawable
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Calculating total and relative percentage
		BigDecimal amount = mExpenseGroupList.get(position).getAmount();
		float relativePercentage = 0;
		if(mMaxGroupExpenseValue.doubleValue() != 0){
			relativePercentage = amount.divide(mMaxGroupExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}

		// Setting the full bar size only once
		if(mFullBarSize == 0){
			mFullBarSize = viewHolder.mBarView.getLayoutParams().width;
		}

		// Setting bar size
		viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * relativePercentage);
		// Setting amount
		viewHolder.mAmountTextView.setText("$"+ mExpenseAmountFormat.format(amount));
	}

	/**
	 * Configures a User view holder
	 * @param viewHolder
	 * @param position
	 */
	private void configureUserPrimaryViewHolder(ViewHolderUserPrimary viewHolder, int position){
		// Setting the icon drawable
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Calculating total and relative percentage
		BigDecimal amount = mExpenseGroupList.get(position).getAmount();
		float totalPercentage = 0;
		if(mTotalExpenseValue.doubleValue() != 0){
			totalPercentage = amount.divide(mTotalExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}
		float relativePercentage = 0;
		if(mMaxGroupExpenseValue.doubleValue() != 0){
			relativePercentage = amount.divide(mMaxGroupExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}

		// Setting the full bar size only once
		if(mFullBarSize == 0){
			mFullBarSize = viewHolder.mBarView.getLayoutParams().width;
		}

		// Setting bar size and percentage
		viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * relativePercentage);
		viewHolder.mAmountTextView.setText(String.valueOf((int)(totalPercentage*100))+"%");
	}

	/**
	 * Configures a User view holder
	 * @param viewHolder
	 * @param position
	 */
	private void configureUserSecondaryViewHolder(ViewHolderUserSecondary viewHolder, int position){
		// Setting the icon drawable
		viewHolder.mIconImageView.setImageDrawable(mExpenseGroupList.get(position).getDrawable());

		// Setting amount
		viewHolder.mAmount = mExpenseGroupList.get(position).getAmount();

		// Setting total amount
		viewHolder.mAmountTextView.setText("$"+ mExpenseAmountFormat.format(viewHolder.mAmount));

		// Setting seekbar progress for the first time
		if(!viewHolder.mInitializedSeekBar){
			viewHolder.mInitializedSeekBar = true;
			int expenseShare = ((ExpenseGroupUser)mExpenseGroupList.get(position)).getUserToProject().getExpensesShare().intValue();
			viewHolder.mSeekBar.setProgress(expenseShare);
		}

		// Setting balance amount and color
		viewHolder.updateShareBalance(viewHolder.mSeekBar, viewHolder.mAmount, viewHolder.mShareBalanceTextView);
	}

	/**
	 * Configures a Date view holder
	 * @param viewHolder
	 * @param position
	 */
	private void configureDateViewHolder(ViewHolderDate viewHolder, int position){
		ExpenseGroupDate expenseGroup = (ExpenseGroupDate)mExpenseGroupList.get(position);

		// Setting the month textView
		viewHolder.mMonthTextView.setText(MonthMapper.values()[expenseGroup.getMonthYear().get(Calendar.MONTH)].getShortStringId());

		// Setting the year textView
		viewHolder.mYearTextView.setText(String.valueOf(expenseGroup.getMonthYear().get(Calendar.YEAR)));

		// Setting the full bar size only once
		if(mFullBarSize == 0){
			mFullBarSize = viewHolder.mBarView.getLayoutParams().width;
		}

		// Calculating relative percentage
		BigDecimal amount = mExpenseGroupList.get(position).getAmount();
		float relativePercentage = 0;
		if(mMaxGroupExpenseValue.doubleValue() != 0){
			relativePercentage = amount.divide(mMaxGroupExpenseValue, DIVISION_PRESICION,  RoundingMode.HALF_UP).floatValue();
		}

		// Setting bar size and percentage
		viewHolder.mBarView.getLayoutParams().width = (int)(mFullBarSize * relativePercentage);

		// Setting the amount and the project balance
		BigDecimal balance = mCurrentProject.getBudget().subtract(amount);
		viewHolder.mAmountTextView.setText(CURRENCY_SIGN+mExpenseAmountFormat.format(amount));
		viewHolder.mBalanceTextView.setText(CURRENCY_SIGN+mExpenseAmountFormat.format(balance.abs()));
		if(balance.signum()>0){
			viewHolder.mBalanceTextView.setTextColor(ContextCompat.getColor(mBaseFragment.getContext(), R.color.green));
		} else {
			viewHolder.mBalanceTextView.setTextColor(ContextCompat.getColor(mBaseFragment.getContext(), R.color.red));
		}
	}

	/**
	 * Returns the whole user expense list for this project
	 * @return
	 */
	private List<UserExpense> getUserExpensesForCurrentProject(Calendar calendar){
		List<UserExpense> userExpenseList = null;

		// Getting the UserExpense list from the database
		try {
			userExpenseList = mBaseFragment.getHelper().getUserExpensesByProjectId(mCurrentProject.getId(), calendar);
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
	private List<? extends ExpenseGroup> getExpenseGroupList(){
		List<? extends ExpenseGroup> expenseGroupList = null;

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
	private List<ExpenseGroupUser> getUserExpenseGroupList(){
		List<ExpenseGroupUser> expenseGroupList = new ArrayList<ExpenseGroupUser>();

		List<UserExpense> userExpenseList = null;
		if(isMonthlyProject()){
			userExpenseList = getUserExpensesForCurrentProject(mCalendar);
		} else {
			userExpenseList = getUserExpensesForCurrentProject(null);
		}
		try {
			for(User user:mBaseFragment.getHelper().getAllUsers()){
				// Creating new ExpenseGroup object
				ExpenseGroupUser expenseGroup = new ExpenseGroupUser();

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

				// Setting whether this is a primary view
				expenseGroup.setShowPrimary(mShowPrimaryView);
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
	private List<ExpenseGroupDate> getDateExpenseGroupList(){
		List<ExpenseGroupDate> expenseGroupList = new ArrayList<ExpenseGroupDate>();

		List<UserExpense> userExpenseList = getUserExpensesForCurrentProject(null);
		for(UserExpense userExpense:userExpenseList){
			// Creating a userExpense calendar
			Calendar userExpenseCalendar = Calendar.getInstance();
			userExpenseCalendar.setTime(userExpense.getExpenseDate());

			// Checking to which expense group object this expense corresponds to and updating its value
			boolean addNewExpenseGroup = true;
			for(ExpenseGroupDate expenseGroupDate:expenseGroupList){
				if(sameMonthYear(expenseGroupDate.getMonthYear(), userExpenseCalendar)){
					addNewExpenseGroup = false;
					expenseGroupDate.setAmount(expenseGroupDate.getAmount().add(userExpense.getExpense()));
				}
			}

			// Adding a new expense group to the list if the expense date didn't match any existing one
			if(addNewExpenseGroup){
				ExpenseGroupDate expenseGroupDate = new ExpenseGroupDate();
				expenseGroupDate.setMonthYear(userExpenseCalendar);
				expenseGroupDate.setAmount(userExpense.getExpense());
				expenseGroupList.add(expenseGroupDate);
			}
		}

		return expenseGroupList;
	}

	/**
	 * Determines whether both calendar objects correspond to same year and month
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean sameMonthYear(Calendar date1, Calendar date2){
		int year1 = date1.get(Calendar.YEAR);
		int month1 = date1.get(Calendar.MONTH);

		int year2 = date2.get(Calendar.YEAR);
		int month2 = date2.get(Calendar.MONTH);

		return (year1 == year2) && (month1 == month2);
	}

	/**
	 * Returns a list of ExpenseGroup items organized by CATEGORY
	 * @return
	 */
	private List<ExpenseGroupCategory> getCategoryExpenseGroupList(){
		List<ExpenseGroupCategory> expenseGroupList = new ArrayList<ExpenseGroupCategory>();

		List<UserExpense> userExpenseList = null;
		if(isMonthlyProject()){
			userExpenseList = getUserExpensesForCurrentProject(mCalendar);
		} else {
			userExpenseList = getUserExpensesForCurrentProject(null);
		}
		for(ExpenseCategoryMapper expenseCategoryMapper:ExpenseCategoryMapper.values()){
			// Creating new ExpenseGroup object
			ExpenseGroupCategory expenseGroup = new ExpenseGroupCategory();

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

			// Setting whether this is a primary view
			expenseGroup.setShowPrimary(mShowPrimaryView);
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
	 * Returns a boolean indicating whether this is a monthly project
	 * @return
	 */
	private boolean isMonthlyProject(){
		return mCurrentProject.getProjectType().getCod().equals(ProjectTypeMapper.monthly.toString());
	}

	/**
	 * Updates the content of the recycler
	 */
	public void updateRecycler(){
		// Getting a sorted list of SingleUserExpenses
		mExpenseGroupList = getExpenseGroupList();
		mTotalExpenseValue = getTotalExpenseValue();
		mMaxGroupExpenseValue = getMaxGroupExpenseValue();

		// Notify of all the ViewHolders that are going to be removed
		notifyDataSetChanged();
	}
	
	/**
	 * Sets the ShowPrimaryView boolean
	 * @param showPrimaryView
	 */
	public void setShowPrimaryView(boolean showPrimaryView){
		mShowPrimaryView = showPrimaryView;
	}

	/**
	 * Sets the BalanceMode for the expense group adapter
	 * @param balanceMode
	 */
	public void setBalanceMode(BalanceMode balanceMode) {
		mBalanceMode = balanceMode;
	}
	
	/**
	 * Gets the BalanceMode for the expense group adapter
	 * @param balanceMode
	 */
	public BalanceMode getBalanceMode() {
		return mBalanceMode;
	}
}
