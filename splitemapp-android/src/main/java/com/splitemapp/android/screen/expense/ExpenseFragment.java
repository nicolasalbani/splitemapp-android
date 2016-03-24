package com.splitemapp.android.screen.expense;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.DatePickerFragment;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.widget.DecimalDigitsInputFilter;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class ExpenseFragment extends RestfulFragmentWithBlueActionbar {
	
	private static final int EXPENSE_CATEGORY_COLUMNS = 3;

	private static final String TAG = ExpenseFragment.class.getSimpleName();

	private User mCurrentUser;
	private Project mCurrentProject;
	private UserExpense mUserExpense;
	private short mSelectedCategory;

	private EditText mExpenseAmount;
	private ExpenseAmountFormat mExpenseAmountFormat;
	private TextView mExpenseDateText;

	private RecyclerView mExpenseCategoryRecycler;
	private ExpenseCategoryAdapter mExpenseCategoryAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private AppBarLayout appBarLayout;
	boolean showingExpense = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try{
			// We get the current user and project instances
			mCurrentUser = getHelper().getLoggedUser();
			mCurrentProject = getHelper().getProject(Globals.getExpenseActivityProjectId());

			// Setting the expense amount format
			mExpenseAmountFormat = new ExpenseAmountFormat();
			
			// If we got an expense id, we are meant to edit that expense
			if(isNewExpense()){
				mUserExpense = new UserExpense();
				mUserExpense.setExpenseDate(new Date());
			} else {
				mUserExpense = getHelper().getUserExpenseById(Globals.getExpenseActivityExpenseId());
			}
		} catch (SQLException e){
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		// Setting already selected category

		// We inflate the expense date text view and load todays date by default
		mExpenseDateText = (TextView) v.findViewById(R.id.e_expense_date_textView);
		mExpenseDateText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment fragment = new DatePickerFragment(){
					@Override
					public void onDateSet(DatePicker view, int year, int month, int day) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.MONTH, month);
						cal.set(Calendar.DAY_OF_MONTH, day);
						mUserExpense.setExpenseDate(cal.getTime());
						updateExpenseDateDisplay(mUserExpense);
					}};
					fragment.show(getActivity().getSupportFragmentManager(), TAG);
			}
		});
		updateExpenseDateDisplay(mUserExpense);

		// We enable showing the title in this particular screen
		appBarLayout = (AppBarLayout) v.findViewById(R.id.e_appBarLayout);
		appBarLayout.addOnOffsetChangedListener(new OnOffsetChangedListener(){
			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
				if(verticalOffset < -120 && !showingExpense){
					mCancel.setText("Expense: $" +mExpenseAmount.getText().toString());
					showingExpense = true;
				} else if (verticalOffset >= -120 && showingExpense) {
					mCancel.setText(getTitleResourceId());
					showingExpense = false;
				}
				Log.i(TAG, "onOffsetChanged: " +verticalOffset);
			}});

		// We inflate the expense amount object
		mExpenseAmount = (EditText) v.findViewById(R.id.e_expense_amount_editText);
		mExpenseAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(ExpenseAmountFormat.MAX_DIGITS_BEFORE_DECIMAL,ExpenseAmountFormat.MAX_DIGITS_AFTER_DECIMAL)});

		// If we are editing the expense, we populate the values
		if(!isNewExpense()){
			mSelectedCategory = (short) (mUserExpense.getExpenseCategory().getId().shortValue()-1);
			mExpenseAmount.setText(mExpenseAmountFormat.format(mUserExpense.getExpense()));
			updateExpenseDateDisplay(mUserExpense);
		}

		// Creating a single user expense adapter to be used in the recycler view
		mExpenseCategoryAdapter = new ExpenseCategoryAdapter(this, mSelectedCategory){
			@Override
			protected void onClick(View view, int position) {
				mSelectedCategory = (short)position;
			}
		};

		// We populate the list of projects for this user
		mExpenseCategoryRecycler = (RecyclerView) v.findViewById(R.id.e_expense_categories_recyclerView);
		mExpenseCategoryRecycler.setAdapter(mExpenseCategoryAdapter);

		// Using a grid layout manager
		mLayoutManager = new GridLayoutManager(getActivity(), EXPENSE_CATEGORY_COLUMNS);
		mExpenseCategoryRecycler.setLayoutManager(mLayoutManager);

		return v;
	}

	private void persistExpense(){
		try {
			// We get an instance of the expense category
			ExpenseCategory expenseCategory = getHelper().getExpenseCategory((short)(mSelectedCategory+1));

			// We save the user expense to the DB
			mUserExpense.setExpense(new BigDecimal(mExpenseAmount.getText().toString()));
			mUserExpense.setExpenseCategory(expenseCategory);
			mUserExpense.setProject(mCurrentProject);
			mUserExpense.setUser(mCurrentUser);
			getHelper().persistUserExpense(mUserExpense);

			// Pushing the changes
			pushUserExpenses();

			// Moving back to the project screen
			getActivity().onBackPressed();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

	}

	private void updateExpense(){
		try {
			// We get an instance of the expense category
			ExpenseCategory expenseCategory = getHelper().getExpenseCategory((short)(mSelectedCategory+1));

			// We save the user expense to the DB
			mUserExpense.setExpense(new BigDecimal(mExpenseAmount.getText().toString()));
			mUserExpense.setExpenseCategory(expenseCategory);
			mUserExpense.setUpdatedAt(new Date());

			// TODO Only set the user if we are owning the expense
			// mUserExpense.setUser(mCurrentUser);

			getHelper().updateUserExpense(mUserExpense);

			// Pushing the changes
			pushUserExpenses();

			// Moving back to the project screen
			getActivity().onBackPressed();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

	}

	/**
	 * Updates the expense date textView based on the content of the userExpense object
	 * @param userExpense
	 */
	private void updateExpenseDateDisplay(UserExpense userExpense){
		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
		Date date = userExpense.getExpenseDate();
		mExpenseDateText.setText(dateFormat.format(date));
	}

	/**
	 * Returns boolean indicating whether this is a new expense or we are editing one
	 * @return
	 */
	private boolean isNewExpense(){
		Long expenseActivityExpenseId = Globals.getExpenseActivityExpenseId();
		if(expenseActivityExpenseId == null){
			return true;
		} else {
			return false;
		}
	}


	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_expense;
	}

	@Override
	protected int getTitleResourceId() {
		if(isNewExpense()){
			return R.string.e_new_title;
		} else {
			return R.string.e_edit_title;
		}

	}

	@Override
	protected void doneAction() {
		if(isNewExpense()){
			persistExpense();
		} else {
			updateExpense();
		}
	}

}
