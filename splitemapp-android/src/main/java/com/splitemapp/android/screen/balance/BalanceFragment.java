package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.commons.domain.Project;

public class BalanceFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = BalanceFragment.class.getSimpleName();
	
	private static final String CURRENCY_SIGN = "$";

	private Project mCurrentProject;

	private View mFragmentView;

	private DecimalFormat mExpenseAmountFormat;
	private TextView mMonthTextView;
	private TextView mYearTextView;
	private TextView mTotalTextView;
	private TextView mBudgetTextView;
	private TextView mBalanceTextView;

	private Calendar mCalendar;

	private RecyclerView mExpenseGroupRecycler;
	private ExpenseGroupAdapter mExpenseGroupAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

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
		mExpenseAmountFormat = new DecimalFormat();
		mExpenseAmountFormat.setMaximumFractionDigits(Constants.MAX_DIGITS_AFTER_DECIMAL);
		mExpenseAmountFormat.setMinimumFractionDigits(Constants.MAX_DIGITS_AFTER_DECIMAL);

		// Populating the balance screen TextViews
		try {
			updateTextViews(mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught while updating TextViews", e);
		}

		// Creating a single user expense adapter to be used in the recycler view
		mExpenseGroupAdapter = new ExpenseGroupAdapter(mCurrentProject, this);

		// We populate the list of projects for this user
		mExpenseGroupRecycler = (RecyclerView) mFragmentView.findViewById(R.id.b_expense_group_recyclerView);
		mExpenseGroupRecycler.setAdapter(mExpenseGroupAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mExpenseGroupRecycler.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mExpenseGroupRecycler.setLayoutManager(mLayoutManager);

		// Setting the default animator for the view
		mExpenseGroupRecycler.setItemAnimator(new CustomItemAnimator());

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

			// Setting total expense
			BigDecimal totalExpenseValue = getHelper().getTotalExpenseValueByProjectId(projectId);
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
	 * Makes all necessary updates to this fragment
	 */
	private void updateFragment(){
		try {
			// Getting current project from database
			mCurrentProject = getHelper().getProject(Globals.getExpenseActivityProjectId());

			// Updating all TextViews
			updateTextViews(mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}

		// Updating the RecyclerView
		mExpenseGroupAdapter.updateRecycler();

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
	}
}
