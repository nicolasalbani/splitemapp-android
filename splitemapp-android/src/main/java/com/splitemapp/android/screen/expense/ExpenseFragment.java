package com.splitemapp.android.screen.expense;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragmentWithBlueActionbar;
import com.splitemapp.android.screen.DatePickerFragment;
import com.splitemapp.android.widget.DecimalDigitsInputFilter;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class ExpenseFragment extends BaseFragmentWithBlueActionbar {

	private static final int MAX_DIGITS_BEFORE_DECIMAL = 5;
	private static final int MAX_DIGITS_AFTER_DECIMAL = 2;

	private static final String TAG = ExpenseFragment.class.getSimpleName();

	private User mCurrentUser;
	private Project mCurrentProject;
	private UserExpense mUserExpense;
	private short mSelectedCategory;

	private EditText mExpenseAmount;
	private TextView mExpenseDateText;
	private GridView mExpenseCategory;
	
	private CollapsingToolbarLayout collapsingToolbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try{
			// We get the current user and project instances
			mCurrentUser = getHelper().getLoggedUser();
			mCurrentProject = getHelper().getProjectById(Globals.getExpenseActivityProjectId());

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

		// We populate the expense category grid view
		mExpenseCategory = (GridView) v.findViewById(R.id.e_expense_categories_gridView);
		mExpenseCategory.setAdapter(getExpenseCategoryAdapter());
		mExpenseCategory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				mSelectedCategory = (short)position;
			}
		});


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
		collapsingToolbar = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);

		// We inflate the expense amount object
		mExpenseAmount = (EditText) v.findViewById(R.id.e_expense_amount_editText);
		mExpenseAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(MAX_DIGITS_BEFORE_DECIMAL,MAX_DIGITS_AFTER_DECIMAL)});
		mExpenseAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				collapsingToolbar.setTitle("$" +arg0);
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void afterTextChanged(Editable arg0) {}
		});

		// If we are editing the expense, we populate the values
		if(!isNewExpense()){
			mSelectedCategory = (short) (mUserExpense.getExpenseCategory().getId().shortValue()-1);
			mExpenseAmount.setText(mUserExpense.getExpense().toString());
			updateExpenseDateDisplay(mUserExpense);
		}

		return v;
	}

	private void saveExpense(){
		try {
			// We get an instance of the expense category
			Dao<ExpenseCategory,Short> expenseCategoryDao = getHelper().getExpenseCategoryDao();
			ExpenseCategory expenseCategory = expenseCategoryDao.queryForId((short)(mSelectedCategory+1));

			// We save the user expense to the DB
			Dao<UserExpense,Long> userExpensesDao = getHelper().getUserExpenseDao();
			mUserExpense.setExpense(new BigDecimal(mExpenseAmount.getText().toString()));
			mUserExpense.setExpenseCategory(expenseCategory);
			mUserExpense.setProject(mCurrentProject);
			mUserExpense.setUser(mCurrentUser);
			userExpensesDao.create(mUserExpense);

			// Moving back to the project screen
			getActivity().onBackPressed();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

	}
	
	private void updateExpense(){
		try {
			// We get an instance of the expense category
			Dao<ExpenseCategory,Short> expenseCategoryDao = getHelper().getExpenseCategoryDao();
			ExpenseCategory expenseCategory = expenseCategoryDao.queryForId((short)(mSelectedCategory+1));

			// We save the user expense to the DB
			Dao<UserExpense,Long> userExpensesDao = getHelper().getUserExpenseDao();
			mUserExpense.setExpense(new BigDecimal(mExpenseAmount.getText().toString()));
			mUserExpense.setExpenseCategory(expenseCategory);
			mUserExpense.setUpdatedAt(new Date());
			
			// TODO Only set the user if we are owning the expense
			// mUserExpense.setUser(mCurrentUser);
			
			userExpensesDao.update(mUserExpense);

			// Moving back to the project screen
			getActivity().onBackPressed();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

	}

	private ArrayAdapter<String> getExpenseCategoryAdapter(){
		String[] expenseCategoryArray = null;
		try {
			List<ExpenseCategory> expenseCategoryList = getHelper().getExpenseCategoryDao().queryForAll();
			List<String> expenseCategoryStringList = new ArrayList<String>();
			for(ExpenseCategory ec:expenseCategoryList){
				expenseCategoryStringList.add(ec.getTitle());
			}
			expenseCategoryArray = expenseCategoryStringList.toArray(new String[]{});
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, expenseCategoryArray);
		return adapter;
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
			saveExpense();
		} else {
			updateExpense();
		}
	}

}
