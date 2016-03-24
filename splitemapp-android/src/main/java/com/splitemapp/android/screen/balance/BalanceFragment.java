package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.screen.expense.ExpenseAmountFormat;
import com.splitemapp.android.widget.LinearLayoutManager;
import com.splitemapp.commons.domain.Project;

public class BalanceFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = BalanceFragment.class.getSimpleName();

	private static final String CURRENCY_SIGN = "$";
	private static final int DIVISION_PRESICION = 4;

	private Project mCurrentProject;

	private View mFragmentView;

	private View mTopMonthView;
	private View mTopAverageView;

	private ExpenseAmountFormat mExpenseAmountFormat;
	private TextView mMonthTextView;
	private TextView mYearTextView;
	private TextView mTotalTextView;
	private TextView mBudgetTextView;
	private TextView mBalanceTextView;

	private View mLeftArrowView;
	private View mRightArrowView;

	private Calendar mCalendar;

	private RecyclerView mExpenseGroupRecycler;
	private ExpenseGroupAdapter mExpenseGroupAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private TextView mCategoryTextView;
	private TextView mUserTextView;
	private TextView mDateTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We get the current user and project instances
		try {
			mCurrentProject = getHelper().getProject(Globals.getExpenseActivityProjectId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// We get the current date by default
		mCalendar = Calendar.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		mFragmentView = super.onCreateView(inflater, container, savedInstanceState);

		// Setting the expense amount format
		mExpenseAmountFormat = new ExpenseAmountFormat();

		// Getting top layouts
		mTopMonthView = mFragmentView.findViewById(R.id.b_top_month_view);
		mTopAverageView = mFragmentView.findViewById(R.id.b_top_average_view);

		// If this is a one time project we dont show the months view
		if(!isMonthlyProject()){
			mTopMonthView.setVisibility(View.GONE);
			mTopAverageView.setVisibility(View.GONE);
		}

		// Set onClick listener for right/left arrows
		mLeftArrowView = mFragmentView.findViewById(R.id.b_left_arrow_imageView);
		mLeftArrowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mCalendar.add(Calendar.MONTH, -1);

				// Updating the fragment
				updateFragment();
			}
		});
		mRightArrowView = mFragmentView.findViewById(R.id.b_right_arrow_imageView);
		mRightArrowView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mCalendar.add(Calendar.MONTH, 1);

				// Updating the fragment
				updateFragment();
			}
		});

		// Set onClick listener for CATEGORY button
		mCategoryTextView = (TextView) mFragmentView.findViewById(R.id.b_category_textView);
		mCategoryTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Setting visibility for top views if it is a monthly project
				if(isMonthlyProject()){
					mTopMonthView.setVisibility(View.VISIBLE);
					mTopAverageView.setVisibility(View.GONE);
				}

				// Setting the balance mode to category
				mExpenseGroupAdapter.setBalanceMode(BalanceMode.CATEGORY);
				mExpenseGroupAdapter.setShowPrimaryView(true);

				// Updating the fragment
				updateFragment();
			}
		});

		// Set onClick listener for USER button
		mUserTextView = (TextView) mFragmentView.findViewById(R.id.b_user_textView);
		mUserTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Setting visibility for top views if it is a monthly project
				if(isMonthlyProject()){
					mTopMonthView.setVisibility(View.VISIBLE);
					mTopAverageView.setVisibility(View.GONE);
				}

				// Setting the balance mode to user
				mExpenseGroupAdapter.setBalanceMode(BalanceMode.USER);
				mExpenseGroupAdapter.setShowPrimaryView(true);

				// Updating the fragment
				updateFragment();
			}
		});

		// Set onClick listener for DATE button
		mDateTextView = (TextView) mFragmentView.findViewById(R.id.b_date_textView);
		mDateTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Setting visibility for top views
				mTopMonthView.setVisibility(View.GONE);
				mTopAverageView.setVisibility(View.VISIBLE);

				// Setting the balance mode to date
				mExpenseGroupAdapter.setBalanceMode(BalanceMode.DATE);

				// Updating the fragment
				updateFragment();
			}
		});

		// If this is a one time project we don't show the DATE view 
		if(!isMonthlyProject()){
			mDateTextView.setVisibility(View.GONE);
		}

		// Creating a single user expense adapter to be used in the recycler view
		mExpenseGroupAdapter = new ExpenseGroupAdapter(mCurrentProject, this, mCalendar, BalanceMode.CATEGORY);

		// We populate the list of projects for this user
		mExpenseGroupRecycler = (RecyclerView) mFragmentView.findViewById(R.id.b_expense_group_recyclerView);
		mExpenseGroupRecycler.setAdapter(mExpenseGroupAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mExpenseGroupRecycler.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mExpenseGroupRecycler.setLayoutManager(mLayoutManager);

		// Setting focusability to false to avoid going to the bottom of the screen
		mExpenseGroupRecycler.setFocusable(false);

		// Updating text views and recycler
		updateFragment();

		return mFragmentView;
	}

	/**
	 * Populates all text views with the updated value from the database
	 * @throws SQLException 
	 */
	private void updateTextViews(Long projectId) throws SQLException{
		if(mFragmentView != null){
			// Setting month
			mMonthTextView = (TextView) mFragmentView.findViewById(R.id.b_monthTextView);
			mMonthTextView.setText(MonthMapper.values()[mCalendar.get(Calendar.MONTH)].getStringId());

			// Setting year
			mYearTextView = (TextView) mFragmentView.findViewById(R.id.b_yearTextView);
			mYearTextView.setText(String.valueOf(mCalendar.get(Calendar.YEAR)));

			BigDecimal totalExpenseValue = null;
			if(isMonthlyProject()){
				// Setting total expense or average in case of DATE balance mode
				if(mExpenseGroupAdapter.getBalanceMode() == BalanceMode.DATE){
					totalExpenseValue = getHelper().getTotalExpenseValueByProjectId(projectId, null);
					totalExpenseValue = totalExpenseValue.divide(new BigDecimal(mExpenseGroupAdapter.getItemCount()), DIVISION_PRESICION,  RoundingMode.HALF_UP);
				} else {
					totalExpenseValue = getHelper().getTotalExpenseValueByProjectId(projectId, mCalendar);
				}
			} else {
				// Setting total expense
				totalExpenseValue = getHelper().getTotalExpenseValueByProjectId(projectId, null);
			}
			mTotalTextView = (TextView) mFragmentView.findViewById(R.id.b_total_textView);
			mTotalTextView.setText(CURRENCY_SIGN+mExpenseAmountFormat.format(totalExpenseValue));

			// Setting budget TextView
			BigDecimal budgetValue = mCurrentProject.getBudget();
			mBudgetTextView = (TextView) mFragmentView.findViewById(R.id.b_budget);
			mBudgetTextView.setText(CURRENCY_SIGN+mExpenseAmountFormat.format(budgetValue));

			// Setting balance TextView
			BigDecimal balanceValue = budgetValue.subtract(totalExpenseValue);
			mBalanceTextView = (TextView) mFragmentView.findViewById(R.id.b_balance);
			mBalanceTextView.setText(CURRENCY_SIGN+mExpenseAmountFormat.format(balanceValue.abs()));
			if(balanceValue.signum()>0){
				mBalanceTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
			} else {
				mBalanceTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
			}
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
	 * Makes all necessary updates to this fragment
	 */
	private void updateFragment(){
		try {
			// Getting current project from database
			mCurrentProject = getHelper().getProject(Globals.getExpenseActivityProjectId());

			// Updating the RecyclerView
			mExpenseGroupAdapter.updateRecycler();

			// Updating all TextViews
			updateTextViews(mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	protected void onRefresh(String response) {
		updateFragment();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateFragment();
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_balance;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.b_title;
	}

	@Override
	protected void doneAction() {
		// Simulate OnBackPressed
		getActivity().onBackPressed();
		//TODO Maybe we need to update the expense share if they changed
	}
}
