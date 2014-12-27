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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.DatePickerFragment;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class ExpenseFragment extends BaseFragment {

	public static final String EXTRA_PROJECT_ID = "com.splitemapp.android.project_id";
	public static final String EXTRA_USER_ID = "com.splitemapp.android.user_id";
	public static final String EXTRA_EXPENSE_ID = "com.splitemapp.android.expense_id";

	private static final String DECIMAL_DELIMITER_KEY = ".";
	private static final String DELETE_KEY = "<";
	private static final String EXPENSE_AMOUNT_INITIAL_VALUE = "0";
	private static final String[] CALCULATOR_VALUES = new String[]{"1","2","3","4","5","6","7","8","9",DECIMAL_DELIMITER_KEY,"0",DELETE_KEY};

	private static final String TAG = ExpenseFragment.class.getSimpleName();

	private User mCurrentUser;
	private Project mCurrentProject;
	private UserExpense mUserExpense;
	private int mSelectedCategory;

	private TextView mExpenseAmount;
	private TextView mExpenseDateText;
	private GridView mExpenseCategory;
	private GridView mExpenseCalculator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getActivity().getIntent().getExtras();

		// We get the current user and project instances
		Long userId = (Long)arguments.getSerializable(EXTRA_USER_ID);
		mCurrentUser = getCurrentUser(userId);
		Long projectId = (Long)arguments.getSerializable(EXTRA_PROJECT_ID);
		mCurrentProject = getCurrentProject(projectId);

		// If we got an expense id, we are meant to edit that expense
		Long userExpenseId = (Long)arguments.getSerializable(EXTRA_EXPENSE_ID);
		if(userExpenseId != null){
			mUserExpense = getUserExpense(userExpenseId);
		} else {
			mUserExpense = new UserExpense();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_add_expense, container, false);

		// We populate the expense category grid view
		mExpenseCategory = (GridView) v.findViewById(R.id.ae_expense_categories_gridView);
		mExpenseCategory.setAdapter(getExpenseCategoryAdapter());
		mExpenseCategory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				mSelectedCategory = position;
			}
		});
		if(mUserExpense.getId() != null){
			mSelectedCategory = mUserExpense.getId().intValue()-1;
		}

		// We inflate the expense date text view and load todays date by default
		mExpenseDateText = (TextView) v.findViewById(R.id.ae_expense_date_textView);
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
						updateExpenseDateDisplay();
					}};
					fragment.show(getActivity().getSupportFragmentManager(), TAG);
			}
		});
		if(mUserExpense.getExpenseDate() == null){
			mUserExpense.setExpenseDate(new Date());
		}
		updateExpenseDateDisplay();

		// We inflate the expense amount object
		mExpenseAmount = (TextView) v.findViewById(R.id.ae_expense_amount_textView);
		if(mUserExpense.getExpense() != null){
			mExpenseAmount.setText(mUserExpense.getExpense().toString());
		}

		// We populate the expense calculator grid view
		mExpenseCalculator = (GridView) v.findViewById(R.id.ae_expense_calculator_gridView);
		mExpenseCalculator.setAdapter(getExpenseCalculatorAdapter());
		mExpenseCalculator.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				String pressedKey = CALCULATOR_VALUES[position];
				if(pressedKey == DECIMAL_DELIMITER_KEY){
					if(!mExpenseAmount.getText().toString().contains(DECIMAL_DELIMITER_KEY)){
						mExpenseAmount.setText(mExpenseAmount.getText()+pressedKey);
					}
				} else if (pressedKey == DELETE_KEY){
					int amountLength = mExpenseAmount.getText().length();
					if(amountLength == 1){
						mExpenseAmount.setText(EXPENSE_AMOUNT_INITIAL_VALUE);
					} else {
						mExpenseAmount.setText(mExpenseAmount.getText().subSequence(0, amountLength-1));
					}
				} else {
					if(mExpenseAmount.getText().equals(EXPENSE_AMOUNT_INITIAL_VALUE)){
						mExpenseAmount.setText(pressedKey);
					} else {
						mExpenseAmount.setText(mExpenseAmount.getText() + pressedKey);
					}
				}
			}
		});
		return v;
	}

	public void saveExpense(){
		try {
			// We get an instance of the expense category
			Dao<ExpenseCategory,Integer> expenseCategoryDao = getHelper().getExpenseCategoryDao();
			ExpenseCategory expenseCategory = expenseCategoryDao.queryForId(mSelectedCategory+1);

			// We save the user expense to the DB
			Dao<UserExpense,Integer> userExpensesDao = getHelper().getUserExpensesDao();
			mUserExpense.setExpense(new BigDecimal(mExpenseAmount.getText().toString()));
			mUserExpense.setExpenseCategory(expenseCategory);
			mUserExpense.setProject(mCurrentProject);
			mUserExpense.setUser(mCurrentUser);
			userExpensesDao.createOrUpdate(mUserExpense);
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

	private void updateExpenseDateDisplay(){
		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
		String date = dateFormat.format(mUserExpense.getExpenseDate());
		mExpenseDateText.setText(date);
	}

	private ArrayAdapter<String> getExpenseCalculatorAdapter(){
		ExpenseCalculatorAdapter adapter = new ExpenseCalculatorAdapter(CALCULATOR_VALUES);
		return adapter;
	}

	private class ExpenseCalculatorAdapter extends ArrayAdapter<String>{

		public ExpenseCalculatorAdapter(String[] calculatorKey){
			super(getActivity(), 0, calculatorKey);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//If we weren't given a view, inflate one
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_calculator, parent, false);
			}

			// We obtain the calculator key
			String calculatorKey = getItem(position);

			TextView calculatorKeyTextView = (TextView)convertView.findViewById(R.id.ae_calculator_key_textView);
			calculatorKeyTextView.setText(calculatorKey);

			return convertView;
		}
	}

	private Project getCurrentProject(Long projectId){
		Project project = null;
		try {
			Dao<Project,Integer> projectDao = getHelper().getProjectDao();
			project = projectDao.queryForId(projectId.intValue());
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return project;
	}

	private UserExpense getUserExpense(Long userExpenseId){
		UserExpense userExpense = null;
		try {
			// We get the user expense
			Dao<UserExpense,Integer> userExpensesDao = getHelper().getUserExpensesDao();
			userExpense = userExpensesDao.queryForId(userExpenseId.intValue());

			// We get the expense category
			Dao<ExpenseCategory,Integer> expenseCategoryDao = getHelper().getExpenseCategoryDao();
			ExpenseCategory expenseCategory = expenseCategoryDao.queryForId(userExpense.getExpenseCategory().getId().intValue());

			userExpense.setExpenseCategory(expenseCategory);
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return userExpense;
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

}
